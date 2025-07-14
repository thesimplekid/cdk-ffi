# Getting Started with CDK Swift

This guide will help you get started with using the CDK Swift package in your iOS or macOS application.

## Installation

### Add to Xcode Project

1. Open Xcode and your project
2. Go to **File > Add Package Dependencies**
3. Enter the package URL: `https://github.com/cashubtc/cdk-ffi` (when available)
4. Select the version you want to use
5. Add to your target

### Add to Swift Package

Add this to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/cashubtc/cdk-ffi", from: "1.0.0")
],
targets: [
    .target(
        name: "YourTarget",
        dependencies: [
            .product(name: "CdkFfi", package: "cdk-ffi")
        ]
    )
]
```

## Basic Setup

### 1. Import the module

```swift
import CdkFfi
```

### 2. Create a wallet

```swift
do {
    // Generate a new mnemonic
    let mnemonic = try generateMnemonic()
    
    // Create local storage
    let localStore = try FfiLocalStore()
    
    // Create wallet
    let wallet = try FfiWallet.fromMnemonic(
        mintUrl: "https://mint.example.com",
        unit: .sat,
        localstore: localStore,
        mnemonicWords: mnemonic
    )
    
    // Initialize mint info
    let _ = try wallet.getMintInfo()
    
    print("Wallet created successfully!")
    print("Mint URL: \(wallet.mintUrl())")
    print("Unit: \(wallet.unit())")
    
} catch {
    print("Error creating wallet: \(error)")
}
```

### 3. Check balance

```swift
do {
    let balance = try wallet.balance()
    print("Current balance: \(balance.value) \(wallet.unit())")
} catch {
    print("Error getting balance: \(error)")
}
```

## Common Operations

### Requesting Payment (Mint)

```swift
// Create a mint quote for receiving payment
let amount = FfiAmount(value: 1000) // 1000 sats
let quote = try wallet.mintQuote(amount: amount, description: "Payment request")

print("Payment request: \(quote.request)")
print("Quote ID: \(quote.id)")

// After invoice is paid, mint the ecash
let mintedAmount = try wallet.mint(quoteId: quote.id, splitTarget: .default)
print("Minted: \(mintedAmount.value) sats")
```

### Sending Payment (Send Token)

```swift
let sendAmount = FfiAmount(value: 100) // 100 sats
let sendOptions = FfiSendOptions(
    memo: nil,
    amountSplitTarget: .default,
    sendKind: .onlineExact,
    includeFee: true,
    metadata: [:],
    maxProofs: nil
)

let token = try wallet.send(amount: sendAmount, options: sendOptions, memo: nil)
print("Token to share: \(token.tokenString)")
```

### Paying Lightning Invoice (Melt)

```swift
let invoice = "lnbc1000n1..." // Lightning invoice

// Create melt quote
let meltQuote = try wallet.meltQuote(request: invoice)
print("Fee: \(meltQuote.feeReserve.value) sats")

// Pay the invoice
let melted = try wallet.melt(quoteId: meltQuote.id)
print("Payment status: \(melted.state)")

if let preimage = melted.preimage {
    print("Payment proof: \(preimage)")
}
```

## Error Handling

The CDK uses Swift's native error handling. All operations that can fail throw errors:

```swift
do {
    let result = try wallet.someOperation()
    // Handle success
} catch let error as FfiError {
    switch error {
    case .WalletError(let message):
        print("Wallet error: \(message)")
    case .NetworkError(let message):
        print("Network error: \(message)")
    case .InvalidInput(let message):
        print("Invalid input: \(message)")
    case .InternalError(let message):
        print("Internal error: \(message)")
    }
} catch {
    print("Unknown error: \(error)")
}
```

## Using with SwiftUI

```swift
import SwiftUI
import CdkFfi

class WalletManager: ObservableObject {
    @Published var wallet: FfiWallet?
    @Published var balance: UInt64 = 0
    @Published var error: String?
    
    func createWallet() {
        Task {
            do {
                let mnemonic = try generateMnemonic()
                let localStore = try FfiLocalStore()
                let newWallet = try FfiWallet.fromMnemonic(
                    mintUrl: "https://mint.example.com",
                    unit: .sat,
                    localstore: localStore,
                    mnemonicWords: mnemonic
                )
                
                try newWallet.getMintInfo()
                
                await MainActor.run {
                    self.wallet = newWallet
                    self.updateBalance()
                }
            } catch {
                await MainActor.run {
                    self.error = error.localizedDescription
                }
            }
        }
    }
    
    func updateBalance() {
        guard let wallet = wallet else { return }
        
        Task {
            do {
                let walletBalance = try wallet.balance()
                await MainActor.run {
                    self.balance = walletBalance.value
                }
            } catch {
                await MainActor.run {
                    self.error = error.localizedDescription
                }
            }
        }
    }
}

struct ContentView: View {
    @StateObject private var walletManager = WalletManager()
    
    var body: some View {
        VStack {
            if let wallet = walletManager.wallet {
                Text("Balance: \(walletManager.balance) sats")
                Button("Refresh Balance") {
                    walletManager.updateBalance()
                }
            } else {
                Button("Create Wallet") {
                    walletManager.createWallet()
                }
            }
            
            if let error = walletManager.error {
                Text("Error: \(error)")
                    .foregroundColor(.red)
            }
        }
        .padding()
    }
}
```

## Threading and Async Operations

The CDK operations are synchronous but can block, so it's recommended to run them on background threads:

```swift
Task.detached {
    do {
        let result = try wallet.someOperation()
        
        await MainActor.run {
            // Update UI with result
        }
    } catch {
        await MainActor.run {
            // Handle error
        }
    }
}
```

## Best Practices

1. **Always handle errors**: CDK operations can fail for various reasons
2. **Use background threads**: Don't block the main thread with CDK operations  
3. **Store mnemonic securely**: Use Keychain for production apps
4. **Validate inputs**: Check amounts and addresses before operations
5. **Update mint info**: Call `getMintInfo()` after creating a wallet
6. **Check balances**: Verify sufficient balance before sending

## Next Steps

- Check out the [full API documentation](README.md)
- Look at the [example app](Sources/CdkFfiExample/main.swift)
- Run the [test suite](Tests/CdkFfiTests/CdkFfiTests.swift) to understand usage patterns
- Explore the [CDK documentation](https://github.com/cashubtc/cdk) for more details on Cashu protocol
