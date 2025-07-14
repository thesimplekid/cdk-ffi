import XCTest
@testable import CdkFfi

final class CdkFfiTests: XCTestCase {
    func testMnemonicGeneration() throws {
        let mnemonic = try generateMnemonic()
        XCTAssertFalse(mnemonic.isEmpty)
        
        // Check that it has 12 words
        let words = mnemonic.split(separator: " ")
        XCTAssertEqual(words.count, 12, "Mnemonic should have 12 words")
    }
    
    func testLocalStoreCreation() throws {
        let localStore = try FfiLocalStore()
        XCTAssertNotNil(localStore)
    }
    
    func testLocalStoreWithPath() throws {
        let tempDir = FileManager.default.temporaryDirectory
        let dbPath = tempDir.appendingPathComponent("test.db").path
        
        let localStore = try FfiLocalStore.newWithPath(dbPath: dbPath)
        XCTAssertNotNil(localStore)
        
        // Clean up
        try? FileManager.default.removeItem(atPath: dbPath)
    }
    
    func testWalletCreation() throws {
        let mnemonic = try generateMnemonic()
        let localStore = try FfiLocalStore()
        
        let mintUrl = "https://mint.minibits.cash/Bitcoin"
        let wallet = try FfiWallet.fromMnemonic(
            mintUrl: mintUrl,
            unit: .sat,
            localstore: localStore,
            mnemonicWords: mnemonic
        )
        
        XCTAssertEqual(wallet.mintUrl(), mintUrl)
        XCTAssertEqual(wallet.unit(), "sat")
    }
    
    func testAmountEquality() {
        let amount1 = FfiAmount(value: 100)
        let amount2 = FfiAmount(value: 100)
        let amount3 = FfiAmount(value: 200)
        
        XCTAssertEqual(amount1, amount2)
        XCTAssertNotEqual(amount1, amount3)
    }
    
    func testCurrencyUnitValues() {
        // Test that currency units are properly defined
        let sat = FfiCurrencyUnit.sat
        let msat = FfiCurrencyUnit.msat
        let usd = FfiCurrencyUnit.usd
        let eur = FfiCurrencyUnit.eur
        
        XCTAssertNotEqual(sat, msat)
        XCTAssertNotEqual(sat, usd)
        XCTAssertNotEqual(sat, eur)
    }
}
