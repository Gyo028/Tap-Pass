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

        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        fullNameEditText = findViewById(R.id.setup_full_name)
        rfidEditText = findViewById(R.id.setup_rfid)
        phoneEditText = findViewById(R.id.setup_phone_number)
        progressBar = findViewById(R.id.progressbar)
        val continueButton: Button = findViewById(R.id.continue_button)

        progressBar.visibility = View.GONE

        continueButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val userInputCode = rfidEditText.text.toString().trim()

            checkRfidMapping(userId, userInputCode)
        }
    }

    private fun checkRfidMapping(userId: String, typedCode: String) {
        progressBar.visibility = View.VISIBLE

        fstore.collection("rfid_mapping")
            .whereEqualTo("rfid_uid", typedCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val mappingDoc = querySnapshot.documents[0]
                    val hexId = mappingDoc.id
                    val readableId = mappingDoc.getString("rfid_uid") ?: typedCode
                    val currentOwner = mappingDoc.getString("owner_userid")

                    if (currentOwner.isNullOrEmpty()) {
                        // Pass both the HEX (for hardware) and the UID (for profile)
                        saveFinalProfile(userId, hexId, readableId)
                    } else {
                        progressBar.visibility = View.GONE
                        rfidEditText.error = "This card is already registered"
                    }
                } else {
                    progressBar.visibility = View.GONE
                    rfidEditText.error = "Invalid Card ID"
                    Toast.makeText(this, "RFID ID not found in system", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFinalProfile(userId: String, hexId: String, readableId: String) {
        val fullName = fullNameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        val profileData = hashMapOf(
            "fullName" to fullName,
            "rfidHex" to hexId,      // The hardware-level UID (e.g., A1 B2 C3 D4)
            "rfidUid" to readableId, // The user-friendly ID (e.g., 12345678)
            "phoneNumber" to phone,
            "email" to auth.currentUser?.email,
            "balance" to 0.0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val batch = fstore.batch()
        val userRef = fstore.collection("users").document(userId)
        val mappingRef = fstore.collection("rfid_mapping").document(hexId)

        batch.set(userRef, profileData)
        batch.update(mappingRef, "owner_userid", userId)

        batch.commit().addOnSuccessListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Profile Setup Success!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Save Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateForm(): Boolean {
        if (fullNameEditText.text.isNullOrEmpty()) { fullNameEditText.error = "Required"; return false }
        if (rfidEditText.text.isNullOrEmpty()) { rfidEditText.error = "Required"; return false }
        if (phoneEditText.text.isNullOrEmpty()) { phoneEditText.error = "Required"; return false }
        return true
    }
}