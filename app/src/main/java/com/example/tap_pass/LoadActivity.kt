package com.example.tap_pass

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

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
    private val MAX_LIMIT = 50000.0

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

        imagePreview = findViewById(R.id.imagePreview)
        totalAmountValue = findViewById(R.id.totalAmountValue)
        uploadButton = findViewById(R.id.uploadButton)
        progressBar = findViewById(R.id.progressBar)
        submitButton = findViewById(R.id.submitButton)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        uploadButton.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Amount Buttons
        findViewById<Button>(R.id.button50).setOnClickListener { addToTotal(50.0) }
        findViewById<Button>(R.id.button100).setOnClickListener { addToTotal(100.0) }
        findViewById<Button>(R.id.button150).setOnClickListener { addToTotal(150.0) }
        findViewById<Button>(R.id.button200).setOnClickListener { addToTotal(200.0) }

        findViewById<Button>(R.id.addCustomAmountButton).setOnClickListener {
            val customInput = findViewById<EditText>(R.id.customAmountInput)
            val amount = customInput.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                addToTotal(amount)
                customInput.text.clear()
            }
        }

        findViewById<Button>(R.id.undoButton).setOnClickListener { undoLast() }
        findViewById<Button>(R.id.clearButton).setOnClickListener { clearAll() }

        submitButton.setOnClickListener { startLoadTopupProcess() }

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

    private fun startLoadTopupProcess() {
        val user = auth.currentUser ?: return
        if (totalAmount <= 0 || imageUri == null) {
            showAlert("Required", "Please add an amount and upload your proof of payment.")
            return
        }

        setLoading(true)
        val userRef = db.collection("users").document(user.uid)

        // 1. Check current balance from server before updating
        userRef.get().addOnSuccessListener { document ->
            val currentBalance = document.getDouble("balance") ?: 0.0

            if (currentBalance + totalAmount > MAX_LIMIT) {
                setLoading(false)
                showAlert("Limit Exceeded", "Your account cannot hold more than ₱50,000. Current balance: ₱${String.format("%.2f", currentBalance)}")
            } else {
                executeBatchLoad(user.uid, userRef)
            }
        }.addOnFailureListener {
            setLoading(false)
            showAlert("Error", "Could not verify account status.")
        }
    }

    private fun executeBatchLoad(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val loadTopupRef = db.collection("load_topup").document()
        val inboxRef = userRef.collection("inbox").document()

        val loadData = hashMapOf(
            "userId" to userId,
            "amount" to totalAmount,
            "proof" to "image_uploaded",
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "processed"
        )

        val inboxData = hashMapOf(
            "title" to "Top-up Successful",
            "message" to "Successfully loaded ₱${String.format("%.2f", totalAmount)} to your account.",
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false,
            "type" to "LOAD"
        )

        val batch = db.batch()
        batch.update(userRef, "balance", FieldValue.increment(totalAmount))
        batch.set(loadTopupRef, loadData)
        batch.set(inboxRef, inboxData)

        batch.commit().addOnSuccessListener {
            setLoading(false)
            showSuccessDialog()
        }.addOnFailureListener { e ->
            setLoading(false)
            showAlert("Firestore Error", e.message ?: "Failed to process.")
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Load Success")
            .setMessage("₱$totalAmount has been successfully added to your account.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false).show()
    }

    private fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK", null).show()
    }
}