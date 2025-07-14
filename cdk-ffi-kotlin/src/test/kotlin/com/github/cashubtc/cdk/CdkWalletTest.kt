package com.github.cashubtc.cdk

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class CdkWalletTest {

    @Test
    fun testMnemonicGeneration() {
        val mnemonic = generateMnemonic()
        assertNotNull(mnemonic)
        
        // Should be 12 words
        val words = mnemonic.trim().split("\\s+".toRegex())
        assertEquals(12, words.size, "Mnemonic should contain 12 words")
        
        // Each word should not be empty
        assertTrue(words.all { it.isNotBlank() }, "All words should be non-empty")
    }

    @Test
    fun testAmountCreation() {
        val amount1 = createAmount(1000UL)
        assertEquals(1000UL, amount1.value)
        assertEquals(1000L, amount1.longValue)
        assertTrue(amount1.isPositive())
        
        val amount2 = createAmount(0UL)
        assertTrue(amount2.isZero())
        assertTrue(!amount2.isPositive())
    }

    @Test
    fun testSendOptionsCreation() {
        val sendOptions = createSendOptions(
            splitTarget = CdkSplitTarget.DEFAULT,
            sendKind = CdkSendKind.OnlineExact,
            includeFee = true,
            metadata = mapOf("test" to "value"),
            maxProofs = 100UL
        )
        
        assertEquals(CdkSplitTarget.DEFAULT, sendOptions.amountSplitTarget)
        assertEquals(CdkSendKind.OnlineExact, sendOptions.sendKind)
        assertTrue(sendOptions.includeFee)
        assertEquals("value", sendOptions.metadata["test"])
        assertEquals(100UL, sendOptions.maxProofs)
    }

    @Test
    fun testWalletBuilder() {
        val builder = walletBuilder()
            .mintUrl("https://mint.example.com")
            .unit(CdkCurrencyUnit.SAT)
            .dbPath("/tmp/test.db")
        
        assertNotNull(builder)
        
        // We can't actually create the wallet without a real mint and proper setup,
        // but we can verify the builder works
    }
}
