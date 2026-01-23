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

    // Timer Variables
    private val handler = Handler(Looper.getMainLooper())
    private var totalAllowedSeconds: Long = 0
    private var startTimeMillis: Long = 0

    // History Variables
    private lateinit var adapter: SessionAdapter
    private val historyList = mutableListOf<SessionHistory>()

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
                timerText?.setTextColor(if (remainingSeconds < 60) Color.RED else Color.WHITE)
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

        // 1. Setup RecyclerView for History
        setupRecyclerView(view)

        // 2. Start Active Session Listener (Your existing logic)
        listenForActiveSession(userId)

        // 3. Start History Listener (New recording logic)
        listenForPastSessions(userId)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SessionAdapter(historyList)
        recyclerView.adapter = adapter
    }

    private fun listenForActiveSession(userId: String) {
        db.collection("users").document(userId).collection("sessions")
            .whereEqualTo("status", "STARTED")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("TIMER_TEST", "Firestore Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents[0]
                    val startTime = doc.getTimestamp("startTime")?.toDate()?.time ?: System.currentTimeMillis()
                    val pcName = doc.getString("pcNumber") ?: "PC"

                    db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                        val balance = userDoc.getDouble("balance") ?: 0.0
                        initializeCountdown(balance, startTime, pcName)
                    }
                } else {
                    stopTimer()
                    view?.findViewById<TextView>(R.id.pcStatusText)?.text = "PC OFFLINE"
                    view?.findViewById<TextView>(R.id.pcStatusText)?.setTextColor(Color.GRAY)
                    view?.findViewById<TextView>(R.id.timerText)?.text = "00:00:00"
                }
            }
    }

    private fun listenForPastSessions(userId: String) {
        db.collection("users").document(userId).collection("sessions")
            .whereIn("status", listOf("ENDED", "COMPLETED"))
            .orderBy("endTime", Query.Direction.DESCENDING) // Newest on top
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("HISTORY_LOG", "History listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    historyList.clear()
                    for (doc in snapshots) {
                        val session = doc.toObject(SessionHistory::class.java)
                        historyList.add(session)
                    }
                    adapter.notifyDataSetChanged()
                    Log.d("HISTORY_LOG", "Loaded ${historyList.size} past sessions")
                }
            }
    }

    private fun initializeCountdown(balance: Double, serverStartTime: Long, pcName: String) {
        stopTimer()
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