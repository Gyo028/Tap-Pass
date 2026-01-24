package com.example.tap_pass.inbox

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Currency

class LoadActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var uploadButton: Button
    private lateinit var totalAmountValue: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var submitButton: Button

    private var imageUri: Uri? = null
    private var totalAmount = 0.0
    private val transactionHistory = mutableListOf<Double>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imagePreview.setImageURI(it)
            uploadButton.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        // Initialize Views
        imagePreview = findViewById(R.id.imagePreview)
        totalAmountValue = findViewById(R.id.totalAmountValue)
        uploadButton = findViewById(R.id.uploadButton)
        progressBar = findViewById(R.id.progressBar)
        submitButton = findViewById(R.id.submitButton)

        // Navigation & Upload
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        uploadButton.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Preset Amount Buttons
        findViewById<Button>(R.id.button50).setOnClickListener { addToTotal(50.0) }
        findViewById<Button>(R.id.button100).setOnClickListener { addToTotal(100.0) }
        findViewById<Button>(R.id.button150).setOnClickListener { addToTotal(150.0) }
        findViewById<Button>(R.id.button200).setOnClickListener { addToTotal(200.0) }

        // Custom Amount Input
        findViewById<Button>(R.id.addCustomAmountButton).setOnClickListener {
            val customInput = findViewById<EditText>(R.id.customAmountInput)
            val amount = customInput.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                addToTotal(amount)
                customInput.text.clear()
            }
        }

        // Tools
        findViewById<Button>(R.id.undoButton).setOnClickListener { undoLast() }
        findViewById<Button>(R.id.clearButton).setOnClickListener { clearAll() }

        // Submit Action
        submitButton.setOnClickListener { startSubmissionProcess() }

        updateTotalDisplay()
    }

    private fun addToTotal(amount: Double) {
        totalAmount += amount
        transactionHistory.add(amount)
        updateTotalDisplay()
    }

    private fun undoLast() {
        if (transactionHistory.isNotEmpty()) {
            totalAmount -= transactionHistory.removeAt(transactionHistory.size - 1)
            if (totalAmount < 0) totalAmount = 0.0
            updateTotalDisplay()
        }
    }

    private fun clearAll() {
        totalAmount = 0.0
        transactionHistory.clear()
        imageUri = null
        imagePreview.setImageResource(R.drawable.placeholder)
        uploadButton.visibility = View.VISIBLE
        updateTotalDisplay()
    }

    private fun updateTotalDisplay() {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("PHP")
        totalAmountValue.text = format.format(totalAmount)
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        submitButton.isEnabled = !isLoading
        submitButton.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun startSubmissionProcess() {
        val user = auth.currentUser ?: return

        // Validation
        if (totalAmount <= 0) {
            showAlert("Error", "Please enter a valid amount.")
            return
        }
        if (imageUri == null) {
            showAlert("Required", "Please upload your proof of payment.")
            return
        }

        setLoading(true)

        // FETCH USER PROFILE DATA FIRST
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val realName = document.getString("fullName") ?: "Unknown User"
                    val rfid = document.getString("rfidUid") ?: "No RFID Assigned"

                    // Now submit with the actual data
                    submitRequestToAdmin(user.uid, realName, rfid)
                } else {
                    setLoading(false)
                    showAlert("Profile Error", "User record not found in database.")
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showAlert("Network Error", "Could not verify user: ${e.message}")
            }
    }

    private fun submitRequestToAdmin(userId: String, realName: String, rfid: String) {
        val loadData = hashMapOf(
            "userId" to userId,
            "fullName" to realName,
            "rfidUid" to rfid,
            "amount" to totalAmount,
            "proof" to imageUri.toString(),
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "pending",
            "remarks" to ""
        )

        db.collection("load_topup")
            .add(loadData)
            .addOnSuccessListener {
                setLoading(false)
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showAlert("Firestore Error", e.message ?: "Failed to submit request.")
            }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Request Submitted")
            .setMessage("Your top-up of ₱${String.format("%.2f", totalAmount)} has been sent for review.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }
}