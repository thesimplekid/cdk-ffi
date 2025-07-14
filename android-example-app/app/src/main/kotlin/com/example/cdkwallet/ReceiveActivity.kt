package com.example.cdkwallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cdkwallet.databinding.ActivityReceiveBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uniffi.cdk_ffi.FfiMintQuoteState

class ReceiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveBinding
    private val viewModel: WalletViewModel by viewModels { 
        WalletViewModel.createFactory(application) 
    }
    
    private var currentQuoteId: String? = null
    private var isPollingQuote = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeWalletState()
    }
    
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            
            btnCreateQuote.setOnClickListener {
                val amountText = edtAmount.text.toString().trim()
                val description = edtDescription.text.toString().trim().takeIf { it.isNotBlank() } ?: "CDK Wallet Payment"
                
                if (amountText.isNotBlank()) {
                    val amount = amountText.toULongOrNull()
                    if (amount != null && amount > 0UL) {
                        createMintQuote(amount, description)
                    } else {
                        showToast("Please enter a valid amount")
                    }
                } else {
                    showToast("Please enter an amount")
                }
            }
            
            btnCheckQuote.setOnClickListener {
                currentQuoteId?.let { quoteId ->
                    checkQuoteStatus(quoteId)
                } ?: showToast("No active quote")
            }
            
            btnMintTokens.setOnClickListener {
                currentQuoteId?.let { quoteId ->
                    mintTokens(quoteId)
                } ?: showToast("No active quote")
            }
        }
    }
    
    private fun observeWalletState() {
        viewModel.walletState.observe(this) { state ->
            when (state) {
                is WalletState.Initialized -> {
                    binding.txtBalance.text = "Balance: ${state.balance} sats"
                }
                is WalletState.Error -> {
                    showToast("Wallet error: ${state.error}")
                }
                else -> {
                    binding.txtBalance.text = "Balance: Unknown"
                }
            }
        }
    }
    
    private fun createMintQuote(amount: ULong, description: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateQuote.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val quote = viewModel.createMintQuote(amount, description)
                if (quote != null) {
                    currentQuoteId = quote.id
                    
                    binding.apply {
                        txtQuoteInfo.text = """
                            Quote ID: ${quote.id}
                            Amount: ${quote.amount.value} sats
                            State: ${quote.state}
                            Expires: ${quote.expiry}
                        """.trimIndent()
                        txtQuoteInfo.visibility = View.VISIBLE
                        
                        btnCheckQuote.visibility = View.VISIBLE
                        btnMintTokens.visibility = View.VISIBLE
                    }
                    
                    // Show QR code for the Lightning invoice
                    startActivity(Intent(this@ReceiveActivity, QRCodeActivity::class.java).apply {
                        putExtra("mode", "show")
                        putExtra("data", quote.request)
                        putExtra("title", "Lightning Invoice")
                        putExtra("subtitle", "${amount} sats")
                    })
                    
                    // Start polling for payment
                    startQuotePolling(quote.id)
                    
                } else {
                    showToast("Failed to create mint quote")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnCreateQuote.isEnabled = true
            }
        }
    }
    
    private fun checkQuoteStatus(quoteId: String) {
        lifecycleScope.launch {
            try {
                val quoteResponse = viewModel.checkMintQuoteState(quoteId)
                if (quoteResponse != null) {
                    val statusText = """
                        Quote: ${quoteResponse.quote}
                        State: ${quoteResponse.state}
                        ${if (quoteResponse.expiry != null) "Expires: ${quoteResponse.expiry}" else ""}
                    """.trimIndent()
                    
                    binding.txtQuoteStatus.text = statusText
                    binding.txtQuoteStatus.visibility = View.VISIBLE
                    
                    when (quoteResponse.state) {
                        FfiMintQuoteState.PAID -> {
                            showToast("Quote has been paid! You can now mint tokens.")
                            binding.btnMintTokens.isEnabled = true
                        }
                        FfiMintQuoteState.ISSUED -> {
                            showToast("Tokens already minted for this quote.")
                            binding.btnMintTokens.isEnabled = false
                        }
                        FfiMintQuoteState.UNPAID -> {
                            showToast("Quote is still unpaid. Please pay the Lightning invoice.")
                            binding.btnMintTokens.isEnabled = false
                        }
                    }
                } else {
                    showToast("Failed to check quote status")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }
    
    private fun startQuotePolling(quoteId: String) {
        if (isPollingQuote) return
        
        isPollingQuote = true
        lifecycleScope.launch {
            try {
                while (isPollingQuote && currentQuoteId == quoteId) {
                    val quoteResponse = viewModel.checkMintQuoteState(quoteId)
                    if (quoteResponse != null) {
                        when (quoteResponse.state) {
                            FfiMintQuoteState.PAID -> {
                                isPollingQuote = false
                                binding.txtQuoteStatus.text = "✅ Quote paid! Ready to mint tokens."
                                binding.txtQuoteStatus.visibility = View.VISIBLE
                                binding.btnMintTokens.isEnabled = true
                                showToast("Payment received! You can now mint tokens.")
                                return@launch
                            }
                            FfiMintQuoteState.ISSUED -> {
                                isPollingQuote = false
                                binding.txtQuoteStatus.text = "✅ Tokens already minted."
                                binding.txtQuoteStatus.visibility = View.VISIBLE
                                binding.btnMintTokens.isEnabled = false
                                return@launch
                            }
                            FfiMintQuoteState.UNPAID -> {
                                // Continue polling
                                binding.txtQuoteStatus.text = "⏳ Waiting for payment..."
                                binding.txtQuoteStatus.visibility = View.VISIBLE
                            }
                        }
                    }
                    
                    delay(3000) // Poll every 3 seconds
                }
            } catch (e: Exception) {
                isPollingQuote = false
                binding.txtQuoteStatus.text = "Error polling quote: ${e.message}"
                binding.txtQuoteStatus.visibility = View.VISIBLE
            }
        }
    }
    
    private fun mintTokens(quoteId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnMintTokens.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val mintedAmount = viewModel.mintTokens(quoteId)
                if (mintedAmount != null) {
                    showToast("Successfully minted ${mintedAmount.value} sats!")
                    
                    // Refresh balance
                    viewModel.refreshBalance()
                    
                    // Clear the form
                    binding.apply {
                        edtAmount.text?.clear()
                        edtDescription.text?.clear()
                        txtQuoteInfo.visibility = View.GONE
                        txtQuoteStatus.visibility = View.GONE
                        btnCheckQuote.visibility = View.GONE
                        btnMintTokens.visibility = View.GONE
                    }
                    
                    currentQuoteId = null
                    isPollingQuote = false
                    
                } else {
                    showToast("Failed to mint tokens")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnMintTokens.isEnabled = true
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isPollingQuote = false
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
