package com.github.cashubtc.cdk

/**
 * Simple verification script to ensure the package is working correctly.
 */
fun main() {
    try {
        println("CDK FFI Kotlin Package Verification")
        println("===================================")
        
        // Test mnemonic generation
        println("Testing mnemonic generation...")
        val mnemonic = generateMnemonic()
        println("✅ Generated mnemonic: ${mnemonic.split(" ").take(3).joinToString(" ")}...")
        
        // Test amount creation
        println("\nTesting amount creation...")
        val amount = createAmount(1000UL)
        println("✅ Created amount: ${amount.value}")
        println("✅ Amount as long: ${amount.longValue}")
        println("✅ Amount is positive: ${amount.isPositive()}")
        
        // Test send options creation
        println("\nTesting send options creation...")
        val sendOptions = createSendOptions(
            splitTarget = CdkSplitTarget.DEFAULT,
            sendKind = CdkSendKind.OnlineExact,
            includeFee = true
        )
        println("✅ Created send options with split target: ${sendOptions.amountSplitTarget}")
        
        // Test wallet builder
        println("\nTesting wallet builder...")
        val builder = walletBuilder()
            .mintUrl("https://mint.example.com")
            .unit(CdkCurrencyUnit.SAT)
        println("✅ Created wallet builder")
        
        println("\n🎉 Package verification completed successfully!")
        println("The CDK FFI Kotlin package is properly configured and ready to use.")
        
    } catch (e: Exception) {
        println("❌ Package verification failed: ${e.message}")
        e.printStackTrace()
    }
}
