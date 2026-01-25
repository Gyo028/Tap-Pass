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
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuthException
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        window.decorView.post { hideSystemUI() }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.login_email)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.progressbar)

        val registerTV = findViewById<TextView>(R.id.registerText)
        val forgotPassBtn = findViewById<Button>(R.id.forgotPasswordButton)

        loginButton.setOnClickListener { loginUser() }
        forgotPassBtn.setOnClickListener { handleForgotPassword() }

        setupRegisterLink(registerTV)
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        checkUserRole(user.uid)
                    } else {
                        resetUI()
                        showVerificationWarningDialog()
                    }
                } else {
                    resetUI()
                    val exception = task.exception
                    val errorCode = (exception as? FirebaseAuthException)?.errorCode

                    // CHECK THIS IN LOGCAT (Filter: AUTH_DEBUG)
                    Log.e("AUTH_DEBUG", "Login Failed. Code: $errorCode | Msg: ${exception?.message}")

                    val errorMessage = when (errorCode) {
                        "ERROR_INVALID_EMAIL" -> "The email format is incorrect."
                        "ERROR_WRONG_PASSWORD" -> "Incorrect password."
                        "ERROR_USER_NOT_FOUND" -> "No account exists for this email."
                        "ERROR_USER_DISABLED" -> "This account has been disabled."
                        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later."
                        "ERROR_OPERATION_NOT_ALLOWED" -> "Login provider not enabled in Firebase."
                        else -> "Login Failed: ${exception?.localizedMessage}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleForgotPassword() {
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            emailEditText.error = "Enter email first"
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showVerificationWarningDialog() {
        val user = auth.currentUser
        val builder = AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Email Not Verified")
            .setMessage("Please verify your email address to continue.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> auth.signOut() }
            .setNeutralButton("Resend Email") { _, _ ->
                user?.sendEmailVerification()?.addOnCompleteListener {
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.WHITE)
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
        }.addOnFailureListener { resetUI() }
    }

    private fun setupRegisterLink(textView: TextView) {
        val text = "Don't have an account? Sign up"
        val ss = SpannableString(text)
        val start = text.indexOf("Sign up")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, Register::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.parseColor("#DDA0FF")
                ds.isUnderlineText = true
            }
        }
        if (start != -1) {
            ss.setSpan(clickableSpan, start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = ss
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun resetUI() {
        progressBar.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}