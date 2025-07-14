package com.example.cdkwallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cdkwallet.databinding.ActivitySendBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SendActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySendBinding
    private val viewModel: WalletViewModel by viewModels { 
        WalletViewModel.createFactory(application) 
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeWalletState()
    }
    
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            
            btnScanQr.setOnClickListener {
                startActivity(Intent(this@SendActivity, QRCodeActivity::class.java).apply {
                    putExtra("mode", "scan")
                })
            }
            
            btnPrepareSend.setOnClickListener {
                val amountText = edtAmount.text.toString().trim()
                if (amountText.isNotBlank()) {
                    val amount = amountText.toULongOrNull()
                    if (amount != null && amount > 0UL) {
                        prepareSend(amount)
                    } else {
                        showToast("Please enter a valid amount")
                    }
                } else {
                    showToast("Please enter an amount")
                }
            }
            
            btnSendTokens.setOnClickListener {
                val amountText = edtAmount.text.toString().trim()
                val memo = edtMemo.text.toString().trim().takeIf { it.isNotBlank() }
                
                if (amountText.isNotBlank()) {
                    val amount = amountText.toULongOrNull()
                    if (amount != null && amount > 0UL) {
                        sendTokens(amount, memo)
                    } else {
                        showToast("Please enter a valid amount")
                    }
                } else {
                    showToast("Please enter an amount")
                }
            }
            
            btnPayInvoice.setOnClickListener {
                val invoice = edtInvoice.text.toString().trim()
                if (invoice.isNotBlank()) {
                    payLightningInvoice(invoice)
                } else {
                    showToast("Please enter a Lightning invoice")
                }
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
    
    private fun prepareSend(amount: ULong) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPrepareSend.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val preparedSend = viewModel.prepareSend(amount)
                if (preparedSend != null) {
                    binding.apply {
                        txtPreparedInfo.text = """
                            Amount: ${preparedSend.amount.value} sats
                            Swap Fee: ${preparedSend.swapFee.value} sats
                            Send Fee: ${preparedSend.sendFee.value} sats
                            Total Fee: ${preparedSend.totalFee.value} sats
                        """.trimIndent()
                        txtPreparedInfo.visibility = View.VISIBLE
                        btnSendTokens.visibility = View.VISIBLE
                    }
                } else {
                    showToast("Failed to prepare send")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnPrepareSend.isEnabled = true
            }
        }
    }
    
    private fun sendTokens(amount: ULong, memo: String?) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendTokens.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val token = viewModel.sendTokens(amount, memo)
                if (token != null) {
                    // Show QR code of the token
                    startActivity(Intent(this@SendActivity, QRCodeActivity::class.java).apply {
                        putExtra("mode", "show")
                        putExtra("data", token.tokenString)
                        putExtra("title", "Cashu Token")
                        putExtra("subtitle", "${amount} sats")
                    })
                    
                    // Refresh balance
                    viewModel.refreshBalance()
                    showToast("Tokens sent successfully!")
                    
                    // Clear fields
                    binding.edtAmount.text?.clear()
                    binding.edtMemo.text?.clear()
                    binding.txtPreparedInfo.visibility = View.GONE
                    binding.btnSendTokens.visibility = View.GONE
                    
                } else {
                    showToast("Failed to send tokens")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSendTokens.isEnabled = true
            }
        }
    }
    
    private fun payLightningInvoice(invoice: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPayInvoice.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // First create a melt quote
                val meltQuote = viewModel.createMeltQuote(invoice)
                if (meltQuote != null) {
                    binding.txtInvoiceInfo.text = """
                        Quote ID: ${meltQuote.id}
                        Amount: ${meltQuote.amount.value} sats
                        Fee Reserve: ${meltQuote.feeReserve.value} sats
                    """.trimIndent()
                    binding.txtInvoiceInfo.visibility = View.VISIBLE
                    
                    // Small delay for user to see the quote info
                    delay(1000)
                    
                    // Now pay the invoice
                    val meltResult = viewModel.payInvoice(meltQuote.id)
                    if (meltResult != null) {
                        showToast("Invoice paid successfully!")
                        binding.txtInvoiceInfo.text = "${binding.txtInvoiceInfo.text}\n\nPayment Status: ${meltResult.state}\nFee Paid: ${meltResult.feePaid.value} sats"
                        
                        // Refresh balance
                        viewModel.refreshBalance()
                        
                        // Clear invoice field
                        binding.edtInvoice.text?.clear()
                    } else {
                        showToast("Failed to pay invoice")
                    }
                } else {
                    showToast("Failed to create payment quote")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnPayInvoice.isEnabled = true
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
