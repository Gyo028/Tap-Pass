package com.example.tap_pass.transactions

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.R
import com.example.tap_pass.home_main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class ConfirmSendActivity : AppCompatActivity() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val MAX_LIMIT = 50000.0 // ₱50,000 limit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_send)

        val backButton: ImageView = findViewById(R.id.backButton)
        val confirmButton: Button = findViewById(R.id.confirmButton)
        val amountValue: TextView = findViewById(R.id.amountValue)
        val recipientName: TextView = findViewById(R.id.recipientName)
        val recipientRfid: TextView = findViewById(R.id.recipientRfid)

        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        val amountStr = intent.getStringExtra("amount")
        val amount = amountStr?.toDoubleOrNull() ?: 0.0

        if (amount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        amountValue.text = format.format(amount)

        val rfid = intent.getStringExtra("rfid") ?: ""
        val name = intent.getStringExtra("recipientName") ?: ""
        recipientName.text = name
        recipientRfid.text = "RFID: $rfid"

        backButton.setOnClickListener { finish() }

        confirmButton.setOnClickListener {
            confirmButton.isEnabled = false // Prevent double clicks
            sendMoney(amount, rfid)
        }
    }

    private fun sendMoney(amount: Double, recipientRfid: String) {
        val senderUid = auth.currentUser?.uid ?: return

        fstore.collection("users")
            .whereEqualTo("rfidUid", recipientRfid)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    Toast.makeText(this, "Recipient not found", Toast.LENGTH_SHORT).show()
                    findViewById<Button>(R.id.confirmButton).isEnabled = true
                    return@addOnSuccessListener
                }

                val recipientDoc = query.documents[0]
                processTransaction(senderUid, recipientDoc.id, amount, recipientRfid)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                findViewById<Button>(R.id.confirmButton).isEnabled = true
            }
    }

    private fun processTransaction(senderUid: String, recipientUid: String, amount: Double, recipientRfid: String) {
        val senderRef = fstore.collection("users").document(senderUid)
        val recipientRef = fstore.collection("users").document(recipientUid)

        fstore.runTransaction { transaction ->
            val senderSnap = transaction.get(senderRef)
            val recipientSnap = transaction.get(recipientRef)

            val senderBalance = senderSnap.getDouble("balance") ?: 0.0
            val recipientBalance = recipientSnap.getDouble("balance") ?: 0.0
            val senderName = senderSnap.getString("fullName") ?: "Unknown"
            val recipientName = recipientSnap.getString("fullName") ?: "Unknown"
            val senderRfid = senderSnap.getString("rfidUid") ?: "Unknown"

            // 1. Logic Validations
            if (amount > senderBalance) {
                throw Exception("Insufficient balance")
            }

            if (recipientBalance + amount > MAX_LIMIT) {
                throw Exception("Recipient's balance would exceed ₱50,000 limit.")
            }

            // 2. Atomic Balance Updates
            transaction.update(senderRef, "balance", senderBalance - amount)
            transaction.update(recipientRef, "balance", recipientBalance + amount)

            // 3. Create History Logs for both sides
            val senderTx = hashMapOf(
                "type" to "SEND",
                "amount" to amount,
                "otherUserRfid" to recipientRfid,
                "otherUserName" to recipientName,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val recipientTx = hashMapOf(
                "type" to "RECEIVE",
                "amount" to amount,
                "otherUserRfid" to senderRfid,
                "otherUserName" to senderName,
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Save to the sub-collections
            transaction.set(senderRef.collection("transactions").document(), senderTx)
            transaction.set(recipientRef.collection("transactions").document(), recipientTx)

            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Transfer successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            findViewById<Button>(R.id.confirmButton).isEnabled = true
            Toast.makeText(this, it.message ?: "Transaction failed", Toast.LENGTH_LONG).show()
        }
    }
}