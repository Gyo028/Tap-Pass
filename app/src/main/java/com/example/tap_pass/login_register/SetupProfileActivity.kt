package com.example.tap_pass.login_register

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.home_main.MainActivity
import com.example.tap_pass.R
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

        // 1. Prepare window for full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_setup_profile)

        // 2. Safely hide status and navigation bars
        window.decorView.post {
            hideSystemUI()
        }

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

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
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
            "rfidHex" to hexId,
            "rfidUid" to readableId,
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