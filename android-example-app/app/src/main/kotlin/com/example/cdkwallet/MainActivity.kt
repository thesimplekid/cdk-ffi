package com.example.cdkwallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cdkwallet.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WalletViewModel by viewModels { 
        WalletViewModel.Factory(application) 
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeWalletState()
        
        // Initialize wallet on first launch
        if (!viewModel.isWalletInitialized()) {
            initializeWallet()
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnSend.setOnClickListener {
                if (viewModel.isWalletInitialized()) {
                    startActivity(Intent(this@MainActivity, SendActivity::class.java))
                } else {
                    showToast("Wallet not initialized")
                }
            }
            
            btnReceive.setOnClickListener {
                if (viewModel.isWalletInitialized()) {
                    startActivity(Intent(this@MainActivity, ReceiveActivity::class.java))
                } else {
                    showToast("Wallet not initialized")
                }
            }
            
            btnRefresh.setOnClickListener {
                if (viewModel.isWalletInitialized()) {
                    refreshWallet()
                } else {
                    initializeWallet()
                }
            }
            
            btnReset.setOnClickListener {
                resetWallet()
            }
        }
    }
    
    private fun observeWalletState() {
        viewModel.walletState.observe(this) { state ->
            binding.apply {
                when (state) {
                    is WalletState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        btnSend.isEnabled = false
                        btnReceive.isEnabled = false
                        btnRefresh.isEnabled = false
                        txtStatus.text = "Loading..."
                    }
                    is WalletState.Initialized -> {
                        progressBar.visibility = View.GONE
                        btnSend.isEnabled = true
                        btnReceive.isEnabled = true
                        btnRefresh.isEnabled = true
                        txtBalance.text = "${state.balance} sats"
                        txtMintUrl.text = state.mintUrl
                        txtStatus.text = "Wallet Ready"
                        txtMnemonic.text = "Mnemonic: ${state.mnemonic}"
                    }
                    is WalletState.Error -> {
                        progressBar.visibility = View.GONE
                        btnSend.isEnabled = false
                        btnReceive.isEnabled = false
                        btnRefresh.isEnabled = true
                        txtStatus.text = "Error: ${state.error}"
                        showToast("Error: ${state.error}")
                    }
                    is WalletState.Uninitialized -> {
                        progressBar.visibility = View.GONE
                        btnSend.isEnabled = false
                        btnReceive.isEnabled = false
                        btnRefresh.isEnabled = true
                        txtBalance.text = "0 sats"
                        txtMintUrl.text = "No mint connected"
                        txtStatus.text = "Wallet not initialized"
                        txtMnemonic.text = "No mnemonic"
                    }
                }
            }
        }
    }
    
    private fun initializeWallet() {
        lifecycleScope.launch {
            try {
                viewModel.initializeWallet()
                showToast("Wallet initialized successfully")
            } catch (e: Exception) {
                showToast("Failed to initialize wallet: ${e.message}")
            }
        }
    }
    
    private fun refreshWallet() {
        lifecycleScope.launch {
            try {
                viewModel.refreshBalance()
                showToast("Balance updated")
            } catch (e: Exception) {
                showToast("Failed to refresh: ${e.message}")
            }
        }
    }
    
    private fun resetWallet() {
        lifecycleScope.launch {
            try {
                viewModel.resetWallet()
                showToast("Wallet reset successfully")
            } catch (e: Exception) {
                showToast("Failed to reset wallet: ${e.message}")
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
