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

        // 1. UI Configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        setContentView(R.layout.activity_profile)
        window.statusBarColor = Color.TRANSPARENT

        window.decorView.post {
            hideSystemUI()
        }

        auth = FirebaseAuth.getInstance()

        // 2. View Initialization
        val fullNameTextView: TextView = findViewById(R.id.profile_full_name)
        val userIdTextView: TextView = findViewById(R.id.profile_user_id)
        val logoutButton: RelativeLayout = findViewById(R.id.logout_button)
        val changePasswordButton: RelativeLayout = findViewById(R.id.reset_pin_button)
        val backButton: ImageView = findViewById(R.id.backButton)
        val upcomingRequestsButton: RelativeLayout = findViewById(R.id.upcoming_requests_button)
        val adminDivider: View = findViewById(R.id.admin_divider)

        // 3. Data Population (with null-safety fallbacks)
        val fullName = intent.getStringExtra("fullName") ?: "User"
        val username = intent.getStringExtra("username") ?: ""
        val rfid = intent.getStringExtra("rfid") ?: "Not Set"

        fullNameTextView.text = fullName
        userIdTextView.text = "RFID: $rfid"

        // 4. Admin Role Check
        val isAdmin = "Admin".equals(username, ignoreCase = true)
        if (isAdmin) {
            upcomingRequestsButton.visibility = View.VISIBLE
            adminDivider.visibility = View.VISIBLE
            upcomingRequestsButton.setOnClickListener {
                startActivity(Intent(this, UpcomingRequestsActivity::class.java))
            }
        } else {
            upcomingRequestsButton.visibility = View.GONE
            adminDivider.visibility = View.GONE
        }

        // 5. Navigation Listeners
        backButton.setOnClickListener {
            finish()
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // 6. Logout Logic (The Fix)
        logoutButton.setOnClickListener {
            // Sign out from Firebase first
            auth.signOut()

            // Define the destination
            val intent = Intent(this, LoginActivity::class.java)

            // Flags: CLEAR_TOP ensures we don't just add a new LoginActivity on top,
            // but return to the existing one (if it exists) or create it as the new root.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // finish() is enough here because CLEAR_TASK handles the stack.
            // If the app still closes, try removing finishAffinity() and using finish().
            finish()
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