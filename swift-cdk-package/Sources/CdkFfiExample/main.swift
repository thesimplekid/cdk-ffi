import Foundation
import CdkFfi

@main
struct CdkFfiExample {
    static func main() async {
        print("CDK FFI Swift Example")
        
        do {
            // Generate a mnemonic
            let mnemonic = try generateMnemonic()
            print("Generated mnemonic: \(mnemonic)")
            
            // Create a local store
            let localStore = try FfiLocalStore()
            print("Created local store")
            
            // Create a wallet
            let mintUrl = "https://mint.minibits.cash/Bitcoin"
            let wallet = try FfiWallet.fromMnemonic(
                mintUrl: mintUrl,
                unit: .sat,
                localstore: localStore,
                mnemonicWords: mnemonic
            )
            
            print("Created wallet for mint: \(wallet.mintUrl())")
            print("Wallet unit: \(wallet.unit())")
            
            // Get mint info
            let mintInfo = try wallet.getMintInfo()
            print("Mint info: \(mintInfo)")
            
            // Check balance
            let balance = try wallet.balance()
            print("Wallet balance: \(balance.value) sats")
            
        } catch {
            print("Error: \(error)")
        }
    }
}
