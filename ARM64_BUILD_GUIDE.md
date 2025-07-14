# ARM64 Cross-Compilation Guide

## Overview
The CDK FFI project now supports cross-compilation to ARM64 (aarch64) for both Kotlin and Swift bindings.

## Prerequisites
The Nix development environment includes all necessary cross-compilation tools:
- ARM64 Rust target (`aarch64-unknown-linux-gnu`)
- Cross-compilation toolchain
- ARM64 system libraries (OpenSSL, SQLite)

## Available Commands

### Direct Rust Building
```bash
# Build ARM64 release
cargo build --target=aarch64-unknown-linux-gnu --release

# Build ARM64 debug
cargo build --target=aarch64-unknown-linux-gnu
```

### Just Commands

#### Rust Library
```bash
# Build ARM64 release
just build-arm64

# Build ARM64 debug  
just build-arm64-debug
```

#### Kotlin Bindings
```bash
# Build Kotlin bindings for ARM64 (release)
just build-kotlin-arm64
```

#### Swift Bindings
```bash
# Build Swift bindings for ARM64 (release)
just build-swift-arm64

# Build Swift bindings for ARM64 (debug)
just build-swift-arm64-debug

# Build Swift bindings for ARM64 without formatting
just build-swift-arm64-no-format
```

#### All Bindings
```bash
# Build all language bindings for ARM64
just build-all-bindings-arm64
```

## Generated Files

### Directory Structure
```
target/aarch64-unknown-linux-gnu/
├── release/
│   └── libcdk_ffi.so           # ARM64 release library
└── debug/
    └── libcdk_ffi.so           # ARM64 debug library

bindings/
├── kotlin/
│   ├── libcdk_ffi.so           # ARM64 library copy
│   └── uniffi/cdk_ffi/
│       └── cdk_ffi.kt          # Kotlin bindings
├── swift/
│   ├── libcdk_ffi.so           # ARM64 library copy
│   ├── cdk_ffi.swift           # Swift bindings
│   ├── cdk_ffiFFI.h            # C header
│   └── cdk_ffiFFI.modulemap    # Module map
└── python/
    ├── libcdk_ffi.so           # ARM64 library copy
    └── cdk_ffi.py              # Python bindings
```

## Verification

### Check Library Architecture
```bash
# Verify ARM64 architecture
file target/aarch64-unknown-linux-gnu/release/libcdk_ffi.so
# Output: ELF 64-bit LSB shared object, ARM aarch64, version 1 (SYSV)

# Verify bindings libraries
file bindings/kotlin/libcdk_ffi.so
file bindings/swift/libcdk_ffi.so
```

### List All Available Commands
```bash
# Show all ARM64-specific commands
just --list | grep -E "(arm64|ARM64)"
```

## Cross-Compilation Environment

The Nix flake automatically sets up the cross-compilation environment with:

- **Rust Target**: `aarch64-unknown-linux-gnu`
- **C Compiler**: `aarch64-unknown-linux-gnu-gcc`
- **Linker**: `aarch64-unknown-linux-gnu-gcc`
- **Cross Libraries**: ARM64 versions of OpenSSL and SQLite

### Environment Variables
The build automatically uses these environment variables:
```bash
CC_aarch64_unknown_linux_gnu=aarch64-unknown-linux-gnu-gcc
CARGO_TARGET_AARCH64_UNKNOWN_LINUX_GNU_LINKER=aarch64-unknown-linux-gnu-gcc
PKG_CONFIG_ALLOW_CROSS=1
```

## Usage in Projects

### Kotlin Projects
Use the generated Kotlin bindings (`bindings/kotlin/`) with the ARM64 library for deployment on ARM64 systems.

### Swift Projects
Use the generated Swift bindings (`bindings/swift/`) with the ARM64 library for deployment on ARM64 systems.

## Troubleshooting

### Build Fails
1. Ensure you're in the Nix development shell: `nix develop`
2. Verify cross-compilation tools are available:
   ```bash
   which aarch64-unknown-linux-gnu-gcc
   ```

### Formatting Warnings
You may see warnings like:
```
Warning: Unable to auto-format cdk_ffi.swift using swiftformat: Os { code: 2, kind: NotFound, message: "No such file or directory" }
Warning: Unable to auto-format cdk_ffi.py using yapf: Os { code: 2, kind: NotFound, message: "No such file or directory" }
```

These are **non-fatal warnings** - the bindings are still generated successfully. The formatters are optional tools that improve code style but don't affect functionality.

### Missing Dependencies
The Nix environment should handle all dependencies automatically. If issues arise, rebuild the environment:
```bash
nix develop --refresh
```

## Development Workflow

For ARM64 development:
```bash
# Build ARM64 library and all bindings
just build-all-bindings-arm64

# Or build specific bindings
just build-kotlin-arm64
just build-swift-arm64

# Verify architecture
file bindings/kotlin/libcdk_ffi.so
```
