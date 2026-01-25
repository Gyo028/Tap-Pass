package com.example.tap_pass.profile

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.example.tap_pass.admin.UpcomingRequestsActivity
import com.example.tap_pass.login_register.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Prepare for edge-to-edge drawing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_profile)

        // Make background flow behind the status bar area
        window.statusBarColor = Color.TRANSPARENT

        // 2. Hide system bars after the view is stable
        window.decorView.post {
            hideSystemUI()
        }

        auth = FirebaseAuth.getInstance()

        // Initialize Views
        val fullNameTextView: TextView = findViewById(R.id.profile_full_name)
        val userIdTextView: TextView = findViewById(R.id.profile_user_id)
        val logoutButton: RelativeLayout = findViewById(R.id.logout_button)
        val changePasswordButton: RelativeLayout = findViewById(R.id.reset_pin_button)
        val backButton: ImageView = findViewById(R.id.backButton)
        val upcomingRequestsButton: RelativeLayout = findViewById(R.id.upcoming_requests_button)
        val adminDivider: View = findViewById(R.id.admin_divider)

        // Get user data passed from MainActivity
        val fullName = intent.getStringExtra("fullName")
        val username = intent.getStringExtra("username")
        val rfid = intent.getStringExtra("rfid")

        fullNameTextView.text = fullName
        userIdTextView.text = "RFID: $rfid"

        // Admin-only access control logic
        val isAdmin = "Admin".equals(username, ignoreCase = true)
        if (isAdmin) {
            upcomingRequestsButton.visibility = View.VISIBLE
            adminDivider.visibility = View.VISIBLE
            upcomingRequestsButton.setOnClickListener {
                startActivity(Intent(this, UpcomingRequestsActivity::class.java))
            }
        }

        // Standard Back Navigation
        backButton.setOnClickListener { finish() }

        // Logout Logic: Redirects to Login and clears the activity stack
        logoutButton.setOnClickListener {
            auth.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
            finish()
        }

        // Navigate to Change Password
        changePasswordButton.setOnClickListener {
            val passwordIntent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(passwordIntent)
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                // Hide only status bars for a cleaner look while keeping nav accessible
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