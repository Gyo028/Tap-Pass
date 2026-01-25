package com.example.tap_pass.profile

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. UI Setup (Edge-to-Edge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        setContentView(R.layout.activity_change_password)
        window.statusBarColor = Color.TRANSPARENT

        window.decorView.post { hideSystemUI() }

        // 2. Initialize Firebase and Views
        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressbar)

        val currentPwdEditText = findViewById<TextInputEditText>(R.id.currentPassword)
        val newPwdEditText = findViewById<TextInputEditText>(R.id.newPassword)
        val confirmNewPwdEditText = findViewById<TextInputEditText>(R.id.confirmNewPassword)
        val confirmButton = findViewById<Button>(R.id.confirm_reset_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        // 3. Button Listeners
        cancelButton.setOnClickListener { finish() }

        confirmButton.setOnClickListener {
            val currentPwd = currentPwdEditText.text.toString().trim()
            val newPwd = newPwdEditText.text.toString().trim()
            val confirmPwd = confirmNewPwdEditText.text.toString().trim()

            // Basic Validation
            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPwd.length < 6) {
                newPwdEditText.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (newPwd != confirmPwd) {
                confirmNewPwdEditText.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Start Firebase Update
            performPasswordUpdate(currentPwd, newPwd)
        }
    }

    private fun performPasswordUpdate(currentPwd: String, newPwd: String) {
        val user = auth.currentUser

        if (user != null && user.email != null) {
            progressBar.visibility = View.VISIBLE

            // Re-authenticate user for security
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPwd)

            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // Re-auth success, proceed to update
                    user.updatePassword(newPwd).addOnCompleteListener { updateTask ->
                        progressBar.visibility = View.GONE
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Authentication failed. Check current password.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
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
    }
}