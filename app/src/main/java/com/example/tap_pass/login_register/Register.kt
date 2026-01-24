package com.example.tap_pass.login_register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        dataPolicyCheckBox = findViewById(R.id.dataPolicyCheckBox)
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
        val signInText = getString(R.string.sign_in_text)
        val ss = SpannableString(signInText)
        val signInStartIndex = signInText.indexOf("Sign in")
        val signInEndIndex = signInStartIndex + "Sign in".length

        if (signInStartIndex != -1) {
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
            ss.setSpan(clickableSpan, signInStartIndex, signInEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInTextView.text = ss
            signInTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Setup clickable "Terms and Conditions" link
        val termsTextView: TextView = findViewById(R.id.termsText)
        val termsText = getString(R.string.terms_agree_text)
        val termsSs = SpannableString(termsText)
        val termsStartIndex = termsText.indexOf("Terms and Conditions")
        val termsEndIndex = termsStartIndex + "Terms and Conditions".length

        if (termsStartIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showTermsDialog()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }
            termsSs.setSpan(clickableSpan, termsStartIndex, termsEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            termsTextView.text = termsSs
            termsTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Setup clickable "Data Policy Act" link
        val dataPolicyTextView: TextView = findViewById(R.id.dataPolicyText)
        val dataPolicyText = getString(R.string.data_policy_agree_text)
        val dataPolicySs = SpannableString(dataPolicyText)
        val dataPolicyStartIndex = dataPolicyText.indexOf("Data Policy Act")
        val dataPolicyEndIndex = dataPolicyStartIndex + "Data Policy Act".length

        if (dataPolicyStartIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showDataPolicyDialog()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = "#DDA0FF".toColorInt()
                    ds.isUnderlineText = true
                }
            }
            dataPolicySs.setSpan(clickableSpan, dataPolicyStartIndex, dataPolicyEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            dataPolicyTextView.text = dataPolicySs
            dataPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        // Handle edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showTermsDialog() {
        val termsAndConditions = """
        Terms and Conditions

        1. Acceptance of Terms
        By using this app and RFID card system, you agree to these Terms and Conditions.

        2. Accounts & RFID Usage
        Each customer is assigned an RFID card linked to their account. Login time, date, name, and RFID details may be recorded for monitoring and security.

        3. Cashless Payments
        All transactions in the café are processed through RFID cards. Customers can request to add balance via the mobile app, and admins must approve these requests before funds are available.

        4. Earnings & Records
        Displayed earnings and balances are based on system records. Actual values depend on verified transactions. The café is not responsible for errors caused by incorrect input or unauthorized use.

        5. User Responsibilities
        Customers and admins must not misuse the app, attempt unauthorized access, or tamper with RFID data. Fraudulent activity may result in account suspension.

        6. Privacy & Data
        Personal information (including RFID, login details, and transaction history) is stored securely and used only for account management and payment processing.

        7. Changes to Terms
        We may update these Terms and Conditions at any time. Continued use of the app means acceptance of the updated terms.
        """

        AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Terms and Conditions")
            .setMessage(termsAndConditions)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDataPolicyDialog() {
        val dataPolicy = """
        Data Policy Act

        Collection of Data
        We collect basic information such as customer name, RFID details, login time/date, and transaction records to operate the cashless payment system.

        Use of Data
        Data is used only for account management, payment processing, and monitoring café usage. Admins may access transaction requests to approve or reject them.

        Security
        All stored data is protected and not shared with third parties. RFID and login details are kept confidential.

        User Rights
        Customers may request to review or update their account information. Misuse or fraudulent activity may result in account suspension.

        Updates
        This Data Policy may be updated as needed. Continued use of the app means acceptance of the latest version.
        """

        AlertDialog.Builder(this, R.style.TermsDialogTheme)
            .setTitle("Data Policy Act")
            .setMessage(dataPolicy)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val termsChecked = termsCheckBox.isChecked
        val dataPolicyChecked = dataPolicyCheckBox.isChecked

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
        if (!dataPolicyChecked) {
            Toast.makeText(this, "Please accept the data policy act", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
        return passwordPattern.matches(password)
    }
}