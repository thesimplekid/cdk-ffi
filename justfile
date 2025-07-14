# CDK FFI Build Commands
# Use `just --list` to see all available commands

# Variables
kotlin_dir := "bindings/kotlin"
swift_dir := "bindings/swift"
python_dir := "bindings/python"
lib_name := "libcdk_ffi"
kotlin_example_dir := "kotlin-cdk-example"

# Default command - show help
default:
    @just --list

# Build the Rust library in release mode
build:
    cargo build --release

# Build the Rust library for ARM64 in release mode
build-arm64:
    cargo build --target=aarch64-unknown-linux-gnu --release

# Build the Rust library in debug mode
build-debug:
    cargo build

# Build the Rust library for ARM64 in debug mode
build-arm64-debug:
    cargo build --target=aarch64-unknown-linux-gnu

# Run tests
test:
    cargo test

# Run clippy linter
lint:
    cargo clippy --all-targets --all-features

# Format code
fmt:
    cargo fmt --all

# Check formatting
fmt-check:
    cargo fmt --all -- --check

# Clean build artifacts
clean:
    cargo clean
    rm -rf bindings/

# Create binding directories
_create-dirs:
    mkdir -p {{kotlin_dir}}
    mkdir -p {{swift_dir}}
    mkdir -p {{python_dir}}

# Build Kotlin bindings (release)
build-kotlin: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}}
    @echo "Kotlin bindings generated in {{kotlin_dir}}/"

# Build Kotlin bindings for ARM64 (release)
build-kotlin-arm64: build-arm64 _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/aarch64-unknown-linux-gnu/release/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}}
    @echo "Kotlin bindings (ARM64) generated in {{kotlin_dir}}/"

# Build Kotlin bindings without formatting (release)
build-kotlin-no-format: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}} --no-format
    @echo "Kotlin bindings (no formatting) generated in {{kotlin_dir}}/"

# Build Kotlin bindings (debug)
build-kotlin-debug: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}}
    @echo "Kotlin bindings (debug) generated in {{kotlin_dir}}/"

# Build Kotlin bindings without formatting (debug)
build-kotlin-debug-no-format: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}} --no-format
    @echo "Kotlin bindings (debug, no formatting) generated in {{kotlin_dir}}/"

# Build Swift bindings (release)
build-swift: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language swift --out-dir {{swift_dir}}
    cp target/release/{{lib_name}}.so {{swift_dir}}/
    @echo "Swift bindings generated in {{swift_dir}}/"

# Build Swift bindings without formatting (release)
build-swift-no-format: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language swift --out-dir {{swift_dir}} --no-format
    cp target/release/{{lib_name}}.so {{swift_dir}}/
    @echo "Swift bindings (no formatting) generated in {{swift_dir}}/"

# Build Swift bindings (debug)
build-swift-debug: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language swift --out-dir {{swift_dir}}
    cp target/debug/{{lib_name}}.so {{swift_dir}}/
    @echo "Swift bindings (debug) generated in {{swift_dir}}/"

# Build Swift bindings without formatting (debug)
build-swift-debug-no-format: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language swift --out-dir {{swift_dir}} --no-format
    cp target/debug/{{lib_name}}.so {{swift_dir}}/
    @echo "Swift bindings (debug, no formatting) generated in {{swift_dir}}/"

# Build Python bindings (release)
build-python: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language python --out-dir {{python_dir}}
    @echo "Python bindings generated in {{python_dir}}/"

# Build Python bindings (debug)
build-python-debug: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language python --out-dir {{python_dir}}
    @echo "Python bindings (debug) generated in {{python_dir}}/"

# Build all language bindings (release)
build-all-bindings: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}}
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language swift --out-dir {{swift_dir}}
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language python --out-dir {{python_dir}}
    cp target/release/{{lib_name}}.so {{swift_dir}}/
    cp target/release/{{lib_name}}.so {{kotlin_dir}}/
    cp target/release/{{lib_name}}.so {{python_dir}}/
    @echo "All bindings generated:"
    @echo "  Kotlin: {{kotlin_dir}}/"
    @echo "  Swift: {{swift_dir}}/"
    @echo "  Python: {{python_dir}}/"

# Build all language bindings without formatting (release)
build-all-bindings-no-format: build _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}} --no-format
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language swift --out-dir {{swift_dir}} --no-format
    cargo run --bin uniffi-bindgen generate --library target/release/{{lib_name}}.so --language python --out-dir {{python_dir}} --no-format
    cp target/release/{{lib_name}}.so {{swift_dir}}/
    cp target/release/{{lib_name}}.so {{kotlin_dir}}/
    cp target/release/{{lib_name}}.so {{python_dir}}/
    @echo "All bindings (no formatting) generated:"
    @echo "  Kotlin: {{kotlin_dir}}/"
    @echo "  Swift: {{swift_dir}}/"
    @echo "  Python: {{python_dir}}/"

# Build all language bindings (debug)
build-all-bindings-debug: build-debug _create-dirs
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}}
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language swift --out-dir {{swift_dir}}
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language python --out-dir {{python_dir}}
    cp target/debug/{{lib_name}}.so {{swift_dir}}/
    cp target/debug/{{lib_name}}.so {{kotlin_dir}}/
    cp target/debug/{{lib_name}}.so {{python_dir}}/
    @echo "All bindings (debug) generated:"
    @echo "  Kotlin: {{kotlin_dir}}/"
    @echo "  Swift: {{swift_dir}}/"
    @echo "  Python: {{python_dir}}/"

# Generate bindings using UDL approach (alternative method)
build-kotlin-udl: build _create-dirs
    cargo run --bin uniffi-bindgen generate src/lib.rs --language kotlin --out-dir {{kotlin_dir}}
    @echo "Kotlin bindings generated using UDL approach in {{kotlin_dir}}/"

# Show generated binding files
show-bindings:
    @echo "Generated binding files:"
    @find bindings/ -type f 2>/dev/null || echo "No bindings found. Run 'just build-kotlin' or 'just build-all-bindings' first."

# Full development workflow
dev: fmt lint test build-kotlin
    @echo "Development workflow complete!"

# Full CI/CD workflow
ci: fmt-check lint test build-all-bindings
    @echo "CI/CD workflow complete!"

# Watch for changes and rebuild Kotlin bindings
watch-kotlin:
    cargo watch -x "build" -s "just build-kotlin-debug"

# Watch for changes and run tests
watch-test:
    cargo watch -x test

# Show library information
info:
    @echo "Library name: {{lib_name}}"
    @echo "Kotlin bindings directory: {{kotlin_dir}}"
    @echo "Swift bindings directory: {{swift_dir}}"
    @echo "Python bindings directory: {{python_dir}}"
    @echo ""
    @echo "Available library files:"
    @find target/ -name "{{lib_name}}*" 2>/dev/null || echo "No library files found. Run 'just build' first."

# Install development dependencies (if needed)
install-deps:
    @echo "Installing development dependencies..."
    cargo install cargo-watch cargo-edit
    @echo "Dependencies installed!"

# Format Kotlin bindings with ktlint (requires ktlint in PATH)
format-kotlin:
    @if [ -d "{{kotlin_dir}}" ]; then \
        echo "Formatting Kotlin files in {{kotlin_dir}}/..."; \
        find {{kotlin_dir}} -name "*.kt" -exec ktlint --format {} \; || echo "Warning: ktlint formatting failed, but files are still usable"; \
    else \
        echo "No Kotlin bindings found. Run 'just build-kotlin' first."; \
    fi

# Quick Kotlin build for development
quick-kotlin: build-debug
    cargo run --bin uniffi-bindgen generate --library target/debug/{{lib_name}}.so --language kotlin --out-dir {{kotlin_dir}} --no-format
    @echo "Quick Kotlin bindings generated in {{kotlin_dir}}/"

# Build and run the Kotlin example project
kotlin-example: build-kotlin
    @echo "Building and running Kotlin CDK example..."
    @if [ ! -d "{{kotlin_example_dir}}" ]; then \
        echo "Error: {{kotlin_example_dir}} directory not found!"; \
        echo "Make sure the Kotlin example project exists."; \
        exit 1; \
    fi
    cd {{kotlin_example_dir}} && \
    ([ -f gradlew ] || gradle wrapper --gradle-version 8.4) && \
    ./gradlew build --quiet && \
    echo "✅ Kotlin example built successfully" && \
    echo "🚀 Running Kotlin example..." && \
    ./gradlew run --console=plain

# Build the Kotlin example project only
kotlin-build: build-kotlin
    @echo "Building Kotlin CDK example project..."
    @if [ ! -d "{{kotlin_example_dir}}" ]; then \
        echo "Error: {{kotlin_example_dir}} directory not found!"; \
        echo "Make sure the Kotlin example project exists."; \
        exit 1; \
    fi
    cd {{kotlin_example_dir}} && \
    ([ -f gradlew ] || gradle wrapper --gradle-version 8.4) && \
    ./gradlew build --quiet && \
    echo "✅ Kotlin example built successfully"

# Clean the Kotlin example project
kotlin-clean:
    @echo "Cleaning Kotlin CDK example project..."
    @if [ -d "{{kotlin_example_dir}}" ]; then \
        cd {{kotlin_example_dir}} && \
        (./gradlew clean --quiet 2>/dev/null || echo "Gradle clean skipped") && \
        echo "✅ Kotlin example cleaned"; \
    else \
        echo "{{kotlin_example_dir}} not found, nothing to clean"; \
    fi

# Run the Kotlin example without rebuilding
kotlin-run:
    @echo "Running Kotlin CDK example..."
    @if [ ! -d "{{kotlin_example_dir}}" ]; then \
        echo "Error: {{kotlin_example_dir}} directory not found!"; \
        echo "Run 'just kotlin-example' to build and run."; \
        exit 1; \
    fi
    cd {{kotlin_example_dir}} && ./gradlew run --console=plain

# Test the Kotlin example project
kotlin-test: build-kotlin
    @echo "Testing Kotlin CDK example project..."
    @if [ ! -d "{{kotlin_example_dir}}" ]; then \
        echo "Error: {{kotlin_example_dir}} directory not found!"; \
        exit 1; \
    fi
    cd {{kotlin_example_dir}} && \
    ./gradlew test --quiet && \
    echo "✅ Kotlin example tests passed"

# Show Kotlin example project status
kotlin-status:
    @echo "Kotlin CDK Example Project Status:"
    @echo "=================================="
    @if [ -d "{{kotlin_example_dir}}" ]; then \
        echo "✅ Project directory: {{kotlin_example_dir}}/"; \
        echo "📁 Project structure:"; \
        find {{kotlin_example_dir}} -type f \( -name "*.kt" -o -name "*.kts" -o -name "gradlew*" \) | head -10; \
        echo ""; \
        if [ -f "{{kotlin_example_dir}}/gradlew" ]; then \
            echo "✅ Gradle wrapper found"; \
        else \
            echo "⚠️  Gradle wrapper not found (will be created automatically)"; \
        fi; \
        if [ -f "{{kotlin_dir}}/uniffi/cdk_ffi/cdk_ffi.kt" ]; then \
            echo "✅ CDK bindings available"; \
        else \
            echo "❌ CDK bindings missing (run 'just build-kotlin' first)"; \
        fi; \
    else \
        echo "❌ Project directory not found: {{kotlin_example_dir}}/"; \
        echo "   Create the Kotlin example project first."; \
    fi
