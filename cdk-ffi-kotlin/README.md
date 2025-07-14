# CDK FFI Kotlin

Kotlin bindings for the [Cashu Development Kit (CDK)](https://github.com/cashubtc/cdk), enabling Cashu wallet functionality in Kotlin/JVM applications.

## Installation

### Gradle

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.cashubtc:cdk-ffi-kotlin:0.1.0")
}
```

Or in `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.cashubtc:cdk-ffi-kotlin:0.1.0'
}
```

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.cashubtc</groupId>
    <artifactId>cdk-ffi-kotlin</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```kotlin
import com.github.cashubtc.cdk.*

fun main() {
    // Generate a new mnemonic
    val mnemonic = generateMnemonic()
    println("Generated mnemonic: $mnemonic")
    
    // Create a new wallet
    val wallet = walletBuilder()
        .mintUrl("https://mint.example.com")
        .unit(CdkCurrencyUnit.SAT)
        .fromMnemonic(mnemonic)
    
    // Initialize mint info
    wallet.getMintInfo()
    
    // Check balance
    val balance = wallet.balance()
    println("Wallet balance: ${balance.value} sats")
    
    // Clean up
    wallet.destroy()
}
```

## Features

- **Wallet Management**: Create and restore wallets from mnemonic phrases
- **Minting**: Request mint quotes and mint new tokens
- **Sending**: Send tokens to other wallets with various options
- **Melting**: Pay Lightning invoices by melting tokens
- **Balance Management**: Check wallet balances and transaction history

## API Documentation

### Core Functions

#### Mnemonic Generation
```kotlin
val mnemonic: String = generateMnemonic()
```

#### Wallet Creation
```kotlin
// Create new wallet
val wallet = walletBuilder()
    .mintUrl("https://mint.example.com")
    .unit(CdkCurrencyUnit.SAT)
    .dbPath("/path/to/wallet.db") // optional
    .fromMnemonic(mnemonic)

// Restore existing wallet
val wallet = walletBuilder()
    .mintUrl("https://mint.example.com")
    .unit(CdkCurrencyUnit.SAT)
    .restoreFromMnemonic(existingMnemonic)
```

#### Basic Operations
```kotlin
// Initialize mint information
wallet.getMintInfo()

// Check balance
val balance = wallet.balance()

// Get mint URL and unit
val mintUrl = wallet.mintUrl()
val unit = wallet.unit()
```

#### Minting (Receiving) Tokens
```kotlin
val amount = createAmount(1000UL) // 1000 sats
val description = "Payment for services"

// Create mint quote
val quote = wallet.mintQuote(amount, description)
println("Pay this invoice: ${quote.request}")

// Check quote status
val quoteState = wallet.mintQuoteState(quote.id)

// If paid, mint the tokens
if (quoteState.state == CdkMintQuoteState.PAID) {
    val mintedAmount = wallet.mint(quote.id, CdkSplitTarget.DEFAULT)
    println("Minted ${mintedAmount.value} sats")
}
```

#### Sending Tokens
```kotlin
val amount = createAmount(500UL) // 500 sats
val sendOptions = createSendOptions(
    splitTarget = CdkSplitTarget.DEFAULT,
    sendKind = CdkSendKind.OnlineExact,
    includeFee = true
)

// Prepare send (estimate fees)
val prepared = wallet.prepareSend(amount, sendOptions)
println("Total fee: ${prepared.totalFee.value} sats")

// Send tokens
val token = wallet.send(amount, sendOptions, null)
println("Token: ${token.tokenString}")
```

#### Melting (Paying Lightning Invoices)
```kotlin
val invoice = "lnbc..." // Lightning invoice

// Create melt quote
val meltQuote = wallet.meltQuote(invoice)
println("Fee estimate: ${meltQuote.feeReserve.value} sats")

// Pay the invoice
val melted = wallet.melt(meltQuote.id)
if (melted.state == "PAID") {
    println("Payment successful!")
    println("Preimage: ${melted.preimage}")
}
```

### Amount Utilities
```kotlin
val amount = createAmount(1000UL)

// Check properties
val isZero = amount.isZero()
val isPositive = amount.isPositive()

// Get values in different types
val asLong = amount.longValue
val asULong = amount.ulongValue
```

### Error Handling
```kotlin
try {
    val wallet = walletBuilder()
        .mintUrl("https://invalid-mint.com")
        .fromMnemonic(mnemonic)
} catch (e: CdkException.NetworkException) {
    println("Network error: ${e.message}")
} catch (e: CdkException.WalletException) {
    println("Wallet error: ${e.message}")
} catch (e: CdkException) {
    println("CDK error: ${e.message}")
}
```

## Memory Management

The CDK FFI library uses native resources that need to be properly cleaned up:

```kotlin
// Always destroy wallets when done
wallet.use { w ->
    // Use wallet
    val balance = w.balance()
} // Automatically destroyed

// Or manually
wallet.destroy()
```

## Platform Support

This library supports:
- JVM 11+
- Android API 21+
- Linux x86_64
- Linux ARM64
- macOS (planned)
- Windows (planned)

## Native Library

The package includes the native library (`libcdk_ffi.so`) for the target platform. The library is automatically loaded when you import the package.

## Examples

See the [examples](examples/) directory for complete working examples.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
