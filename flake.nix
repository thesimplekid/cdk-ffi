{
  description = "CDK FFI - Cashu Development Kit FFI bindings";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    rust-overlay.url = "github:oxalica/rust-overlay";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, rust-overlay, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        overlays = [ (import rust-overlay) ];
        pkgs = import nixpkgs {
          inherit system overlays;
        };
        
        # Use the latest stable Rust version
        rustToolchain = pkgs.rust-bin.stable.latest.default.override {
          extensions = [ "rust-src" "clippy" "rustfmt" ];
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Rust toolchain
            rustToolchain
            
            # Required system dependencies for building
            pkg-config
            openssl
            sqlite
            protobuf
            
            # Build tools
            cmake
            gcc
            
            # Development tools
            rust-analyzer
            cargo-watch
            cargo-edit
            just
            
            # UniFFI might need additional tools for bindings generation
            python3
            python3Packages.pip
            python3Packages.setuptools
            
            # For Swift bindings (if needed)
            # swift # Uncomment if generating Swift bindings
            
            # For Kotlin/Java bindings
            jdk
            ktlint
            
            # For testing and development
            curl
            git
          ];

          # Environment variables
          RUST_SRC_PATH = "${rustToolchain}/lib/rustlib/src/rust/library";
          PKG_CONFIG_PATH = "${pkgs.openssl.dev}/lib/pkgconfig:${pkgs.sqlite.dev}/lib/pkgconfig";
          
          # SQLite configuration for cdk-sqlite
          SQLITE3_INCLUDE_DIR = "${pkgs.sqlite.dev}/include";
          SQLITE3_LIB_DIR = "${pkgs.sqlite.out}/lib";
          
          # OpenSSL configuration (for networking dependencies)
          OPENSSL_DIR = "${pkgs.openssl.dev}";
          OPENSSL_LIB_DIR = "${pkgs.openssl.out}/lib";
          OPENSSL_INCLUDE_DIR = "${pkgs.openssl.dev}/include";

          # Protobuf configuration
          PROTOC = "${pkgs.protobuf}/bin/protoc";
          PROTOC_INCLUDE = "${pkgs.protobuf}/include";

          shellHook = ''
            echo "CDK FFI Development Environment"
            echo "Rust version: $(rustc --version)"
            echo "Cargo version: $(cargo --version)"
            echo "Just version: $(just --version)"
            echo ""
            echo "Available commands:"
            echo "  just --list                 - List all available just commands"
            echo "  just build                  - Build the project"
            echo "  just build-kotlin           - Build Kotlin bindings"
            echo "  just build-all-bindings     - Build all language bindings"
            echo "  just test                   - Run tests"
            echo "  just lint                   - Run linter"
            echo "  just fmt                    - Format code"
            echo ""
            echo "Direct cargo commands:"
            echo "  cargo build                 - Build the project"
            echo "  cargo test                  - Run tests"
            echo "  cargo clippy                - Run linter"
            echo "  cargo fmt                   - Format code"
            echo "  cargo run --bin uniffi-bindgen - Generate UniFFI bindings"
            echo ""
            echo "SQLite path: ${pkgs.sqlite.out}/lib"
            echo "OpenSSL path: ${pkgs.openssl.out}"
            echo "Protobuf compiler: ${pkgs.protobuf}/bin/protoc"
          '';
        };

        # For building the project in CI/CD
        packages.default = pkgs.rustPlatform.buildRustPackage {
          pname = "cdk-ffi";
          version = "0.1.0";
          src = ./.;

          cargoLock = {
            lockFile = ./Cargo.lock;
          };

          nativeBuildInputs = with pkgs; [
            pkg-config
            rustToolchain
          ];

          buildInputs = with pkgs; [
            openssl
            sqlite
            protobuf
          ] ++ pkgs.lib.optionals pkgs.stdenv.isDarwin [
            pkgs.darwin.apple_sdk.frameworks.Security
            pkgs.darwin.apple_sdk.frameworks.SystemConfiguration
          ];

          # Environment variables for the build
          SQLITE3_INCLUDE_DIR = "${pkgs.sqlite.dev}/include";
          SQLITE3_LIB_DIR = "${pkgs.sqlite.out}/lib";
          OPENSSL_DIR = "${pkgs.openssl.dev}";
          OPENSSL_LIB_DIR = "${pkgs.openssl.out}/lib";
          OPENSSL_INCLUDE_DIR = "${pkgs.openssl.dev}/include";

          # Protobuf configuration
          PROTOC = "${pkgs.protobuf}/bin/protoc";
          PROTOC_INCLUDE = "${pkgs.protobuf}/include";

          meta = with pkgs.lib; {
            description = "FFI bindings for the Cashu Development Kit (CDK) wallet";
            homepage = "https://github.com/cashubtc/cdk";
            license = licenses.mit;
            maintainers = [ ];
          };
        };
      });
}
