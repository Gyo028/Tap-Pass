package com.example.tap_pass

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SpendingFragment : Fragment(R.layout.fragment_spending) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val handler = Handler(Looper.getMainLooper())
    private var totalAllowedSeconds: Long = 0
    private var startTimeMillis: Long = 0

    private val countdownRunnable = object : Runnable {
        override fun run() {
            val elapsedMillis = System.currentTimeMillis() - startTimeMillis
            val elapsedSeconds = elapsedMillis / 1000
            val remainingSeconds = totalAllowedSeconds - elapsedSeconds

            val timerText = view?.findViewById<TextView>(R.id.timerText)
            val statusText = view?.findViewById<TextView>(R.id.pcStatusText)

            if (remainingSeconds > 0) {
                val h = remainingSeconds / 3600
                val m = (remainingSeconds % 3600) / 60
                val s = remainingSeconds % 60
                timerText?.text = String.format("%02d:%02d:%02d", h, m, s)

                if (remainingSeconds < 60) {
                    timerText?.setTextColor(Color.RED)
                } else {
                    timerText?.setTextColor(Color.WHITE)
                }
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

        // Debug Log: Check if the Fragment started
        println("DEBUG: SpendingFragment started for user: $userId")
        // Put this right before your listener
        Log.d("TIMER_TEST", "Step 1: Listener is being initialized...")

        val query = db.collection("users").document(userId).collection("sessions")
            .whereEqualTo("status", "STARTED")

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("TIMER_TEST", "Step 2 ERROR: Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                Log.d("TIMER_TEST", "Step 2 SUCCESS: Found ${snapshots.size()} active sessions!")
                // Your countdown logic here...
            } else {
                Log.d("TIMER_TEST", "Step 2 INFO: No active session found in Firestore.")
            }
        }

        db.collection("users").document(userId).collection("sessions")
            .whereEqualTo("status", "STARTED")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("DEBUG: Firestore Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents[0]
                    val startTime = doc.getTimestamp("startTime")?.toDate()?.time ?: System.currentTimeMillis()
                    val pcName = doc.getString("pcNumber") ?: "PC"

                    println("DEBUG: Found STARTED session on $pcName")

                    // Fetch Balance from the USER document
                    db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                        val balance = userDoc.getDouble("balance") ?: 0.0
                        println("DEBUG: User Balance found: $balance")

                        initializeCountdown(balance, startTime, pcName)
                    }
                } else {
                    println("DEBUG: No session with status STARTED found")
                    stopTimer()
                    view.findViewById<TextView>(R.id.pcStatusText).text = "PC OFFLINE"
                    view.findViewById<TextView>(R.id.timerText).text = "00:00:00"
                }
            }
    }

    private fun initializeCountdown(balance: Double, serverStartTime: Long, pcName: String) {
        stopTimer() // Reset any existing timer first
        val hourlyRate = 20.0
        totalAllowedSeconds = ((balance / hourlyRate) * 3600).toLong()
        startTimeMillis = serverStartTime

        val statusText = view?.findViewById<TextView>(R.id.pcStatusText)
        statusText?.text = "$pcName ACTIVE"
        statusText?.setTextColor(Color.GREEN)

        handler.post(countdownRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(countdownRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}