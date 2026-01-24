package com.example.tap_pass.admin


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RequestFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RequestAdapter
    private val requestList = mutableListOf<TopUpRequest>()
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var placeholder: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        db = FirebaseFirestore.getInstance()
        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView)
        placeholder = view.findViewById(R.id.placeholder_requests)

        requestsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // FIXED: Only passing 2 arguments (Context and the List)
        // No lambda block here because the logic is now in RequestDetailFragment
        adapter = RequestAdapter(requireContext(), requestList)
        requestsRecyclerView.adapter = adapter

        listenForRequests()

        return view
    }

    private fun listenForRequests() {
        // We filter for 'pending' so processed requests disappear from the main list automatically
        db.collection("load_topup")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FIRESTORE", "Listen failed.", e)
                    return@addSnapshotListener
                }

                requestList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    requestsRecyclerView.visibility = View.VISIBLE
                    placeholder.visibility = View.GONE

                    for (doc in snapshots) {
                        // Using .copy() to inject the Firestore Document ID into our object
                        val req = doc.toObject(TopUpRequest::class.java)?.copy(docId = doc.id)
                        if (req != null) {
                            requestList.add(req)
                        }
                    }
                } else {
                    // Show the "No Pending Requests" placeholder
                    requestsRecyclerView.visibility = View.GONE
                    placeholder.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
    }
}