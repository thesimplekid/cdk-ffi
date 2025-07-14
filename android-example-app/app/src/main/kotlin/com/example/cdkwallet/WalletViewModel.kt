package com.example.cdkwallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.cdk_ffi.*

sealed class WalletState {
    object Uninitialized : WalletState()
    object Loading : WalletState()
    data class Initialized(
        val balance: ULong,
        val mintUrl: String,
        val mnemonic: String
    ) : WalletState()
    data class Error(val error: String) : WalletState()
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _walletState = MutableLiveData<WalletState>(WalletState.Uninitialized)
    val walletState: LiveData<WalletState> = _walletState
    
    private var wallet: FfiWallet? = null
    private var localStore: FfiLocalStore? = null
    private val prefs: SharedPreferences = application.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    
    // Default testnet mint - replace with your preferred mint
    private val defaultMintUrl = "https://testnut.cashu.space"
    private val defaultCurrency = FfiCurrencyUnit.SAT
    
    companion object {
        private const val KEY_MNEMONIC = "mnemonic"
        private const val KEY_MINT_URL = "mint_url"
        private const val KEY_INITIALIZED = "initialized"
    }
    
    fun isWalletInitialized(): Boolean {
        return prefs.getBoolean(KEY_INITIALIZED, false) && wallet != null
    }
    
    suspend fun initializeWallet() {
        _walletState.value = WalletState.Loading
        
        withContext(Dispatchers.IO) {
            try {
                // Check if we have an existing mnemonic
                val existingMnemonic = prefs.getString(KEY_MNEMONIC, null)
                val mintUrl = prefs.getString(KEY_MINT_URL, defaultMintUrl) ?: defaultMintUrl
                
                val mnemonic = if (existingMnemonic != null) {
                    existingMnemonic
                } else {
                    // Generate new mnemonic
                    generateMnemonic().also { newMnemonic ->
                        prefs.edit()
                            .putString(KEY_MNEMONIC, newMnemonic)
                            .putString(KEY_MINT_URL, mintUrl)
                            .apply()
                    }
                }
                
                // Create local store
                val dbPath = "${getApplication<Application>().filesDir}/wallet.db"
                localStore = FfiLocalStore.newWithPath(dbPath)
                
                // Create or restore wallet
                wallet = if (existingMnemonic != null) {
                    FfiWallet.restoreFromMnemonic(
                        mintUrl = mintUrl,
                        unit = defaultCurrency,
                        localstore = localStore!!,
                        mnemonicWords = mnemonic
                    )
                } else {
                    FfiWallet.fromMnemonic(
                        mintUrl = mintUrl,
                        unit = defaultCurrency,
                        localstore = localStore!!,
                        mnemonicWords = mnemonic
                    )
                }
                
                // Initialize mint info
                try {
                    wallet?.getMintInfo()
                } catch (e: Exception) {
                    // Mint might be unavailable, but wallet is still functional
                    kotlinx.coroutines.delay(100) // Small delay for stability
                }
                
                // Get initial balance
                val balance = wallet?.balance()?.value ?: 0UL
                
                // Mark as initialized
                prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
                
                _walletState.postValue(
                    WalletState.Initialized(
                        balance = balance,
                        mintUrl = mintUrl,
                        mnemonic = mnemonic
                    )
                )
                
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to initialize wallet: ${e.message}"))
            }
        }
    }
    
    suspend fun refreshBalance() {
        if (wallet == null) {
            _walletState.value = WalletState.Error("Wallet not initialized")
            return
        }
        
        withContext(Dispatchers.IO) {
            try {
                val balance = wallet?.balance()?.value ?: 0UL
                val currentState = _walletState.value
                if (currentState is WalletState.Initialized) {
                    _walletState.postValue(currentState.copy(balance = balance))
                }
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to refresh balance: ${e.message}"))
            }
        }
    }
    
    suspend fun createMintQuote(amount: ULong, description: String): FfiMintQuote? {
        return withContext(Dispatchers.IO) {
            try {
                wallet?.mintQuote(
                    amount = FfiAmount(amount),
                    description = description
                )
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to create mint quote: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun checkMintQuoteState(quoteId: String): FfiMintQuoteBolt11Response? {
        return withContext(Dispatchers.IO) {
            try {
                wallet?.mintQuoteState(quoteId)
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to check quote state: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun mintTokens(quoteId: String): FfiAmount? {
        return withContext(Dispatchers.IO) {
            try {
                wallet?.mint(quoteId, FfiSplitTarget.DEFAULT)
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to mint tokens: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun createMeltQuote(invoice: String): FfiMeltQuote? {
        return withContext(Dispatchers.IO) {
            try {
                wallet?.meltQuote(invoice)
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to create melt quote: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun payInvoice(quoteId: String): FfiMelted? {
        return withContext(Dispatchers.IO) {
            try {
                wallet?.melt(quoteId)
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to pay invoice: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun sendTokens(amount: ULong, memo: String?): FfiToken? {
        return withContext(Dispatchers.IO) {
            try {
                val sendMemo = memo?.let { 
                    FfiSendMemo(memo = it, includeMemo = true) 
                }
                
                val sendOptions = FfiSendOptions(
                    memo = sendMemo,
                    amountSplitTarget = FfiSplitTarget.DEFAULT,
                    sendKind = FfiSendKind.OnlineExact,
                    includeFee = true,
                    metadata = emptyMap(),
                    maxProofs = null
                )
                
                wallet?.send(
                    amount = FfiAmount(amount),
                    options = sendOptions,
                    memo = sendMemo
                )
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to send tokens: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun prepareSend(amount: ULong): FfiPreparedSend? {
        return withContext(Dispatchers.IO) {
            try {
                val sendOptions = FfiSendOptions(
                    memo = null,
                    amountSplitTarget = FfiSplitTarget.DEFAULT,
                    sendKind = FfiSendKind.OnlineExact,
                    includeFee = true,
                    metadata = emptyMap(),
                    maxProofs = null
                )
                
                wallet?.prepareSend(
                    amount = FfiAmount(amount),
                    options = sendOptions
                )
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to prepare send: ${e.message}"))
                null
            }
        }
    }
    
    suspend fun resetWallet() {
        _walletState.value = WalletState.Loading
        
        withContext(Dispatchers.IO) {
            try {
                // Clean up current wallet
                wallet?.destroy()
                localStore?.destroy()
                wallet = null
                localStore = null
                
                // Clear preferences
                prefs.edit().clear().apply()
                
                // Delete database file
                val dbFile = java.io.File("${getApplication<Application>().filesDir}/wallet.db")
                if (dbFile.exists()) {
                    dbFile.delete()
                }
                
                _walletState.postValue(WalletState.Uninitialized)
                
            } catch (e: Exception) {
                _walletState.postValue(WalletState.Error("Failed to reset wallet: ${e.message}"))
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        wallet?.destroy()
        localStore?.destroy()
    }
    
    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalletViewModel(application) as T
        }
    }
    
    companion object {
        fun createFactory(application: Application): Factory {
            return Factory(application)
        }
    }
}
