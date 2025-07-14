# CDK FFI Kotlin Example

This project demonstrates how to use the CDK (Cashu Development Kit) FFI Kotlin bindings in a Kotlin application.

## Prerequisites

- JDK 11 or higher
- Gradle (or use the wrapper)
- Linux system (for the native library)

## Project Structure

```
kotlin-cdk-example/
├── build.gradle.kts                    # Gradle build configuration
├── gradle/wrapper/                     # Gradle wrapper
├── src/main/kotlin/
│   ├── com/example/
│   │   ├── Main.kt                     # Simple example
│   │   └── WalletManager.kt            # Advanced wallet management
│   └── uniffi/cdk_ffi/
│       └── cdk_ffi.kt                  # CDK FFI bindings
└── src/main/resources/
    └── libcdk_ffi.so                   # Native library
```

## Building and Running

### 1. Build the project
```bash
./gradlew build
```

### 2. Run the simple example
```bash
./gradlew run
```

### 3. Run in development mode
```bash
./gradlew run --console=plain
```

## Usage Examples

### Basic Usage

```kotlin
import uniffi.cdk_ffi.*

// Generate a mnemonic
val mnemonic = generateMnemonic()

// Create local store
val localStore = FfiLocalStore()

// Create wallet
val wallet = FfiWallet.fromMnemonic(
    mintUrl = "https://testnut.cashu.space",
    unit = FfiCurrencyUnit.SAT,
    localstore = localStore,
    mnemonicWords = mnemonic
)

// Check balance
val balance = wallet.balance()
println("Balance: ${balance.value} sats")

// Create mint quote
val amount = FfiAmount(value = 100UL)
val mintQuote = wallet.mintQuote(amount = amount, description = "Test")
```

### Using the Wallet Manager

```kotlin
val walletManager = CdkWalletManager("https://testnut.cashu.space")

// Create wallet
walletManager.createWallet().fold(
    onSuccess = { message -> println("Success: $message") },
    onFailure = { error -> println("Error: ${error.message}") }
)

// Get balance
walletManager.getBalance().fold(
    onSuccess = { balance -> println("Balance: $balance sats") },
    onFailure = { error -> println("Error: ${error.message}") }
)
```

## Available Operations

- **Mnemonic Generation**: `generateMnemonic()`
- **Wallet Creation**: `FfiWallet.fromMnemonic(...)`
- **Balance Checking**: `wallet.balance()`
- **Mint Operations**: `wallet.mintQuote(...)` and `wallet.mint(...)`
- **Melt Operations**: `wallet.meltQuote(...)` and `wallet.melt(...)`
- **Mint Info**: `wallet.getMintInfo()`

## Error Handling

The bindings throw `FfiException` for CDK-specific errors. Always wrap calls in try-catch blocks:

```kotlin
try {
    val balance = wallet.balance()
    println("Balance: ${balance.value}")
} catch (e: FfiException) {
    println("CDK Error: ${e.message}")
} catch (e: Exception) {
    println("Unexpected error: ${e.message}")
}
```

## Data Types

- **FfiAmount**: Represents amounts in satoshis
- **FfiCurrencyUnit**: SAT, MSAT, USD, EUR
- **FfiSplitTarget**: NONE, DEFAULT
- **FfiSendKind**: OnlineExact, OnlineTolerance, etc.
- **FfiMintQuoteState**: UNPAID, PAID, ISSUED

## Troubleshooting

### Library Loading Issues
If you get `UnsatisfiedLinkError`:
1. Ensure `libcdk_ffi.so` is in `src/main/resources/`
2. Check that the library is built for your system architecture
3. Verify JVM system properties are set correctly

### Network Issues
- Ensure the mint URL is accessible
- Some operations require a running Cashu mint
- Network-related failures are expected if mint is unavailable

## Advanced Usage

For more complex scenarios, see the `WalletManager.kt` class which provides:
- Result-based error handling
- Wrapper methods for common operations
- Better separation of concerns

## Dependencies

- Kotlin Standard Library
- JNA (Java Native Access) for FFI
- Kotlin Coroutines (optional, for async operations)
