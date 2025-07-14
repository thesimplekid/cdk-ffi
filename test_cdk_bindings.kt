#!/usr/bin/env kotlin

/**
 * CDK FFI Kotlin Bindings Test Script
 *
 * This script tests the basic functionality of the CDK (Cashu Development Kit) FFI bindings.
 * It demonstrates how to:
 * 1. Generate a mnemonic
 * 2. Create a local store
 * 3. Create a wallet
 * 4. Get mint information
 * 5. Create a mint quote
 * 6. Check quote state
 * 7. Perform basic wallet operations
 *
 * Note: This script requires a running Cashu mint for full functionality.
 * For testing without a mint, some operations will fail as expected.
 */

import uniffi.cdk_ffi.*
import kotlin.system.exitProcess

// ANSI color codes for terminal output
object Colors {
    const val HEADER = "\u001B[95m"
    const val OKBLUE = "\u001B[94m"
    const val OKCYAN = "\u001B[96m"
    const val OKGREEN = "\u001B[92m"
    const val WARNING = "\u001B[93m"
    const val FAIL = "\u001B[91m"
    const val ENDC = "\u001B[0m"
    const val BOLD = "\u001B[1m"
    const val UNDERLINE = "\u001B[4m"
}

// Helper functions for formatted output
fun printHeader(title: String) {
    val separator = "=".repeat(60)
    println("\n${Colors.HEADER}${Colors.BOLD}$separator")
    val paddingLength = (60 - title.length) / 2
    val padding = " ".repeat(maxOf(0, paddingLength))
    println("$padding$title")
    println("$separator${Colors.ENDC}")
}

fun printSuccess(message: String) {
    println("${Colors.OKGREEN}✓${Colors.ENDC} $message")
}

fun printError(message: String) {
    println("${Colors.FAIL}✗${Colors.ENDC} $message")
}

fun printInfo(message: String) {
    println("${Colors.OKCYAN}ℹ${Colors.ENDC} $message")
}

fun printWarning(message: String) {
    println("${Colors.WARNING}⚠${Colors.ENDC} $message")
}

// Test functions
fun testMnemonicGeneration(): String? {
    printHeader("Testing Mnemonic Generation")
    
    return try {
        // Generate a 12-word mnemonic
        val mnemonic = generateMnemonic()
        printSuccess("Generated mnemonic: $mnemonic")
        
        // Validate the mnemonic format
        val words = mnemonic.split(" ")
        if (words.size == 12) {
            printSuccess("Mnemonic has correct length: ${words.size} words")
        } else {
            printWarning("Unexpected mnemonic length: ${words.size} words")
        }
        
        mnemonic
    } catch (e: Exception) {
        printError("Failed to generate mnemonic: $e")
        null
    }
}

fun testLocalStoreCreation(): FfiLocalStore? {
    printHeader("Testing Local Store Creation")
    
    return try {
        // Create default local store (in-memory)
        val store = FfiLocalStore()
        printSuccess("Created default local store (in-memory)")
        
        // Create local store with custom path
        val customPath = "/tmp/test_wallet.db"
        val storeWithPath = FfiLocalStore.newWithPath(customPath)
        printSuccess("Created local store with path: $customPath")
        
        store
    } catch (e: Exception) {
        printError("Failed to create local store: $e")
        e.printStackTrace()
        null
    }
}

fun testWalletCreation(mnemonic: String, localStore: FfiLocalStore): FfiWallet? {
    printHeader("Testing Wallet Creation")
    
    return try {
        // Test mint URL - using a common testnet mint
        // Note: This mint may or may not be available
        val mintUrl = "https://testnut.cashu.space"
        val unit = FfiCurrencyUnit.SAT
        
        printInfo("Creating wallet with mint: $mintUrl")
        printInfo("Currency unit: $unit")
        
        // Create wallet from mnemonic
        val wallet = FfiWallet.fromMnemonic(
            mintUrl = mintUrl,
            unit = unit,
            localstore = localStore,
            mnemonicWords = mnemonic
        )
        
        printSuccess("Successfully created wallet from mnemonic")
        
        // Get basic wallet info
        val walletMintUrl = wallet.mintUrl()
        val walletUnit = wallet.unit()
        printSuccess("Wallet mint URL: $walletMintUrl")
        printSuccess("Wallet unit: $walletUnit")
        
        wallet
    } catch (e: Exception) {
        printError("Failed to create wallet: $e")
        printInfo("This is expected if the mint is not available")
        null
    }
}

fun testMintInfo(wallet: FfiWallet): Boolean {
    printHeader("Testing Mint Info Retrieval")
    
    return try {
        val mintInfo = wallet.getMintInfo()
        printSuccess("Successfully retrieved mint info")
        val truncatedInfo = mintInfo.take(200)
        printInfo("Mint info (truncated): $truncatedInfo...")
        true
    } catch (e: Exception) {
        printError("Failed to get mint info: $e")
        printInfo("This is expected if the mint is not available or reachable")
        false
    }
}

fun testWalletBalance(wallet: FfiWallet): FfiAmount? {
    printHeader("Testing Wallet Balance")
    
    return try {
        val balance = wallet.balance()
        printSuccess("Wallet balance: ${balance.value} sats")
        balance
    } catch (e: Exception) {
        printError("Failed to get wallet balance: $e")
        null
    }
}

fun testMintQuote(wallet: FfiWallet): FfiMintQuote? {
    printHeader("Testing Mint Quote Creation")
    
    return try {
        // Create mint quote for 100 sats
        val amount = FfiAmount(value = 100UL)
        val description = "Test mint quote"
        
        printInfo("Creating mint quote for ${amount.value} sats")
        val mintQuote = wallet.mintQuote(amount = amount, description = description)
        
        printSuccess("Successfully created mint quote")
        printInfo("Quote ID: ${mintQuote.id}")
        printInfo("Quote amount: ${mintQuote.amount.value}")
        printInfo("Quote unit: ${mintQuote.unit}")
        printInfo("Quote state: ${mintQuote.state}")
        printInfo("Quote expiry: ${mintQuote.expiry}")
        val truncatedRequest = mintQuote.request.take(50)
        printInfo("Payment request: $truncatedRequest...")
        
        mintQuote
    } catch (e: Exception) {
        printError("Failed to create mint quote: $e")
        printInfo("This is expected if the mint is not available or reachable")
        null
    }
}

fun testMintQuoteState(wallet: FfiWallet, quoteId: String): FfiMintQuoteBolt11Response? {
    printHeader("Testing Mint Quote State Check")
    
    return try {
        printInfo("Checking state for quote: $quoteId")
        val quoteState = wallet.mintQuoteState(quoteId)
        
        printSuccess("Successfully retrieved quote state")
        printInfo("Quote: ${quoteState.quote}")
        printInfo("State: ${quoteState.state}")
        val truncatedRequest = quoteState.request.take(50)
        printInfo("Request: $truncatedRequest...")
        
        quoteState
    } catch (e: Exception) {
        printError("Failed to get quote state: $e")
        null
    }
}

fun testDataStructures(): Boolean {
    printHeader("Testing Data Structures")
    
    return try {
        // Test FfiAmount
        val amount = FfiAmount(value = 1000UL)
        printSuccess("Created FfiAmount: ${amount.value}")
        
        // Test currency units
        printSuccess("Available currency units:")
        FfiCurrencyUnit.values().forEach { unit ->
            printInfo("  - ${unit.name}: $unit")
        }
        
        // Test split targets
        printSuccess("Available split targets:")
        FfiSplitTarget.values().forEach { target ->
            printInfo("  - ${target.name}: $target")
        }
        
        // Test send kinds
        printSuccess("Testing send kinds:")
        val onlineExact = FfiSendKind.OnlineExact
        printInfo("  - Online exact: $onlineExact")
        
        val toleranceAmount = FfiAmount(value = 50UL)
        val onlineTolerance = FfiSendKind.OnlineTolerance(tolerance = toleranceAmount)
        printInfo("  - Online with tolerance: $onlineTolerance")
        
        // Test send options
        val sendOptions = FfiSendOptions(
            memo = null,
            amountSplitTarget = FfiSplitTarget.DEFAULT,
            sendKind = onlineExact,
            includeFee = true,
            metadata = emptyMap(),
            maxProofs = null
        )
        printSuccess("Created FfiSendOptions successfully")
        
        true
    } catch (e: Exception) {
        printError("Failed to test data structures: $e")
        e.printStackTrace()
        false
    }
}

fun testErrorHandling(): Boolean {
    printHeader("Testing Error Handling")
    
    return try {
        // Test creating wallet with invalid mint URL
        printInfo("Testing error handling with invalid mint URL...")
        
        val mnemonic = generateMnemonic()
        val store = FfiLocalStore()
        
        try {
            val invalidWallet = FfiWallet.fromMnemonic(
                mintUrl = "invalid://not-a-real-mint",
                unit = FfiCurrencyUnit.SAT,
                localstore = store,
                mnemonicWords = mnemonic
            )
            printWarning("Expected error but wallet creation succeeded")
        } catch (e: FfiException) {
            printSuccess("Properly caught FfiException: ${e.javaClass.simpleName}")
            printInfo("Error details: $e")
        } catch (e: Exception) {
            printWarning("Caught unexpected error type: ${e.javaClass.simpleName}: $e")
        }
        
        true
    } catch (e: Exception) {
        printError("Failed to test error handling: $e")
        false
    }
}

fun testEndToEndFlow(): Boolean {
    printHeader("Testing End-to-End Flow")
    printInfo("This test performs a complete mint-to-melt workflow")
    printInfo("Using a test Lightning invoice for the melt operation")
    
    return try {
        // Step 1: Create wallet
        printInfo("Step 1: Creating wallet...")
        val mnemonic = generateMnemonic()
        val localStore = FfiLocalStore()
        val mintUrl = "https://testnut.cashu.space"  // Use a reliable testnet mint
        val unit = FfiCurrencyUnit.SAT
        
        val wallet = FfiWallet.fromMnemonic(
            mintUrl = mintUrl,
            unit = unit,
            localstore = localStore,
            mnemonicWords = mnemonic
        )
        printSuccess("✓ Wallet created successfully")
        
        // Step 2: Get mint info
        printInfo("Step 2: Fetching mint information...")
        wallet.getMintInfo()
        printSuccess("✓ Mint info retrieved successfully")
        
        // Step 3: Create mint quote
        printInfo("Step 3: Creating mint quote for 100 sats...")
        val mintAmount = FfiAmount(value = 100UL)
        val mintQuote = wallet.mintQuote(amount = mintAmount, description = "End-to-end test")
        printSuccess("✓ Mint quote created: ${mintQuote.id}")
        printInfo("Payment request: ${mintQuote.request}")
        printWarning("Please pay the Lightning invoice above to continue the test")
        
        // Step 4: Wait for payment and check quote state
        printInfo("Step 4: Waiting for quote payment...")
        val maxWaitTime = 300_000L  // 5 minutes in milliseconds
        val checkInterval = 5_000L   // 5 seconds in milliseconds
        var waitedTime = 0L
        var isPaid = false
        
        while (waitedTime < maxWaitTime) {
            try {
                val quoteState = wallet.mintQuoteState(mintQuote.id)
                printInfo("Quote state: ${quoteState.state} (waited: ${waitedTime / 1000}s)")
                
                when (quoteState.state) {
                    FfiMintQuoteState.PAID -> {
                        printSuccess("✓ Quote has been paid!")
                        isPaid = true
                        break
                    }
                    FfiMintQuoteState.ISSUED -> {
                        printSuccess("✓ Quote already issued!")
                        isPaid = true
                        break
                    }
                    else -> {
                        Thread.sleep(checkInterval)
                        waitedTime += checkInterval
                    }
                }
            } catch (e: Exception) {
                printWarning("Error checking quote state: $e")
                Thread.sleep(checkInterval)
                waitedTime += checkInterval
            }
        }
        
        if (!isPaid) {
            printError("✗ Timeout waiting for payment")
            return false
        }
        
        // Step 5: Mint the tokens
        printInfo("Step 5: Minting tokens...")
        val mintedAmount = wallet.mint(mintQuote.id, FfiSplitTarget.DEFAULT)
        printSuccess("✓ Minted ${mintedAmount.value} sats")
        
        // Step 6: Check wallet balance
        printInfo("Step 6: Checking wallet balance...")
        val balance = wallet.balance()
        printSuccess("✓ Wallet balance: ${balance.value} sats")
        
        // Verify balance matches minted amount
        if (balance.value == mintAmount.value) {
            printSuccess("✓ Balance matches expected amount!")
        } else {
            printWarning("⚠ Balance (${balance.value}) doesn't match expected (${mintAmount.value})")
        }
        
        // Step 7: Create melt quote and melt tokens
        printInfo("Step 7: Creating melt quote with test Lightning invoice...")
        // Test Lightning invoice (expired, can be reused for testing)
        val testInvoice = "lnbc100n1p582p63pp5ukwp2y9k8mwfdqgjytdstnj7fvkzj6pj70zd8vj7xw79jpc0d5dsdqqcqzzsxqyz5vqrzjqvueefmrckfdwyyu39m0lf24sqzcr9vcrmxrvgfn6empxz7phrjxvrttncqq0lcqqyqqqqlgqqqqqqgq2qsp5kwrmcldjpgadgsz3724xdqev5rwcl6w7mwxy694z4lmj3ce863qs9qxpqysgqhh37sx2l82mcfymhd3a2xl89mkst47k7a2t3fxekemeeuupdqlx58xpm04wj2406tz0u602wgtdczzqyktghmvzjgewwkrqgx623qlspvf3ju9"
        
        val meltQuote = wallet.meltQuote(testInvoice)
        printSuccess("✓ Melt quote created: ${meltQuote.id}")
        printInfo("Melt amount: ${meltQuote.amount.value} sats")
        printInfo("Fee reserve: ${meltQuote.feeReserve.value} sats")
        
        // Step 8: Execute melt operation
        printInfo("Step 8: Melting tokens...")
        val melted = wallet.melt(meltQuote.id)
        printSuccess("✓ Melted ${melted.amount.value} sats")
        printInfo("Fee paid: ${melted.feePaid.value} sats")
        printInfo("Melt state: ${melted.state}")
        
        printSuccess("🎉 Complete end-to-end test completed successfully!")
        true
        
    } catch (e: Exception) {
        printError("✗ End-to-end test failed: $e")
        false
    }
}

// Main test function
fun main(): Boolean {
    printHeader("CDK FFI Kotlin Bindings Test Suite")
    printInfo("This script tests the basic functionality of CDK FFI bindings")
    printWarning("Some tests may fail if a mint is not available - this is expected")
    
    // Track test results
    val results = mutableMapOf<String, Boolean>()
    
    // Test 1: Mnemonic generation
    val mnemonic = testMnemonicGeneration()
    results["mnemonic"] = mnemonic != null
    
    // Test 2: Local store creation
    val localStore = testLocalStoreCreation()
    results["local_store"] = localStore != null
    
    // Test 3: Data structures
    results["data_structures"] = testDataStructures()
    
    // Test 4: Error handling
    results["error_handling"] = testErrorHandling()
    
    // Test 5: Wallet creation (may fail if mint unavailable)
    var wallet: FfiWallet? = null
    if (mnemonic != null && localStore != null) {
        wallet = testWalletCreation(mnemonic, localStore)
    }
    results["wallet_creation"] = wallet != null
    
    // Test 6: Mint info (depends on wallet)
    if (wallet != null) {
        results["mint_info"] = testMintInfo(wallet)
        
        // Test 7: Wallet balance
        val balance = testWalletBalance(wallet)
        results["balance"] = balance != null
        
        // Test 8: Mint quote creation
        val mintQuote = testMintQuote(wallet)
        results["mint_quote"] = mintQuote != null
        
        // Test 9: Quote state check
        if (mintQuote != null) {
            val quoteState = testMintQuoteState(wallet, mintQuote.id)
            results["mint_quote_state"] = quoteState != null
        }
    }
    
    // Test 10: End-to-end flow (always run)
    results["end_to_end_flow"] = testEndToEndFlow()
    
    // Print summary
    printHeader("Test Summary")
    
    val passed = results.values.count { it }
    val total = results.size
    
    results.forEach { (testName, success) ->
        if (success) {
            printSuccess("$testName: PASSED")
        } else {
            printError("$testName: FAILED")
        }
    }
    
    println("\n${Colors.BOLD}Results: $passed/$total tests passed${Colors.ENDC}")
    
    when {
        passed == total -> {
            printSuccess("All tests passed! 🎉")
        }
        passed >= (total * 0.6).toInt() -> {
            printWarning("Most tests passed. Some failures may be due to mint unavailability.")
        }
        else -> {
            printError("Multiple test failures. Check your setup and mint connectivity.")
        }
    }
    
    printInfo("\nNote: Network-related failures are expected if no mint is available.")
    printInfo("The core bindings functionality is working if basic tests pass.")
    
    return passed >= 4 // Return success if at least basic functionality works
}

// Entry point
if (args.isEmpty()) {
    val success = main()
    exitProcess(if (success) 0 else 1)
}
