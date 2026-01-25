package com.example.tap_pass.inbox

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
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
    private lateinit var cancelButton: Button // Added Cancel Button

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

        // Enable edge-to-edge drawing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_load)

        // Hide Status Bar (Immersive)
        window.decorView.post {
            hideSystemUI()
        }

        // Initialize Views
        imagePreview = findViewById(R.id.imagePreview)
        totalAmountValue = findViewById(R.id.totalAmountValue)
        uploadButton = findViewById(R.id.uploadButton)
        progressBar = findViewById(R.id.progressBar)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton) // Initialized from XML

        // Navigation (Bottom Buttons)
        cancelButton.setOnClickListener { finish() }

        // Image Upload
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
        cancelButton.isEnabled = !isLoading // Disable cancel during upload
        submitButton.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun startSubmissionProcess() {
        val user = auth.currentUser ?: return

        if (totalAmount <= 0) {
            showAlert("Error", "Please enter a valid amount.")
            return
        }
        if (imageUri == null) {
            showAlert("Required", "Please upload your proof of payment.")
            return
        }

        setLoading(true)

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val realName = document.getString("fullName") ?: "Unknown User"
                    val rfid = document.getString("rfidUid") ?: "No RFID Assigned"
                    submitRequestToAdmin(user.uid, realName, rfid)
                } else {
                    setLoading(false)
                    showAlert("Profile Error", "User record not found.")
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showAlert("Network Error", e.message ?: "Failed to verify profile.")
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
                showAlert("Firestore Error", e.message ?: "Failed to submit.")
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

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                // Hide only status bar to match Home Screen; keep navigation if desired,
                // or hide both for true full screen:
                controller.hide(WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        // Make the system bar area transparent so the background draws through
        window.statusBarColor = Color.TRANSPARENT
    }
}