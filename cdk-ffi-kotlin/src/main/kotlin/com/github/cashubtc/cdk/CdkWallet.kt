/**
 * CDK FFI Kotlin Library
 * 
 * This package provides Kotlin bindings for the Cashu Development Kit (CDK).
 * It allows you to create and manage Cashu wallets, mint tokens, send/receive
 * payments, and interact with Cashu mints from Kotlin/JVM applications.
 */
@file:JvmName("CDKWallet")

package com.github.cashubtc.cdk

// Re-export the main types from the FFI bindings for convenience
// Note: The actual FFI bindings will be copied from uniffi.cdk_ffi package

/**
 * Main CDK Wallet class for Cashu operations
 */
public typealias CdkWallet = uniffi.cdk_ffi.FfiWallet

/**
 * Local storage for CDK wallet data
 */
public typealias CdkLocalStore = uniffi.cdk_ffi.FfiLocalStore

/**
 * Amount type for CDK operations
 */
public typealias CdkAmount = uniffi.cdk_ffi.FfiAmount

/**
 * Token representation
 */
public typealias CdkToken = uniffi.cdk_ffi.FfiToken

/**
 * Mint quote for receiving tokens
 */
public typealias CdkMintQuote = uniffi.cdk_ffi.FfiMintQuote

/**
 * Melt quote for paying Lightning invoices
 */
public typealias CdkMeltQuote = uniffi.cdk_ffi.FfiMeltQuote

/**
 * Send options configuration
 */
public typealias CdkSendOptions = uniffi.cdk_ffi.FfiSendOptions

/**
 * Currency unit enum
 */
public typealias CdkCurrencyUnit = uniffi.cdk_ffi.FfiCurrencyUnit

/**
 * Split target enum
 */
public typealias CdkSplitTarget = uniffi.cdk_ffi.FfiSplitTarget

/**
 * Send kind configuration
 */
public typealias CdkSendKind = uniffi.cdk_ffi.FfiSendKind

/**
 * CDK exception types
 */
public typealias CdkException = uniffi.cdk_ffi.FfiException

/**
 * Generate a 12-word mnemonic phrase for wallet creation
 * 
 * @return A 12-word mnemonic phrase as a string
 * @throws CdkException if mnemonic generation fails
 */
public fun generateMnemonic(): String = uniffi.cdk_ffi.generateMnemonic()

/**
 * Create an amount object from a ULong value
 * 
 * @param value The amount value
 * @return CdkAmount instance
 */
public fun createAmount(value: ULong): CdkAmount = CdkAmount(value)

/**
 * Create an amount object from a Long value
 * 
 * @param value The amount value  
 * @return CdkAmount instance
 */
public fun createAmount(value: Long): CdkAmount = CdkAmount(value.toULong())

/**
 * Create send options with default values
 * 
 * @param splitTarget Split target strategy (default: DEFAULT)
 * @param sendKind Send kind configuration (default: OnlineExact)
 * @param includeFee Whether to include fees (default: true)
 * @param metadata Additional metadata (default: empty map)
 * @param maxProofs Maximum number of proofs (default: null)
 * @return CdkSendOptions instance
 */
public fun createSendOptions(
    splitTarget: CdkSplitTarget = CdkSplitTarget.DEFAULT,
    sendKind: CdkSendKind = CdkSendKind.OnlineExact,
    includeFee: Boolean = true,
    metadata: Map<String, String> = emptyMap(),
    maxProofs: ULong? = null
): CdkSendOptions = CdkSendOptions(
    memo = null,
    amountSplitTarget = splitTarget,
    sendKind = sendKind,
    includeFee = includeFee,
    metadata = metadata,
    maxProofs = maxProofs
)

/**
 * Extension property to get the amount value as Long
 */
public val CdkAmount.longValue: Long get() = this.value.toLong()

/**
 * Extension property to get the amount value as ULong
 */
public val CdkAmount.ulongValue: ULong get() = this.value

/**
 * Extension function to check if amount is zero
 */
public fun CdkAmount.isZero(): Boolean = this.value == 0UL

/**
 * Extension function to check if amount is positive
 */
public fun CdkAmount.isPositive(): Boolean = this.value > 0UL

/**
 * Simple wallet builder for easier initialization
 */
public class WalletBuilder {
    private var mintUrl: String? = null
    private var unit: CdkCurrencyUnit = CdkCurrencyUnit.SAT
    private var dbPath: String? = null
    
    /**
     * Set the mint URL
     */
    public fun mintUrl(url: String): WalletBuilder = apply { mintUrl = url }
    
    /**
     * Set the currency unit (default: SAT)
     */
    public fun unit(unit: CdkCurrencyUnit): WalletBuilder = apply { this.unit = unit }
    
    /**
     * Set the database path (optional)
     */
    public fun dbPath(path: String): WalletBuilder = apply { dbPath = path }
    
    /**
     * Create a new wallet from a mnemonic
     */
    public fun fromMnemonic(mnemonic: String): CdkWallet {
        val mintUrlValue = mintUrl ?: throw IllegalArgumentException("Mint URL must be set")
        val store = if (dbPath != null) {
            CdkLocalStore.newWithPath(dbPath)
        } else {
            CdkLocalStore()
        }
        return CdkWallet.fromMnemonic(mintUrlValue, unit, store, mnemonic)
    }
    
    /**
     * Restore an existing wallet from a mnemonic
     */
    public fun restoreFromMnemonic(mnemonic: String): CdkWallet {
        val mintUrlValue = mintUrl ?: throw IllegalArgumentException("Mint URL must be set")
        val store = if (dbPath != null) {
            CdkLocalStore.newWithPath(dbPath)
        } else {
            CdkLocalStore()
        }
        return CdkWallet.restoreFromMnemonic(mintUrlValue, unit, store, mnemonic)
    }
}

/**
 * Create a wallet builder for easier wallet initialization
 */
public fun walletBuilder(): WalletBuilder = WalletBuilder()
