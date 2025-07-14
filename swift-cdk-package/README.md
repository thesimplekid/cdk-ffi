# CdkFfi - Swift CDK Package

A Swift package providing bindings for the CDK (Cashu Development Kit) library, enabling Swift applications to interact with Cashu ecash systems.

## Features

- **Wallet Management**: Create and manage Cashu wallets
- **Mnemonic Generation**: Generate and restore wallets from mnemonic phrases
- **Mint Operations**: Mint ecash tokens from Lightning invoices
- **Melt Operations**: Pay Lightning invoices using ecash
- **Token Management**: Send and receive ecash tokens
- **Local Storage**: Persistent wallet storage
- **Multiple Currency Units**: Support for sat, msat, USD, EUR

## Installation

### Swift Package Manager

Add the package to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/cashubtc/cdk-ffi", from: "1.0.0")
]
```

Or add it through Xcode:
1. File → Add Package Dependencies
2. Enter the repository URL
3. Choose the version and target

## Quick Start

```swift
import CdkFfi

// Generate a mnemonic phrase
let mnemonic = try generateMnemonic()
print("Mnemonic: \(mnemonic)")

// Create a local store for wallet data
let localStore = try FfiLocalStore()

// Create a wallet from mnemonic
let mintUrl = "https://mint.example.com"
let wallet = try FfiWallet.fromMnemonic(
    mintUrl: mintUrl,
    unit: .sat,
    localstore: localStore,
    mnemonicWords: mnemonic
)

// Initialize mint information
let mintInfo = try wallet.getMintInfo()
print("Mint info: \(mintInfo)")

// Check wallet balance
let balance = try wallet.balance()
print("Balance: \(balance.value) sats")
```

## Usage Examples

### Creating a Mint Quote

```swift
let amount = FfiAmount(value: 1000) // 1000 sats
let description = "Payment for services"

let quote = try wallet.mintQuote(amount: amount, description: description)
print("Quote ID: \(quote.id)")
print("Payment request: \(quote.request)")
```

### Minting Tokens

```swift
let splitTarget = FfiSplitTarget.default
let mintedAmount = try wallet.mint(quoteId: quote.id, splitTarget: splitTarget)
print("Minted: \(mintedAmount.value) sats")
```

### Sending Tokens

```swift
let sendAmount = FfiAmount(value: 100)
let sendOptions = FfiSendOptions(
    memo: nil,
    amountSplitTarget: .default,
    sendKind: .onlineExact,
    includeFee: true,
    metadata: [:],
    maxProofs: nil
)

let token = try wallet.send(amount: sendAmount, options: sendOptions, memo: nil)
print("Token: \(token.tokenString)")
```

### Creating a Melt Quote (Pay Lightning Invoice)

```swift
let invoice = "lnbc..." // Lightning invoice
let meltQuote = try wallet.meltQuote(request: invoice)
print("Fee reserve: \(meltQuote.feeReserve.value) sats")
```

### Paying Lightning Invoice

```swift
let melted = try wallet.melt(quoteId: meltQuote.id)
print("Payment state: \(melted.state)")
if let preimage = melted.preimage {
    print("Payment preimage: \(preimage)")
}
```

## Data Types

### Core Types

- `FfiWallet`: Main wallet interface
- `FfiLocalStore`: Local storage for wallet data
- `FfiAmount`: Represents amounts with value in smallest unit
- `FfiToken`: Represents ecash tokens

### Currency Units

```swift
enum FfiCurrencyUnit {
    case sat     // Bitcoin satoshis
    case msat    // Bitcoin millisatoshis
    case usd     // US Dollars
    case eur     // Euros
}
```

### Send Options

```swift
let sendOptions = FfiSendOptions(
    memo: FfiSendMemo(memo: "Payment memo", includeMemo: true),
    amountSplitTarget: .default,
    sendKind: .onlineExact,
    includeFee: true,
    metadata: ["key": "value"],
    maxProofs: 10
)
```

## Error Handling

The library uses Swift's error handling mechanism. All operations that can fail throw errors:

```swift
do {
    let wallet = try FfiWallet.fromMnemonic(/* ... */)
    let balance = try wallet.balance()
} catch let error as FfiError {
    switch error {
    case .WalletError(let msg):
        print("Wallet error: \(msg)")
    case .NetworkError(let msg):
        print("Network error: \(msg)")
    case .InvalidInput(let msg):
        print("Invalid input: \(msg)")
    case .InternalError(let msg):
        print("Internal error: \(msg)")
    }
} catch {
    print("Unexpected error: \(error)")
}
```

## Platform Support

- iOS 13.0+
- macOS 10.15+
- tvOS 13.0+
- watchOS 6.0+

## Requirements

- Swift 5.9+
- The native CDK library (`libcdk_ffi`) must be available

## License

This package follows the same license as the CDK project.

## Contributing

Contributions are welcome! Please check the main CDK repository for contribution guidelines.

## Links

- [CDK Repository](https://github.com/cashubtc/cdk)
- [Cashu Protocol](https://github.com/cashubtc/nuts)
- [Swift Documentation](https://developer.apple.com/swift/)
