package com.example.tap_pass


import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Patterns
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.core.graphics.toColorInt



class Register : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var progressBar: ProgressBar
    private lateinit var createButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        progressBar = findViewById(R.id.progressbar)
        createButton = findViewById(R.id.createButton)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Hide ProgressBar initially
        progressBar.visibility = View.GONE

        createButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateForm()) {
                // Show loading and disable button
                createButton.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                createButton.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        // Re-enable button and hide loading
                        createButton.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (task.isSuccessful) {
                            // Navigate to SetupProfileActivity
                            val intent = Intent(this, SetupProfileActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.e("RegisterError", task.exception.toString())
                            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Setup clickable "Sign in" link
        val signInTextView: TextView = findViewById(R.id.signInText)
        val text = getString(R.string.sign_in_text)
        val ss = SpannableString(text)
        val startIndex = text.indexOf("Sign in")
        val endIndex = startIndex + "Sign in".length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(this@Register, LoginActivity::class.java))
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }
            ss.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInTextView.text = ss
            signInTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Handle edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val termsChecked = termsCheckBox.isChecked

        emailEditText.error = null
        passwordEditText.error = null
        confirmPasswordEditText.error = null

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }
        if (password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters long"
            return false
        }
        if (!isValidPassword(password)) {
            passwordEditText.error =
                "Password must contain at least one number and one special character"
            return false
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Confirm Password is required"
            return false
        }
        if (confirmPassword != password) {
            confirmPasswordEditText.error = "Passwords do not match"
            return false
        }
        if (!termsChecked) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
        return passwordPattern.matches(password)
    }
}