package com.example.tap_pass.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tap_pass.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RequestDetailFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var request: TopUpRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_request_detail, container, false)
        db = FirebaseFirestore.getInstance()

        // 1. Retrieve the serializable data passed from the list
        request = arguments?.getSerializable("selected_request") as? TopUpRequest

        // 2. Map the views from XML
        val imgReceipt = view.findViewById<ImageView>(R.id.detailReceiptImage)
        val tvName = view.findViewById<TextView>(R.id.detailName)
        val tvAmount = view.findViewById<TextView>(R.id.detailAmount)
        val tvRfid = view.findViewById<TextView>(R.id.detailRfid)
        val etRemarks = view.findViewById<EditText>(R.id.etRemarks)
        val btnApprove = view.findViewById<Button>(R.id.btnApprove)
        val btnReject = view.findViewById<Button>(R.id.btnRejectDetail)

        // 3. Populate Data into Views
        request?.let {
            tvName.text = "User: ${it.fullName}"
            tvAmount.text = "Amount: ₱${it.amount}"
            tvRfid.text = "RFID: ${it.rfidUid}"

            if (it.proof.isNotEmpty()) {
                Glide.with(this)
                    .load(it.proof)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgReceipt)
            }
        }

        // 4. Set Button Listeners
        btnApprove.setOnClickListener {
            updateRequest("processed", etRemarks.text.toString())
        }

        btnReject.setOnClickListener {
            val remarks = etRemarks.text.toString()
            if (remarks.isEmpty()) {
                etRemarks.error = "Please explain the rejection"
            } else {
                updateRequest("rejected", remarks)
            }
        }

        return view
    }

    /**
     * Updates the status in Firestore and notifies the user via the inbox collection.
     * Uses a Transaction to ensure balance and status are updated together.
     */
    private fun updateRequest(newStatus: String, remarks: String) {
        val req = request ?: return
        val requestRef = db.collection("load_topup").document(req.docId)
        val userRef = db.collection("users").document(req.userId)

        // Transaction ensures atomicity (all or nothing)
        db.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)

            // A. Update the top-up request status and admin remarks
            transaction.update(requestRef, "status", newStatus)
            transaction.update(requestRef, "remarks", remarks)

            // B. If approved, increment the user's current balance
            if (newStatus == "processed") {
                val currentBal = userSnap.getDouble("balance") ?: 0.0
                transaction.update(userRef, "balance", currentBal + req.amount)
            }

            // C. Create an Inbox Message for the user
            val inboxRef = db.collection("inbox").document() // Generates new ID

            val title = if (newStatus == "processed") "Top-up Approved! ✅" else "Top-up Rejected ❌"
            val messageBody = if (newStatus == "processed") {
                "Your top-up of ₱${req.amount} was successful. Your balance has been updated."
            } else {
                "Your top-up of ₱${req.amount} was rejected.\n\nReason: $remarks"
            }

            val notification = hashMapOf(
                "userId" to req.userId,
                "title" to title,
                "message" to messageBody,
                "timestamp" to FieldValue.serverTimestamp(),
                "isRead" to false,
                "type" to "topup_update" // Matches the Orange color logic in Adapter
            )

            // Schedule the inbox document creation
            transaction.set(inboxRef, notification)

            null // Transaction must return a value or null
        }.addOnSuccessListener {
            Toast.makeText(context, "Request updated and User notified!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // Return to the list
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}