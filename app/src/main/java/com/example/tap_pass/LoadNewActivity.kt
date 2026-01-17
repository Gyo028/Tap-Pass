package com.example.tap_pass

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat

class LoadNewActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private var imageUri: Uri? = null // To check if an image has been selected
    private var totalAmount = 0.0
    private lateinit var totalAmountValue: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imagePreview.setImageURI(it)
            val uploadButton: Button = findViewById(R.id.uploadButton)
            uploadButton.visibility = Button.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_new)

        imagePreview = findViewById(R.id.imagePreview)
        totalAmountValue = findViewById(R.id.totalAmountValue)
        val uploadButton: Button = findViewById(R.id.uploadButton)
        val submitButton: Button = findViewById(R.id.submitButton)
        val backButton: ImageView = findViewById(R.id.backButton)

        val button50: Button = findViewById(R.id.button50)
        val button100: Button = findViewById(R.id.button100)
        val button150: Button = findViewById(R.id.button150)
        val button200: Button = findViewById(R.id.button200)
        val customAmountInput: EditText = findViewById(R.id.customAmountInput)
        val addCustomAmountButton: Button = findViewById(R.id.addCustomAmountButton)

        backButton.setOnClickListener {
            finish()
        }

        uploadButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        button50.setOnClickListener { addToTotal(50.0) }
        button100.setOnClickListener { addToTotal(100.0) }
        button150.setOnClickListener { addToTotal(150.0) }
        button200.setOnClickListener { addToTotal(200.0) }

        addCustomAmountButton.setOnClickListener {
            val customAmount = customAmountInput.text.toString().toDoubleOrNull()
            if (customAmount != null) {
                addToTotal(customAmount)
                customAmountInput.text.clear()
            }
        }

        submitButton.setOnClickListener {
            // Only submit if an image has been chosen
            if (imageUri != null) {
                showConfirmationDialog()
            } else {
                // Show an error if no image is selected
                val errorDialog = AlertDialog.Builder(this)
                    .setTitle("No Image Selected")
                    .setMessage("Please upload a proof of payment before submitting.")
                    .setPositiveButton("OK", null)
                    .create()
                errorDialog.show()
            }
        }
        updateTotalDisplay()
    }

    private fun addToTotal(amount: Double) {
        totalAmount += amount
        updateTotalDisplay()
    }

    private fun updateTotalDisplay() {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = java.util.Currency.getInstance("PHP")
        totalAmountValue.text = format.format(totalAmount)
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Submitted")
        builder.setMessage("Your payment has been submitted for review. Please wait for confirmation.")
        builder.setPositiveButton("OK") { _, _ ->
            // When the user clicks "OK", finish the activity
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
