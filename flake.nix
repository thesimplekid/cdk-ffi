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
        
        # Cross-compilation setup
        pkgsCrossAarch64 = import nixpkgs {
          inherit system overlays;
          crossSystem = {
            config = "aarch64-unknown-linux-gnu";
          };
        };
        
        # Use the latest stable Rust version with ARM64 target
        rustToolchain = pkgs.rust-bin.stable.latest.default.override {
          extensions = [ "rust-src" "clippy" "rustfmt" ];
          targets = [ "aarch64-unknown-linux-gnu" ];
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
            
            # Cross-compilation tools for ARM64
            pkgsCrossAarch64.stdenv.cc
            pkgsCrossAarch64.openssl.dev
            pkgsCrossAarch64.sqlite.dev
            
            # Development tools
            rust-analyzer
            cargo-watch
            cargo-edit
            just
            
            # UniFFI might need additional tools for bindings generation
            python3
            python3Packages.pip
            python3Packages.setuptools
            python3Packages.requests
            python3Packages.urllib3
            
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

          # Cross-compilation environment variables for ARM64
          CC_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.stdenv.cc}/bin/aarch64-unknown-linux-gnu-gcc";
          CXX_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.stdenv.cc}/bin/aarch64-unknown-linux-gnu-g++";
          AR_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.stdenv.cc}/bin/aarch64-unknown-linux-gnu-ar";
          CARGO_TARGET_AARCH64_UNKNOWN_LINUX_GNU_LINKER = "${pkgsCrossAarch64.stdenv.cc}/bin/aarch64-unknown-linux-gnu-gcc";
          PKG_CONFIG_ALLOW_CROSS = "1";
          OPENSSL_DIR_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.openssl.dev}";
          OPENSSL_LIB_DIR_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.openssl.out}/lib";
          SQLITE3_INCLUDE_DIR_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.sqlite.dev}/include";
          SQLITE3_LIB_DIR_aarch64_unknown_linux_gnu = "${pkgsCrossAarch64.sqlite.out}/lib";

          shellHook = ''
            echo "CDK FFI Development Environment"
            echo "Rust version: $(rustc --version)"
            echo "Cargo version: $(cargo --version)"
            echo "Just version: $(just --version)"
            echo "Gradle version: $(gradle --version | head -3)"
            echo ""
            echo "Cross-compilation targets available:"
            echo "  - aarch64-unknown-linux-gnu (ARM64 Linux)"
            echo ""
            echo "Available commands:"
            echo "  just --list                 - List all available just commands"
            echo "  just build                  - Build the project"
            echo "  just build-kotlin           - Build Kotlin bindings"
            echo "  just build-all-bindings     - Build all language bindings"
            echo "  just kotlin-example         - Build and run Kotlin example"
            echo "  just kotlin-build           - Build Kotlin example project"
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
            echo "Cross-compilation commands:"
            echo "  cargo build --target=aarch64-unknown-linux-gnu - Build for ARM64 Linux"
            echo "  cargo build --target=aarch64-unknown-linux-gnu --release - Build ARM64 release"
            echo ""
            echo "Kotlin example commands:"
            echo "  cd kotlin-cdk-example && ./gradlew build   - Build Kotlin example"
            echo "  cd kotlin-cdk-example && ./gradlew run     - Run Kotlin example"
            echo ""
            echo "SQLite path: ${pkgs.sqlite.out}/lib"
            echo "OpenSSL path: ${pkgs.openssl.out}"
            echo "Protobuf compiler: ${pkgs.protobuf}/bin/protoc"
            echo "Gradle path: $(which gradle)"
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
