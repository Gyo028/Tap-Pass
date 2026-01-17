package com.example.tap_pass

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LoadActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imagePreview.setImageURI(it)
            val uploadButton: Button = findViewById(R.id.uploadButton)
            uploadButton.visibility = Button.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_activity)

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
            Toast.makeText(this, "Payment submitted for review!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
