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

class CustomerActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_activity, container, false)

        val customerActivityRecyclerView: RecyclerView = view.findViewById(R.id.customerActivityRecyclerView)
        val placeholder: LinearLayout = view.findViewById(R.id.placeholder_activity)
        customerActivityRecyclerView.layoutManager = LinearLayoutManager(context)

        // This is where you would fetch your data and populate an adapter.
        // For now, we can use a placeholder list.
        val activityList = ArrayList<Any>() // Replace 'Any' with your data model
        // activityList.add(...) // Example of how to add an activity

        if (activityList.isEmpty()) {
            customerActivityRecyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        } else {
            customerActivityRecyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
            // val adapter = CustomerActivityAdapter(requireContext(), activityList)
            // customerActivityRecyclerView.adapter = adapter
        }

        return view
    }
}
