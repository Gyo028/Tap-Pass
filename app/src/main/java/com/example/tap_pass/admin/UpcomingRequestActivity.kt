package com.example.tap_pass.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UpcomingRequestsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RequestAdapter
    private val requestList = mutableListOf<TopUpRequest>()
    private lateinit var placeholder: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure this layout contains a container with ID: nav_host_fragment
        setContentView(R.layout.activity_admin_requests)

        db = FirebaseFirestore.getInstance()
        placeholder = findViewById(R.id.placeholder_requests)

        val recyclerView = findViewById<RecyclerView>(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // FIXED: Only 2 arguments. The click logic is inside the Adapter now.
        adapter = RequestAdapter(this, requestList)
        recyclerView.adapter = adapter

        listenForRequests()
    }

    private fun listenForRequests() {
        db.collection("load_topup")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                requestList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    placeholder.visibility = View.GONE
                    for (doc in snapshots) {
                        val req = doc.toObject(TopUpRequest::class.java)?.copy(docId = doc.id)
                        if (req != null) requestList.add(req)
                    }
                } else {
                    placeholder.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
    }
}