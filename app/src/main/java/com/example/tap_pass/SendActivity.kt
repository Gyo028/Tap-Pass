package com.example.tap_pass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val backButton: ImageView = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val amountInput: EditText = findViewById(R.id.amountToSendInput)
        val rfidInput: EditText = findViewById(R.id.recipientRfidInput)

        backButton.setOnClickListener {
            finish()
        }

        nextButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val rfid = rfidInput.text.toString()

            if (amount.isNotEmpty() && rfid.isNotEmpty()) {
                val intent = Intent(this, ConfirmSendActivity::class.java)
                intent.putExtra("amount", amount)
                intent.putExtra("rfid", rfid)
                intent.putExtra("recipientName", "Vincent Pogi") // Placeholder
                startActivity(intent)
            } else {
                // Show an error message if fields are empty
            }
        }
    }
}
