package com.example.tap_pass

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat

class ConfirmSendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_send)

        val backButton: ImageView = findViewById(R.id.backButton)
        val confirmButton: Button = findViewById(R.id.confirmButton)
        val amountValue: TextView = findViewById(R.id.amountValue)
        val recipientName: TextView = findViewById(R.id.recipientName)
        val recipientRfid: TextView = findViewById(R.id.recipientRfid)

        val amount = intent.getStringExtra("amount")
        val rfid = intent.getStringExtra("rfid")
        val name = intent.getStringExtra("recipientName")

        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = java.util.Currency.getInstance("PHP")
        amountValue.text = format.format(amount?.toDoubleOrNull() ?: 0.0)

        recipientName.text = name
        recipientRfid.text = "RFID: $rfid"

        backButton.setOnClickListener {
            finish()
        }

        confirmButton.setOnClickListener {
            Toast.makeText(this, "Send Confirmed (Placeholder)", Toast.LENGTH_SHORT).show()
            // Here you would add the logic to actually send the load
            finish()
        }
    }
}
