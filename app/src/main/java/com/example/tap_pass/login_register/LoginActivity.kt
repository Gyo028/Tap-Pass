package com.example.tap_pass.login_register

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.admin.AdminActivity
import com.example.tap_pass.home_main.MainActivity
import com.example.tap_pass.R
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_login)

        window.decorView.post {
            hideSystemUI()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.login_email)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.progressbar)
        val registerTextView: TextView = findViewById(R.id.registerText)

        progressBar.visibility = View.GONE
        loginButton.setOnClickListener { loginUser() }
        setupRegisterLink(registerTextView)
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
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

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Verify if email is confirmed
                    if (user != null && user.isEmailVerified) {
                        checkUserRole(user.uid)
                    } else {
                        // User exists but hasn't clicked the link
                        progressBar.visibility = View.GONE
                        loginButton.visibility = View.VISIBLE
                        showVerificationWarningDialog()
                        auth.signOut()
                    }
                } else {
                    resetUIWithError("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun showVerificationWarningDialog() {
        // Use your theme here to keep the background consistent
        val builder = AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Email Not Verified")
            .setMessage("You haven't verified your email yet. Please check your inbox for the link sent during registration.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Resend Email") { _, _ ->
                auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Verification email resent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error resending email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        val alertDialog = builder.create()
        alertDialog.show()

        // Set button colors to white after the dialog is shown
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.WHITE)
    }

    private fun checkUserRole(uid: String) {
        db.collection("admin").document(uid).get().addOnSuccessListener { adminDoc ->
            if (adminDoc.exists()) {
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                    val intent = if (userDoc.exists()) {
                        Intent(this, MainActivity::class.java)
                    } else {
                        Intent(this, SetupProfileActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }.addOnFailureListener { resetUIWithError("Role verification failed") }
    }

    private fun resetUIWithError(message: String) {
        progressBar.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupRegisterLink(textView: TextView) {
        val text = getString(R.string.log_in_text)
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Sign up")
        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(this@LoginActivity, Register::class.java))
                }
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