package com.example.tap_pass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class AdminRequestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_requests)

        val requestsRecyclerView: RecyclerView = findViewById(R.id.requestsRecyclerView)
        requestsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Create a hardcoded list of requests for demonstration
        val requestList = ArrayList<Request>()
        requestList.add(Request("user1_id", "John Doe"))
        requestList.add(Request("user2_id", "Jane Smith"))
        requestList.add(Request("user3_id", "Peter Pan"))

        val adapter = RequestAdapter(this, requestList)
        requestsRecyclerView.adapter = adapter
    }
}
