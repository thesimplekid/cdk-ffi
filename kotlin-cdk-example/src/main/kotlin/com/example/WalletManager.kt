package com.example

import uniffi.cdk_ffi.*

class CdkWalletManager(private val mintUrl: String) {
    private var wallet: FfiWallet? = null
    private var localStore: FfiLocalStore? = null
    
    fun createWallet(mnemonic: String? = null): Result<String> {
        return try {
            // Create local store
            localStore = FfiLocalStore()
            
            // Generate or use provided mnemonic
            val mnemonicToUse = mnemonic ?: generateMnemonic()
            
            // Create wallet
            wallet = FfiWallet.fromMnemonic(
                mintUrl = mintUrl,
                unit = FfiCurrencyUnit.SAT,
                localstore = localStore!!,
                mnemonicWords = mnemonicToUse
            )
            
            Result.success("Wallet created successfully with mint: $mintUrl")
        } catch (e: FfiException) {
            Result.failure(Exception("CDK Error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create wallet: ${e.message}", e))
        }
    }
    
    fun getBalance(): Result<ULong> {
        return try {
            val balance = wallet?.balance()?.value 
                ?: return Result.failure(Exception("Wallet not initialized"))
            Result.success(balance)
        } catch (e: FfiException) {
            Result.failure(Exception("CDK Error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get balance: ${e.message}", e))
        }
    }
    
    fun createMintQuote(amountSats: ULong, description: String = "Kotlin CDK"): Result<FfiMintQuote> {
        return try {
            val amount = FfiAmount(value = amountSats)
            val quote = wallet?.mintQuote(amount = amount, description = description)
                ?: return Result.failure(Exception("Wallet not initialized"))
            Result.success(quote)
        } catch (e: FfiException) {
            Result.failure(Exception("CDK Error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create mint quote: ${e.message}", e))
        }
    }
    
    fun mintTokens(quoteId: String): Result<ULong> {
        return try {
            val mintedAmount = wallet?.mint(quoteId, FfiSplitTarget.DEFAULT)?.value
                ?: return Result.failure(Exception("Wallet not initialized"))
            Result.success(mintedAmount)
        } catch (e: FfiException) {
            Result.failure(Exception("CDK Error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to mint tokens: ${e.message}", e))
        }
    }
    
    fun createMeltQuote(invoice: String): Result<FfiMeltQuote> {
        return try {
            val quote = wallet?.meltQuote(invoice)
                ?: return Result.failure(Exception("Wallet not initialized"))
            Result.success(quote)
        } catch (e: FfiException) {
            Result.failure(Exception("CDK Error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create melt quote: ${e.message}", e))
        }
    }
}

fun demonstrateWallet() {
    println("🚀 CDK Wallet Manager Demo")
    
    val walletManager = CdkWalletManager("https://testnut.cashu.space")
    
    // Create wallet
    walletManager.createWallet().fold(
        onSuccess = { message -> 
            println("✅ $message")
        },
        onFailure = { error -> 
            println("❌ Failed to create wallet: ${error.message}")
            return
        }
    )
    
    // Check balance
    walletManager.getBalance().fold(
        onSuccess = { balance -> 
            println("💰 Current balance: $balance sats")
        },
        onFailure = { error -> 
            println("⚠️ Could not get balance: ${error.message}")
        }
    )
    
    // Create mint quote
    walletManager.createMintQuote(100UL).fold(
        onSuccess = { quote -> 
            println("📋 Mint quote created:")
            println("   ID: ${quote.id}")
            println("   Amount: ${quote.amount.value} sats")
            println("   Payment Request: ${quote.request.take(50)}...")
        },
        onFailure = { error -> 
            println("❌ Failed to create mint quote: ${error.message}")
        }
    )
    
    println("\n✅ Demo completed!")
}

// You can also create a simple test
fun runTests() {
    println("🧪 Running CDK Tests")
    
    try {
        // Test data structures
        println("Testing data structures...")
        val amount = FfiAmount(value = 1000UL)
        println("✅ FfiAmount created: ${amount.value}")
        
        val sendKind = FfiSendKind.OnlineExact
        println("✅ FfiSendKind created: $sendKind")
        
        println("✅ All tests passed!")
        
    } catch (e: Exception) {
        println("❌ Test failed: ${e.message}")
    }
}
