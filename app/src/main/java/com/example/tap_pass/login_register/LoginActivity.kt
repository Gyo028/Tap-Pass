package com.example.tap_pass.login_register

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.admin.AdminActivity
import com.example.tap_pass.home_main.MainActivity
import com.example.tap_pass.R
import com.example.tap_pass.login_register.Register
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.login_email)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.progressbar)
        val registerTextView: TextView = findViewById(R.id.registerText)

        progressBar.visibility = View.GONE

        loginButton.setOnClickListener {
            loginUser()
        }

        setupRegisterLink(registerTextView)
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!validateForm(email, password)) return

        loginButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    checkUserRole(uid)
                } else {
                    progressBar.visibility = View.GONE
                    loginButton.visibility = View.VISIBLE
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserRole(uid: String) {
        // Step 1: Check if UID exists in 'admin' collection
        db.collection("admin").document(uid).get()
            .addOnSuccessListener { adminDoc ->
                if (adminDoc.exists()) {
                    // It is an Admin
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Admin access granted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Step 2: Not an Admin, check if UID exists in 'users' collection
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            progressBar.visibility = View.GONE
                            if (userDoc.exists()) {
                                // Existing User -> Home
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                // No Admin doc and No User doc -> Setup Profile
                                val intent = Intent(this, SetupProfileActivity::class.java)
                                startActivity(intent)
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            resetUIWithError("Error checking user profile")
                        }
                }
            }
            .addOnFailureListener {
                resetUIWithError("Role verification failed")
            }
    }

    private fun resetUIWithError(message: String) {
        progressBar.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateForm(email: String, password: String): Boolean {
        if (email.isEmpty()) { emailEditText.error = "Required"; return false }
        if (password.isEmpty()) { passwordEditText.error = "Required"; return false }
        return true
    }

    private fun setupRegisterLink(textView: TextView) {
        val text = getString(R.string.log_in_text)
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Sign up")
        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) = startActivity(
                    Intent(
                        this@LoginActivity,
                        Register::class.java
                    )
                )
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.parseColor("#DDA0FF")
                    ds.isUnderlineText = true
                }
            }
            spannableString.setSpan(clickableSpan, startIndex, startIndex + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}