package com.example.tap_pass.transactions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class SendActivity : AppCompatActivity() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var balanceText: TextView
    private val MAX_LIMIT = 50000.0 // The 50k restriction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Prepare for full screen (replaces enableEdgeToEdge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        setContentView(R.layout.activity_send)

        window.decorView.post {
            hideSystemUI()
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val amountInput: EditText = findViewById(R.id.amountToSendInput)
        val rfidInput: EditText = findViewById(R.id.recipientRfidInput)
        balanceText = findViewById(R.id.currentBalanceValue)

        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        val currentUserId = auth.currentUser?.uid ?: return

        // YOUR Balance Listener
        fstore.collection("users")
            .document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val balance = snapshot.getDouble("balance") ?: 0.0
                    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                    balanceText.text = format.format(balance)
                }
            }

        backButton.setOnClickListener { finish() }

        nextButton.setOnClickListener {
            val amountStr = amountInput.text.toString().trim()
            val rfid = rfidInput.text.toString().trim()

            if (amountStr.isEmpty() || rfid.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountValue = amountStr.toDoubleOrNull() ?: 0.0

            // 1. LOOKUP: Check recipient
            fstore.collection("users")
                .whereEqualTo("rfidUid", rfid)
                .get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val recipientDoc = query.documents[0]
                        val name = recipientDoc.getString("fullName") ?: "Unknown User"
                        val recipientBalance = recipientDoc.getDouble("balance") ?: 0.0

                        // 2. RESTRICTION: Check if recipient will exceed 50k
                        if (recipientBalance + amountValue > MAX_LIMIT) {
                            val remainingSpace = MAX_LIMIT - recipientBalance
                            Toast.makeText(
                                this,
                                "Recipient cannot receive this amount. Limit: ₱50,000. They can only receive ₱${String.format("%.2f", remainingSpace)} more.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // 3. PROCEED: If everything is okay
                            val intent = Intent(this, ConfirmSendActivity::class.java)
                            intent.putExtra("amount", amountStr)
                            intent.putExtra("rfid", rfid)
                            intent.putExtra("recipientName", name)
                            // Optional: pass recipient UID to ConfirmSendActivity to save another lookup
                            intent.putExtra("recipientUid", recipientDoc.id)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "RFID not registered", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error finding user", Toast.LENGTH_SHORT).show()
                }
        }
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
}