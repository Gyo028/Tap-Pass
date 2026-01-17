package com.example.tap_pass

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResetPinActivity : AppCompatActivity() {

    // In a real app, you would get this from a secure source
    private var MOCK_CURRENT_PIN = "123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pin)

        val currentPinEditText: EditText = findViewById(R.id.currentPin)
        val newPinEditText: EditText = findViewById(R.id.newPin)
        val confirmNewPinEditText: EditText = findViewById(R.id.confirmNewPin)
        val confirmResetButton: Button = findViewById(R.id.confirm_reset_button)

        confirmResetButton.setOnClickListener {
            val currentPin = currentPinEditText.text.toString()
            val newPin = newPinEditText.text.toString()
            val confirmNewPin = confirmNewPinEditText.text.toString()

            // Clear previous errors
            currentPinEditText.error = null
            newPinEditText.error = null
            confirmNewPinEditText.error = null

            when {
                currentPin != MOCK_CURRENT_PIN -> {
                    currentPinEditText.error = "Incorrect current PIN"
                }
                newPin.length != 6 -> {
                    newPinEditText.error = "New PIN must be 6 digits"
                }
                confirmNewPin != newPin -> {
                    confirmNewPinEditText.error = "PINs do not match"
                }
                else -> {
                    // In a real app, you would save the new PIN securely
                    MOCK_CURRENT_PIN = newPin
                    Toast.makeText(this, "PIN updated successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the profile screen
                }
            }
        }
    }
}
