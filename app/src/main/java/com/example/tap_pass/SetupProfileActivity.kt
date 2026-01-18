package com.example.tap_pass

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var rfidEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        // Bind views
        fullNameEditText = findViewById(R.id.setup_full_name)
        rfidEditText = findViewById(R.id.setup_rfid)
        phoneEditText = findViewById(R.id.setup_phone_number)
        progressBar = findViewById(R.id.progressbar)
        val continueButton: Button = findViewById(R.id.continue_button)

        progressBar.visibility = View.GONE // hide initially

        continueButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            progressBar.visibility = View.VISIBLE // Show spinner

            val userId = auth.currentUser?.uid
            if (userId == null) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = fullNameEditText.text.toString().trim()
            val rfid = rfidEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            val userProfile = hashMapOf(
                "fullName" to fullName,
                "rfidUid" to rfid,
                "phoneNumber" to phone,
                "email" to auth.currentUser?.email,
                "balance" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )

            fstore.collection("users")
                .document(userId)
                .set(userProfile)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile setup complete", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // This must be outside onCreate
    private fun validateForm(): Boolean {
        val fullName = fullNameEditText.text.toString().trim()
        val rfid = rfidEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full name is required"
            return false
        }
        if (rfid.isEmpty()) {
            rfidEditText.error = "RFID is required"
            return false
        }
        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number is required"
            return false
        }
        return true
    }
}
