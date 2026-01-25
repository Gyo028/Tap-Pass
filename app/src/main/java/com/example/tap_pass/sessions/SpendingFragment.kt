package com.example.tap_pass.sessions

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class SpendingFragment : Fragment(R.layout.fragment_spending) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val handler = Handler(Looper.getMainLooper())
    private var totalAllowedSeconds: Long = 0
    private var startTimeMillis: Long = 0
    private val hourlyRate = 20.0

    private var activeSessionId: String? = null
    private var activePcNumber: String? = null

    private lateinit var adapter: SessionAdapter
    private val historyList = mutableListOf<SessionHistory>()

    private val countdownRunnable = object : Runnable {
        override fun run() {
            val currentMillis = System.currentTimeMillis()
            val elapsedSeconds = (currentMillis - startTimeMillis) / 1000
            val remainingSeconds = totalAllowedSeconds - elapsedSeconds

            val timerText = view?.findViewById<TextView>(R.id.timerText)
            val costText = view?.findViewById<TextView>(R.id.itemEstimatedCost)

            // Live cost estimation for display
            val estimatedCost = elapsedSeconds * (hourlyRate / 3600.0)

            if (remainingSeconds > 0) {
                val h = remainingSeconds / 3600
                val m = (remainingSeconds % 3600) / 60
                val s = remainingSeconds % 60

                timerText?.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                timerText?.setTextColor(if (remainingSeconds < 60) Color.RED else Color.WHITE)
                costText?.text = String.format(Locale.getDefault(), "Est. Cost: ₱%.2f", estimatedCost)

                handler.postDelayed(this, 1000)
            } else {
                // Auto-logout if balance is exhausted
                timerText?.text = "00:00:00"
                executeLogoutProcess(auth.currentUser?.uid ?: "")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = auth.currentUser?.uid ?: return

        setupRecyclerView(view)
        listenForActiveSession(userId)
        listenForPastSessions(userId)

        view.findViewById<Button>(R.id.wirelessLogoutButton)?.setOnClickListener {
            if (activeSessionId != null) {
                showLogoutConfirmation(userId)
            } else {
                Toast.makeText(context, "No active session found", Toast.LENGTH_SHORT).show()
            }
        }

        // Real-time balance listener for the user document
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            val balance = snapshot?.getDouble("balance") ?: 0.0
            view.findViewById<TextView>(R.id.currentBalanceText)?.text =
                String.format(Locale.getDefault(), "Balance: ₱%.2f", balance)
        }
    }

    private fun showLogoutConfirmation(userId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to end your session? This will deduct the total cost from your balance.")
            .setPositiveButton("Logout") { _, _ -> executeLogoutProcess(userId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeLogoutProcess(userId: String) {
        val sId = activeSessionId ?: return
        val pNum = activePcNumber ?: return

        // Calculate final cost
        val elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
        val finalCost = elapsedSeconds * (hourlyRate / 3600.0)

        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val currentBalance = userDoc.getDouble("balance") ?: 0.0
            val newBalance = currentBalance - finalCost

            // Use a Batch Write to ensure all fields update together
            val batch = db.batch()
            val userRef = db.collection("users").document(userId)
            val sessionRef = userRef.collection("sessions").document(sId)
            val pcRef = db.collection("pcs").document(pNum)

            // 1. Deduct balance
            batch.update(userRef, "balance", newBalance)

            // 2. Update session with totalCost and ENDED status
            batch.update(sessionRef, mapOf(
                "status" to "ENDED",
                "endTime" to FieldValue.serverTimestamp(),
                "totalCost" to finalCost
            ))

            // 3. Free the PC resource
            batch.update(pcRef, mapOf(
                "status" to "AVAILABLE",
                "currentUserId" to ""
            ))

            batch.commit().addOnSuccessListener {
                Toast.makeText(context, "Session Ended Successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenForActiveSession(userId: String) {
        db.collection("users").document(userId).collection("sessions")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val activeDoc = snapshots?.documents?.find { it.getString("status") == "STARTED" }

                if (activeDoc != null) {
                    activeSessionId = activeDoc.id
                    activePcNumber = activeDoc.getString("pcNumber")
                    startTimeMillis = activeDoc.getTimestamp("startTime")?.toDate()?.time ?: System.currentTimeMillis()

                    db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                        val balance = userDoc.getDouble("balance") ?: 0.0
                        totalAllowedSeconds = ((balance / hourlyRate) * 3600).toLong()

                        view?.findViewById<TextView>(R.id.pcStatusText)?.text = "${activePcNumber} ACTIVE"
                        view?.findViewById<TextView>(R.id.pcStatusText)?.setTextColor(Color.GREEN)

                        // --- PUT THIS EXACT LINE HERE ---
                        handler.removeCallbacks(countdownRunnable) // This stops the "phantom" timers
                        handler.post(countdownRunnable)           // This starts the clean timer
                        // --------------------------------
                    }
                } else {
                    activeSessionId = null
                    activePcNumber = null
                    stopTimer()
                    resetUI()
                }
            }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SessionAdapter(historyList)
        recyclerView.adapter = adapter
    }

    private fun listenForPastSessions(userId: String) {
        db.collection("users").document(userId).collection("sessions")
            .whereIn("status", listOf("ENDED", "COMPLETED"))
            .orderBy("endTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    historyList.clear()
                    for (doc in snapshots) {
                        val session = doc.toObject(SessionHistory::class.java)
                        session?.let { historyList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun resetUI() {
        view?.findViewById<TextView>(R.id.pcStatusText)?.apply {
            text = "PC OFFLINE"
            setTextColor(Color.GRAY)
        }
        view?.findViewById<TextView>(R.id.timerText)?.text = "00:00:00"
        view?.findViewById<TextView>(R.id.itemEstimatedCost)?.text = "Est. Cost: ₱0.00"
    }

    private fun stopTimer() { handler.removeCallbacks(countdownRunnable) }
    override fun onDestroyView() { super.onDestroyView(); stopTimer() }
}