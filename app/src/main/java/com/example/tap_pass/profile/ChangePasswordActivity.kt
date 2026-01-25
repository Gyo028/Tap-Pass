package com.example.tap_pass.profile

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R

class ChangePasswordActivity : AppCompatActivity() {

    // Hardcoded password for testing
    private var MOCK_CURRENT_PASSWORD = "password123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge Drawing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_change_password)

        // Make Status Bar Transparent so background drawable shows through
        window.statusBarColor = Color.TRANSPARENT

        // 2. Apply Immersive UI
        window.decorView.post {
            hideSystemUI()
        }

        // 3. Initialize Views
        val currentPwdEditText: EditText = findViewById(R.id.currentPassword)
        val newPwdEditText: EditText = findViewById(R.id.newPassword)
        val confirmNewPwdEditText: EditText = findViewById(R.id.confirmNewPassword)
        val confirmButton: Button = findViewById(R.id.confirm_reset_button)
        val cancelButton: Button = findViewById(R.id.cancel_button)

        // 4. Navigation - Back to Profile
        cancelButton.setOnClickListener { finish() }

        // 5. Hardcoded Reset Logic
        confirmButton.setOnClickListener {
            val currentPwd = currentPwdEditText.text.toString().trim()
            val newPwd = newPwdEditText.text.toString().trim()
            val confirmPwd = confirmNewPwdEditText.text.toString().trim()

            // Clear previous errors
            currentPwdEditText.error = null
            newPwdEditText.error = null
            confirmNewPwdEditText.error = null

            when {
                currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                currentPwd != MOCK_CURRENT_PASSWORD -> {
                    currentPwdEditText.error = "Incorrect current password"
                }
                newPwd.length < 6 -> {
                    newPwdEditText.error = "Password must be at least 6 characters"
                }
                confirmPwd != newPwd -> {
                    confirmNewPwdEditText.error = "Passwords do not match"
                }
                else -> {
                    // Update successful
                    MOCK_CURRENT_PASSWORD = newPwd
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                // Hide only status bars to keep navigation accessible, or both for full immersive
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