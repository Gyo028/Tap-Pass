package com.example.tap_pass

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TransactionsActivity : AppCompatActivity() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions) // Make sure this matches your XML name

        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        val backButton: ImageView = findViewById(R.id.backButton)
        recyclerView = findViewById(R.id.transactionsRecyclerView)

        backButton.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchTransactions()
    }

    private fun fetchTransactions() {
        val userId = auth.currentUser?.uid ?: return

        fstore.collection("users").document(userId)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                recyclerView.adapter = TransactionAdapter(list)
            }
    }
}