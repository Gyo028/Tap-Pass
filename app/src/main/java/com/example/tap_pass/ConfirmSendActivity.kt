package com.example.tap_pass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class ConfirmSendActivity : AppCompatActivity() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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
        val amount = amountStr?.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            finish() // Close activity if amount is invalid
            return
        }

        // Now safely set the formatted value
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        amountValue.text = format.format(amount)

        // Set recipient details
        val rfid = intent.getStringExtra("rfid") ?: ""
        val name = intent.getStringExtra("recipientName") ?: ""
        recipientName.text = name
        recipientRfid.text = "RFID: $rfid"

        backButton.setOnClickListener { finish() }

        confirmButton.setOnClickListener {
            if (amount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendMoney(amount, rfid)
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun sendMoney(amount: Double, recipientRfid: String) {
        val senderUid = auth.currentUser?.uid ?: return

        // Find recipient by RFID
        fstore.collection("users")
            .whereEqualTo("rfidUid", recipientRfid)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    Toast.makeText(this, "Recipient not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val recipientDoc = query.documents[0]
                val recipientUid = recipientDoc.id

                processTransaction(senderUid, recipientUid, amount, recipientRfid)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun processTransaction(
        senderUid: String,
        recipientUid: String,
        amount: Double,
        recipientRfid: String
    ) {
        val senderRef = fstore.collection("users").document(senderUid)
        val recipientRef = fstore.collection("users").document(recipientUid)

        fstore.runTransaction { transaction ->
            val senderSnap = transaction.get(senderRef)
            val recipientSnap = transaction.get(recipientRef)

            val senderBalance = senderSnap.getDouble("balance") ?: 0.0
            val recipientBalance = recipientSnap.getDouble("balance") ?: 0.0

            val senderName = senderSnap.getString("fullName") ?: "Unknown User"
            val recipientName = recipientSnap.getString("fullName") ?: "Unknown User"
            val senderRfid = senderSnap.getString("rfidUid") ?: ""

            //Insufficient balance
            if (amount > senderBalance) {
                throw Exception("Insufficient balance")
            }

            // Update balances
            transaction.update(senderRef, "balance", senderBalance - amount)
            transaction.update(recipientRef, "balance", recipientBalance + amount)

            //Sender transaction record
            val senderTx = hashMapOf(
                "type" to "SEND",
                "amount" to amount,
                "otherUserRfid" to recipientRfid,
                "otherUserName" to recipientName, // Added this
                "createdAt" to FieldValue.serverTimestamp()
            )

            //Recipient transaction record
            val recipientTx = hashMapOf(
                "type" to "RECEIVE",
                "amount" to amount,
                "otherUserRfid" to senderRfid,
                "otherUserName" to senderName, // Added this
                "createdAt" to FieldValue.serverTimestamp()
            )

            transaction.set(
                senderRef.collection("transactions").document(),
                senderTx
            )
            transaction.set(
                recipientRef.collection("transactions").document(),
                recipientTx
            )

            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Transfer successful", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, it.message ?: "Transaction failed", Toast.LENGTH_LONG).show()
        }
    }
}
