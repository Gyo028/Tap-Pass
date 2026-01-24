package com.example.tap_pass.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.example.tap_pass.admin.UpcomingRequestsActivity
import com.example.tap_pass.login_register.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = Firebase.auth

        val fullNameTextView: TextView = findViewById(R.id.profile_full_name)
        val userIdTextView: TextView = findViewById(R.id.profile_user_id)
        val logoutButton: RelativeLayout = findViewById(R.id.logout_button)
        val resetPinButton: RelativeLayout = findViewById(R.id.reset_pin_button)
        val backButton: ImageView = findViewById(R.id.backButton)
        val upcomingRequestsButton: RelativeLayout = findViewById(R.id.upcoming_requests_button)
        val adminDivider: View = findViewById(R.id.admin_divider)

        val fullName = intent.getStringExtra("fullName")
        val username = intent.getStringExtra("username")
        val rfid = intent.getStringExtra("rfid")

        fullNameTextView.text = fullName
        userIdTextView.text = "RFID: $rfid"

        // Admin-only access control
        val isAdmin = "Admin".equals(username, ignoreCase = true)
        if (isAdmin) {
            upcomingRequestsButton.visibility = View.VISIBLE
            adminDivider.visibility = View.VISIBLE
            upcomingRequestsButton.setOnClickListener {
                startActivity(Intent(this, UpcomingRequestsActivity::class.java))
            }
        }

        backButton.setOnClickListener { finish() }

        logoutButton.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }

        resetPinButton.setOnClickListener {
            startActivity(Intent(this, ResetPinActivity::class.java))
        }
    }
}
