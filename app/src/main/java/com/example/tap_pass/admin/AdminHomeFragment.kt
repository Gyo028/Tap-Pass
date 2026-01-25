package com.example.tap_pass.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

class AdminHomeFragment : Fragment() {

    private lateinit var fstore: FirebaseFirestore
    private var earningsListener: ListenerRegistration? = null
    private var pcListener: ListenerRegistration? = null

    private lateinit var earningsAmountText: TextView
    private lateinit var pcRecyclerView: RecyclerView
    private lateinit var pcAdapter: PCAdapter
    private val pcList = mutableListOf<PCUnit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use fragment_admin_home to include the RecyclerView
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        fstore = FirebaseFirestore.getInstance()
        earningsAmountText = view.findViewById(R.id.earningsAmountText)
        pcRecyclerView = view.findViewById(R.id.pcRecyclerView)

        setupPCGrid()
        listenToEarnings()
        listenToPCStatus()

        return view
    }

    private fun setupPCGrid() {
        pcRecyclerView.layoutManager = GridLayoutManager(context, 2)
        pcAdapter = PCAdapter(pcList)
        pcRecyclerView.adapter = pcAdapter
    }

    private fun listenToEarnings() {
        // Make sure "status" and "processed" match your Firestore exactly (case-sensitive)
        earningsListener = fstore.collection("load_topup")
            .whereEqualTo("status", "processed")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || view == null) return@addSnapshotListener

                if (error != null) {
                    Log.e("AdminHome", "Earnings Error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var totalSum = 0.0
                    for (doc in snapshot.documents) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        totalSum += amount
                    }

                    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                    earningsAmountText.text = format.format(totalSum)
                }
            }
    }

    private fun listenToPCStatus() {
        pcListener = fstore.collection("pcs")
            .addSnapshotListener { snapshot, error ->
                // Safety check: ensure fragment is still attached to UI
                if (!isAdded || view == null) return@addSnapshotListener

                if (error != null) {
                    Log.e("AdminHome", "PC Status Error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    pcList.clear()
                    for (doc in snapshot.documents) {
                        val pc = doc.toObject(PCUnit::class.java)
                        pc?.let { unit ->
                            unit.docId = doc.id
                            pcList.add(unit)

                            // 1. Primary Trigger: Check if PC is not AVAILABLE
                            val isOccupied = unit.status == "BUSY" || unit.status == "OCCUPIED"
                            val userId = unit.currentUserId ?: ""

                            // 2. Decoupled Lookup: Only attempt fetch if userId is a valid document path
                            if (isOccupied && userId.isNotEmpty()) {
                                fstore.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        unit.userFullName = userDoc.getString("fullName") ?: "Unknown User"
                                        pcAdapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener {
                                        unit.userFullName = "Unknown User"
                                        pcAdapter.notifyDataSetChanged()
                                    }
                            } else if (isOccupied) {
                                // Status is busy but no ID found yet
                                unit.userFullName = "Unknown User"
                            } else {
                                // PC is AVAILABLE
                                unit.userFullName = ""
                            }
                        }
                    }
                    pcAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        earningsListener?.remove()
        pcListener?.remove()
    }
}