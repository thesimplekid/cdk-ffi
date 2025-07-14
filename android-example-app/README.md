# CDK Wallet Demo - Android

This is a demo Android application that showcases the CDK (Cashu Development Kit) FFI Kotlin bindings for building a Lightning-enabled Cashu wallet.

## Features

- **Mnemonic Generation**: Generates and securely stores wallet mnemonics
- **Wallet Management**: Create and restore wallets using the CDK FFI
- **Mint Operations**: Create Lightning invoices and mint Cashu tokens
- **Send Tokens**: Send Cashu tokens with QR code generation
- **Pay Lightning**: Pay Lightning invoices using Cashu tokens
- **QR Code Support**: Generate and scan QR codes for invoices and tokens
- **Balance Tracking**: Real-time wallet balance updates

## Architecture

The app follows MVVM architecture with:

- **MainActivity**: Main wallet dashboard
- **SendActivity**: Send tokens and pay Lightning invoices
- **ReceiveActivity**: Create invoices and mint tokens
- **QRCodeActivity**: QR code generation and scanning
- **WalletViewModel**: Wallet state management and CDK FFI integration

## CDK FFI Integration

This app demonstrates integration with the CDK FFI Kotlin bindings:

- **FfiWallet**: Core wallet operations (mint, melt, send)
- **FfiLocalStore**: Local storage management
- **FfiMintQuote/FfiMeltQuote**: Lightning invoice operations
- **FfiToken**: Cashu token handling

## Setup Requirements

### Android Studio
- Android Studio Arctic Fox or later
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### Dependencies
The app includes all necessary dependencies:
- AndroidX libraries
- Material Design Components
- JNA for native library loading
- ZXing for QR code functionality
- Kotlin Coroutines

### CDK FFI Library
The app includes the CDK FFI native library and Kotlin bindings:
- `libcdk_ffi.so` - Native library (ARM64)
- `uniffi/cdk_ffi/cdk_ffi.kt` - Kotlin bindings

## Building the App

1. **Open in Android Studio**:
   ```bash
   cd android-example-app
   ```
   Then open the project in Android Studio

2. **Build and Run**:
   - Build → Make Project
   - Run → Run 'app'

3. **Install via Command Line**:
   ```bash
   ./gradlew installDebug
   ```

## App Structure

```
app/
├── src/main/
│   ├── kotlin/com/example/cdkwallet/
│   │   ├── MainActivity.kt           # Main dashboard
│   │   ├── WalletViewModel.kt        # Wallet state management
│   │   ├── SendActivity.kt           # Send tokens/pay invoices
│   │   ├── ReceiveActivity.kt        # Create invoices/mint tokens
│   │   └── QRCodeActivity.kt         # QR code handling
│   ├── kotlin/uniffi/cdk_ffi/
│   │   └── cdk_ffi.kt               # CDK FFI Kotlin bindings
│   ├── jniLibs/arm64-v8a/
│   │   └── libcdk_ffi.so            # Native CDK library
│   ├── res/                         # UI resources and layouts
│   └── AndroidManifest.xml          # App configuration
└── build.gradle.kts                 # Build configuration
```

## Usage Instructions

### Initial Setup
1. **Launch the app** - The wallet will initialize automatically
2. **Mnemonic Generation** - A 12-word mnemonic is generated and stored securely
3. **Mint Connection** - The app connects to a testnet Cashu mint

### Receiving Payments
1. **Tap "Receive"** on the main screen
2. **Enter amount** in satoshis
3. **Add description** (optional)
4. **Tap "Create Lightning Invoice"**
5. **Share the QR code** for payment
6. **Wait for payment** - The app polls for payment status
7. **Tap "Mint Tokens"** once paid to add funds to wallet

### Sending Tokens
1. **Tap "Send"** on the main screen
2. **Enter amount** and optional memo
3. **Tap "Prepare Send"** to see fees
4. **Tap "Send Tokens"** to create Cashu token
5. **Share the QR code** with recipient

### Paying Lightning Invoices
1. **Tap "Send"** on the main screen
2. **Paste Lightning invoice** in the bottom section
3. **Tap "Pay Invoice"** to create melt quote and pay

## Technical Details

### CDK FFI Functions Used
- `generateMnemonic()` - Generate wallet mnemonic
- `FfiWallet.fromMnemonic()` - Create wallet from mnemonic
- `wallet.mintQuote()` - Create Lightning invoice
- `wallet.mint()` - Mint tokens after payment
- `wallet.prepareSend()` - Calculate fees for sending
- `wallet.send()` - Create Cashu token
- `wallet.meltQuote()` - Create melt quote for payments
- `wallet.melt()` - Pay Lightning invoice

### Error Handling
- Network errors (mint unavailable)
- Invalid inputs (amounts, invoices)
- Wallet state errors
- Quote expiration handling

### Security Considerations
- Mnemonic stored in Android SharedPreferences (encrypted on device)
- Wallet database stored in app-private directory
- Network communication over HTTPS
- Native library signature verification

## Customization

### Mint Configuration
To use a different Cashu mint, modify the `defaultMintUrl` in `WalletViewModel.kt`:

```kotlin
private val defaultMintUrl = "https://your-mint-url.com"
```

### UI Theming
Colors and themes can be modified in:
- `res/values/colors.xml`
- `res/values/themes.xml`

## Troubleshooting

### Common Issues

1. **Mint Connection Errors**:
   - Check internet connection
   - Verify mint URL is accessible
   - Some operations may work offline

2. **Native Library Loading**:
   - Ensure ARM64 device/emulator
   - Check that `libcdk_ffi.so` is in correct jniLibs folder

3. **Build Errors**:
   - Clean and rebuild project
   - Check Kotlin version compatibility
   - Verify all dependencies are resolved

### Debug Logging
Enable verbose logging by adding to `WalletViewModel.kt`:

```kotlin
private fun log(message: String) {
    android.util.Log.d("CDKWallet", message)
}
```

## Testing

### Unit Tests
The app includes basic error handling tests. Run with:
```bash
./gradlew test
```

### Manual Testing
1. Test wallet creation and restoration
2. Test Lightning invoice creation and payment
3. Test token sending and receiving
4. Test QR code generation and scanning

## Contributing

This is a demo application showcasing CDK FFI integration. For contributing to the core CDK project, see the main repository.

## License

This demo application follows the same license as the CDK project.

---

For more information about CDK and Cashu, visit:
- [CDK Repository](https://github.com/cashubtc/cdk)
- [Cashu Protocol](https://github.com/cashubtc/nuts)
