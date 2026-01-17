package com.example.tap_pass

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LoadNewActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private var imageUri: Uri? = null // To check if an image has been selected

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
        val uploadButton: Button = findViewById(R.id.uploadButton)
        val submitButton: Button = findViewById(R.id.submitButton)
        val backButton: ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        uploadButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
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
