package com.example.tap_pass

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class SendActivity : AppCompatActivity() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var balanceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        // 1. Initialize Views
        val backButton: ImageView = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val amountInput: EditText = findViewById(R.id.amountToSendInput)
        val rfidInput: EditText = findViewById(R.id.recipientRfidInput)
        balanceText = findViewById(R.id.currentBalanceValue)

        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        val currentUserId = auth.currentUser?.uid ?: return

        // 2. Real-time Balance Listener (Shows YOUR current balance)
        fstore.collection("users")
            .document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val balance = snapshot.getLong("balance") ?: 0
                val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                balanceText.text = format.format(balance)
            }

        backButton.setOnClickListener { finish() }

        // 3. Next Button Logic
        nextButton.setOnClickListener {
            val amount = amountInput.text.toString().trim()
            val rfid = rfidInput.text.toString().trim()

            if (amount.isNotEmpty() && rfid.isNotEmpty()) {
                // LOOKUP: Check if the RFID exists and get the Name BEFORE moving to Confirm screen
                fstore.collection("users")
                    .whereEqualTo("rfidUid", rfid)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            val recipientDoc = query.documents[0]
                            val name = recipientDoc.getString("fullName") ?: "Unknown User"

                            // PASS DATA: Send amount, rfid, AND the name we just found
                            val intent = Intent(this, ConfirmSendActivity::class.java)
                            intent.putExtra("amount", amount)
                            intent.putExtra("rfid", rfid)
                            intent.putExtra("recipientName", name)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "RFID not registered", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error finding user", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}