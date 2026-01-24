package com.example.tap_pass.transactions

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class ReceiveQrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_qr)

        val backButton: ImageView = findViewById(R.id.backButton)
        val qrCodeImageView: ImageView = findViewById(R.id.qrCodeImageView)

        val rfid = intent.getStringExtra("rfid") ?: "26-0001" // Placeholder

        backButton.setOnClickListener {
            finish()
        }

        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(rfid, BarcodeFormat.QR_CODE, 250, 250)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
