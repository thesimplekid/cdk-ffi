package com.example.cdkwallet

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cdkwallet.databinding.ActivityQrcodeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class QRCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeBinding
    
    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            showToast("Scan cancelled")
        } else {
            handleScannedData(result.contents)
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startScanning()
        } else {
            showToast("Camera permission required for scanning")
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        handleIntent()
    }
    
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            
            btnCopy.setOnClickListener {
                copyToClipboard()
            }
            
            btnShare.setOnClickListener {
                shareData()
            }
        }
    }
    
    private fun handleIntent() {
        val mode = intent.getStringExtra("mode") ?: return
        
        when (mode) {
            "show" -> {
                val data = intent.getStringExtra("data") ?: return
                val title = intent.getStringExtra("title") ?: "QR Code"
                val subtitle = intent.getStringExtra("subtitle") ?: ""
                
                showQRCode(data, title, subtitle)
            }
            "scan" -> {
                if (checkCameraPermission()) {
                    startScanning()
                } else {
                    requestCameraPermission()
                }
            }
        }
    }
    
    private fun showQRCode(data: String, title: String, subtitle: String) {
        binding.apply {
            txtTitle.text = title
            txtSubtitle.text = subtitle
            txtData.text = data
            
            // Generate QR code
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                    data, 
                    BarcodeFormat.QR_CODE, 
                    400, 
                    400
                )
                imgQrCode.setImageBitmap(bitmap)
                
                btnCopy.visibility = android.view.View.VISIBLE
                btnShare.visibility = android.view.View.VISIBLE
                
            } catch (e: WriterException) {
                showToast("Error generating QR code: ${e.message}")
            }
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, 
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    private fun startScanning() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR Code")
        options.setCameraId(0) // Use back camera
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(true)
        
        scanLauncher.launch(options)
    }
    
    private fun handleScannedData(scannedData: String) {
        // You can add logic here to handle different types of scanned data
        // For now, we'll just copy to clipboard and show the result
        copyToClipboard(scannedData)
        showToast("Scanned data copied to clipboard")
        finish()
    }
    
    private fun copyToClipboard(data: String? = null) {
        val textToCopy = data ?: binding.txtData.text.toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("QR Code Data", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Copied to clipboard")
    }
    
    private fun shareData() {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, binding.txtData.text.toString())
            putExtra(android.content.Intent.EXTRA_SUBJECT, binding.txtTitle.text.toString())
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share"))
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
