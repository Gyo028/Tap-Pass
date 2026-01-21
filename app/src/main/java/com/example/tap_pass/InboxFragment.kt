package com.example.tap_pass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class InboxFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InboxAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var messageList = mutableListOf<InboxMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        // 1. Setup RecyclerView
        recyclerView = view.findViewById(R.id.inboxRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 2. Initialize Adapter with an empty list
        adapter = InboxAdapter(messageList)
        recyclerView.adapter = adapter

        // 3. Start fetching data
        fetchMessages()

        return view
    }

    private fun fetchMessages() {
        val currentUser = auth.currentUser ?: return

        // Look inside users -> [UID] -> inbox
        db.collection("users").document(currentUser.uid)
            .collection("inbox")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Newest on top
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Convert Firestore documents directly into our InboxMessage list
                    val newMessages = snapshot.toObjects(InboxMessage::class.java)

                    // Update the adapter with the fresh data
                    adapter.updateList(newMessages)
                }
            }
    }
}