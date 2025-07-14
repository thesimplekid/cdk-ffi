package com.example

import uniffi.cdk_ffi.*

fun main() {
    println("🔧 CDK FFI Kotlin Example")
    
    try {
        // Test 1: Generate a mnemonic
        println("\n1️⃣ Generating mnemonic...")
        val mnemonic = generateMnemonic()
        println("✅ Generated mnemonic: $mnemonic")
        
        // Test 2: Create a local store
        println("\n2️⃣ Creating local store...")
        val localStore = FfiLocalStore()
        println("✅ Local store created")
        
        // Test 3: Create wallet
        println("\n3️⃣ Creating wallet...")
        val mintUrl = "https://testnut.cashu.space"
        val unit = FfiCurrencyUnit.SAT
        
        val wallet = FfiWallet.fromMnemonic(
            mintUrl = mintUrl,
            unit = unit,
            localstore = localStore,
            mnemonicWords = mnemonic
        )
        println("✅ Wallet created successfully")
        println("   Mint URL: ${wallet.mintUrl()}")
        println("   Unit: ${wallet.unit()}")
        
        // Test 4: Get mint info
        println("\n4️⃣ Getting mint info...")
        val mintInfo = wallet.getMintInfo()
        println("✅ Mint info retrieved (${mintInfo.length} characters)")
        
        // Test 5: Check wallet balance
        println("\n5️⃣ Checking wallet balance...")
        val balance = wallet.balance()
        println("✅ Wallet balance: ${balance.value} sats")
        
        // Test 6: Create a mint quote
        println("\n6️⃣ Creating mint quote...")
        val amount = FfiAmount(value = 100UL)
        val mintQuote = wallet.mintQuote(amount = amount, description = "Kotlin example")
        println("✅ Mint quote created:")
        println("   Quote ID: ${mintQuote.id}")
        println("   Amount: ${mintQuote.amount.value} sats")
        println("   State: ${mintQuote.state}")
        println("   Payment request: ${mintQuote.request.take(50)}...")
        
        println("\n🎉 All basic operations completed successfully!")
        
    } catch (e: FfiException) {
        println("❌ CDK Error: ${e.message}")
        e.printStackTrace()
    } catch (e: Exception) {
        println("❌ Unexpected error: ${e.message}")
        e.printStackTrace()
    }
}
