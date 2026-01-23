package com.example.tap_pass

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class SpendingFragment : Fragment(R.layout.fragment_spending) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val handler = Handler(Looper.getMainLooper())
    private var totalAllowedSeconds: Long = 0
    private var startTimeMillis: Long = 0
    private val hourlyRate = 20.0

    private lateinit var adapter: SessionAdapter
    private val historyList = mutableListOf<SessionHistory>()

    private val countdownRunnable = object : Runnable {
        override fun run() {
            val currentMillis = System.currentTimeMillis()
            val elapsedSeconds = (currentMillis - startTimeMillis) / 1000
            val remainingSeconds = totalAllowedSeconds - elapsedSeconds

            val timerText = view?.findViewById<TextView>(R.id.timerText)
            val statusText = view?.findViewById<TextView>(R.id.pcStatusText)
            val costText = view?.findViewById<TextView>(R.id.itemEstimatedCost)

            // Calculation: (Hourly Rate / 3600 seconds) * seconds passed
            val estimatedCost = elapsedSeconds * (hourlyRate / 3600.0)

            if (remainingSeconds > 0) {
                val h = remainingSeconds / 3600
                val m = (remainingSeconds % 3600) / 60
                val s = remainingSeconds % 60

                timerText?.text = String.format("%02d:%02d:%02d", h, m, s)
                timerText?.setTextColor(if (remainingSeconds < 60) Color.RED else Color.WHITE)

                // Show running cost updating every second
                costText?.text = String.format("Est. Cost: ₱%.2f", estimatedCost)

                handler.postDelayed(this, 1000)
            } else {
                timerText?.text = "00:00:00"
                statusText?.text = "SESSION EXPIRED"
                statusText?.setTextColor(Color.RED)
                stopTimer()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = auth.currentUser?.uid ?: return

        setupRecyclerView(view)
        listenForActiveSession(userId)
        listenForPastSessions(userId)

        // Live Balance Listener
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            val balance = snapshot?.getDouble("balance") ?: 0.0
            view.findViewById<TextView>(R.id.currentBalanceText)?.text = String.format("Balance: ₱%.2f", balance)
        }
    }

    private fun listenForActiveSession(userId: String) {
        db.collection("users").document(userId).collection("sessions")
            .whereEqualTo("status", "STARTED")
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents[0]
                    // Use Server Time to keep sync with Python
                    startTimeMillis = doc.getTimestamp("startTime")?.toDate()?.time ?: System.currentTimeMillis()
                    val pcName = doc.getString("pcNumber") ?: "PC"

                    db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                        val balance = userDoc.getDouble("balance") ?: 0.0
                        totalAllowedSeconds = ((balance / hourlyRate) * 3600).toLong()

                        view?.findViewById<TextView>(R.id.pcStatusText)?.text = "$pcName ACTIVE"
                        view?.findViewById<TextView>(R.id.pcStatusText)?.setTextColor(Color.GREEN)
                        handler.post(countdownRunnable)
                    }
                } else {
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
                        historyList.add(doc.toObject(SessionHistory::class.java))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun resetUI() {
        view?.findViewById<TextView>(R.id.pcStatusText)?.text = "PC OFFLINE"
        view?.findViewById<TextView>(R.id.pcStatusText)?.setTextColor(Color.GRAY)
        view?.findViewById<TextView>(R.id.timerText)?.text = "00:00:00"
        view?.findViewById<TextView>(R.id.itemEstimatedCost)?.text = "Est. Cost: ₱0.00"
    }

    private fun stopTimer() { handler.removeCallbacks(countdownRunnable) }
    override fun onDestroyView() { super.onDestroyView(); stopTimer() }
}