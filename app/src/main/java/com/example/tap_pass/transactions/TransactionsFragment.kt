package com.example.tap_pass.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TransactionsFragment : Fragment() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup Back Button (if your fragment layout has one)
        view.findViewById<ImageView>(R.id.backButton)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        fetchTransactions()

        return view
    }

    private fun fetchTransactions() {
        val currentUserId = auth.currentUser?.uid ?: return

        fstore.collection("users")
            .document(currentUserId)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // If there's an error (like permission denied), this prevents a crash
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // ✅ FORCE Kotlin to use YOUR Transaction data class, not Firebase's
                    val transactionList = snapshot.toObjects(Transaction::class.java)

                    // Only set the adapter if the fragment is still attached to the screen
                    if (isAdded) {
                        recyclerView.adapter = TransactionAdapter(transactionList)
                    }
                }
            }
    }
}
