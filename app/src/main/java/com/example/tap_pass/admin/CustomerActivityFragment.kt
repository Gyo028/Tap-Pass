package com.example.tap_pass.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CustomerActivityFragment : Fragment() {

    private lateinit var adapter: CustomerActivityAdapter
    private val activityList = mutableListOf<CustomerActivity>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_customer_activity, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.customerActivityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CustomerActivityAdapter(activityList)
        recyclerView.adapter = adapter

        listenToSessionsRealtime()
        return view
    }

    private fun listenToSessionsRealtime() {
        // Use collectionGroup to listen to all "sessions" subcollections
        db.collectionGroup("sessions")
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->

                // Check for Errors (Like missing Indexes)
                if (e != null) {
                    Log.e("AdminHome", "Firestore Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Clear list to handle the fresh snapshot
                    activityList.clear()

                    for (doc in snapshots.documents) {
                        val session = doc.toObject(CustomerActivity::class.java)

                        if (session != null) {
                            // Fetch the User's name from the parent document
                            doc.reference.parent.parent?.get()?.addOnSuccessListener { userDoc ->
                                session.fullName = userDoc.getString("fullName") ?: "Unknown User"

                                // Prevent duplicate entries and update UI
                                if (!activityList.any { it.startTime == session.startTime && it.fullName == session.fullName }) {
                                    activityList.add(session)
                                    // Keep sorted by time
                                    activityList.sortByDescending { it.startTime }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
    }
}