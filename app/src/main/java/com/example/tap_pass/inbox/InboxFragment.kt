package com.example.tap_pass.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
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

        recyclerView = view.findViewById(R.id.inboxRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize with the empty list
        adapter = InboxAdapter(messageList)
        recyclerView.adapter = adapter

        fetchMessages()

        return view
    }

    private fun fetchMessages() {
        val currentUser = auth.currentUser ?: return

        // Points to the root collection where the Admin saves notifications
        db.collection("inbox")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Check Logcat for a URL link if this fails!
                    Toast.makeText(context, "Inbox Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newMessages = snapshot.toObjects(InboxMessage::class.java)
                    adapter.updateList(newMessages)
                }
            }
    }
}