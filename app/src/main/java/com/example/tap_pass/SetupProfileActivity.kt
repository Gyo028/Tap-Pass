package com.example.tap_pass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SetupProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        val fullNameEditText: EditText = findViewById(R.id.setup_full_name)
        val usernameEditText: EditText = findViewById(R.id.setup_username)
        val rfidEditText: EditText = findViewById(R.id.setup_rfid)
        val phoneEditText: EditText = findViewById(R.id.setup_phone_number)
        val continueButton: Button = findViewById(R.id.continue_button)

        // Retrieve the username from the registration screen to pre-fill the field
        val registeredUsername = intent.getStringExtra("username")
        usernameEditText.setText(registeredUsername)

        continueButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val username = usernameEditText.text.toString()
            val rfid = rfidEditText.text.toString()
            val phone = phoneEditText.text.toString()

            // Basic validation
            if (fullName.isBlank()) {
                fullNameEditText.error = "Full name is required"
                return@setOnClickListener
            }
            if (username.isBlank()) {
                usernameEditText.error = "Username is required"
                return@setOnClickListener
            }
            if (rfid.isBlank()) {
                rfidEditText.error = "RFID is required"
                return@setOnClickListener
            }
            if (phone.isBlank()) {
                phoneEditText.error = "Phone number is required"
                return@setOnClickListener
            }

            // In a real app, you would save the profile data here.

            // Navigate to the home screen
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("username", username)
            intent.putExtra("rfid", rfid)
            intent.putExtra("phone", phone)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
