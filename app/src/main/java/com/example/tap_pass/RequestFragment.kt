package com.example.tap_pass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class RequestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        val requestsRecyclerView: RecyclerView = view.findViewById(R.id.requestsRecyclerView)
        val placeholder: LinearLayout = view.findViewById(R.id.placeholder_requests)
        requestsRecyclerView.layoutManager = LinearLayoutManager(context)

        // This is where you would fetch your data and populate the adapter.
        // For now, we can use a placeholder list.
        val requestList = ArrayList<Request>()
        // requestList.add(Request(...)) // Example of how to add a request

        if (requestList.isEmpty()) {
            requestsRecyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        } else {
            requestsRecyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
            val adapter = RequestAdapter(requireContext(), requestList)
            requestsRecyclerView.adapter = adapter
        }

        return view
    }
}
