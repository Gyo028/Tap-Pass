package com.example.tap_pass.login_register

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.tap_pass.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Register : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var dataPolicyCheckBox: CheckBox
    private lateinit var progressBar: ProgressBar
    private lateinit var createButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Prepare for full screen (replaces enableEdgeToEdge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_register)

        // 2. Hide bars after view is attached to prevent crashes
        window.decorView.post {
            hideSystemUI()
        }

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        dataPolicyCheckBox = findViewById(R.id.dataPolicyCheckBox)
        progressBar = findViewById(R.id.progressbar)
        createButton = findViewById(R.id.createButton)

        auth = Firebase.auth
        progressBar.visibility = View.GONE

        createButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateForm()) {
                createButton.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                createButton.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        createButton.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (task.isSuccessful) {
                            val intent = Intent(this, SetupProfileActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.e("RegisterError", task.exception.toString())
                            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()
                            createButton.visibility = View.VISIBLE
                        }
                    }
            }
        }

        setupSpannableLinks()
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

    private fun setupSpannableLinks() {
        // Sign In Link
        val signInTextView: TextView = findViewById(R.id.signInText)
        val signInText = getString(R.string.sign_in_text)
        val ss = SpannableString(signInText)
        val signInStartIndex = signInText.indexOf("Sign in")
        if (signInStartIndex != -1) {
            ss.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(this@Register, LoginActivity::class.java))
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }, signInStartIndex, signInStartIndex + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInTextView.text = ss
            signInTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Terms Link
        val termsTextView: TextView = findViewById(R.id.termsText)
        val termsText = getString(R.string.terms_agree_text)
        val termsSs = SpannableString(termsText)
        val termsStartIndex = termsText.indexOf("Terms and Conditions")
        if (termsStartIndex != -1) {
            termsSs.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) = showTermsDialog()
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }, termsStartIndex, termsStartIndex + "Terms and Conditions".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            termsTextView.text = termsSs
            termsTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Data Policy Link
        val dataPolicyTextView: TextView = findViewById(R.id.dataPolicyText)
        val dataPolicyText = getString(R.string.data_policy_agree_text)
        val dataPolicySs = SpannableString(dataPolicyText)
        val dataPolicyStartIndex = dataPolicyText.indexOf("Data Policy Act")
        if (dataPolicyStartIndex != -1) {
            dataPolicySs.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) = showDataPolicyDialog()
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }, dataPolicyStartIndex, dataPolicyStartIndex + "Data Policy Act".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            dataPolicyTextView.text = dataPolicySs
            dataPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    // ... showTermsDialog(), showDataPolicyDialog(), validateForm(), and isValidPassword() remain the same as your previous code ...

    private fun showTermsDialog() {
        val termsAndConditions = """
        Terms and Conditions
        1. Acceptance of Terms...
        """ // (Shortened for brevity)
        AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Terms and Conditions")
            .setMessage(termsAndConditions)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun showDataPolicyDialog() {
        val dataPolicy = """
        Data Policy Act
        Collection of Data...
        """ // (Shortened for brevity)
        AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Data Policy Act")
            .setMessage(dataPolicy)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val termsChecked = termsCheckBox.isChecked
        val dataPolicyChecked = dataPolicyCheckBox.isChecked

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Valid email is required"; return false
        }
        if (password.length < 8 || !isValidPassword(password)) {
            passwordEditText.error = "Password must be 8+ chars with number & special char"; return false
        }
        if (confirmPassword != password) {
            confirmPasswordEditText.error = "Passwords do not match"; return false
        }
        if (!termsChecked || !dataPolicyChecked) {
            Toast.makeText(this, "Please accept all policies", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        return Regex("^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$").matches(password)
    }
}