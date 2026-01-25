package com.example.tap_pass.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import java.text.SimpleDateFormat
import java.util.*

class CustomerActivityAdapter(private val activityList: List<CustomerActivity>) :
    RecyclerView.Adapter<CustomerActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.customerNameTextView)
        val pcNum: TextView = view.findViewById(R.id.pcNumberTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
        val startTimeText: TextView = view.findViewById(R.id.startTimeTextView)
        val startDateText: TextView = view.findViewById(R.id.startDateTextView)
        val endTimeText: TextView = view.findViewById(R.id.endTimeTextView)
        val durationText: TextView = view.findViewById(R.id.durationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activityList[position]
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        holder.name.text = activity.fullName
        holder.pcNum.text = "PC Unit: ${activity.pcNumber}"

        // 1. Handle START Time (Always present if document exists)
        activity.startTime?.let {
            holder.startTimeText.text = "Start: ${timeFormat.format(it.toDate())}"
            holder.startDateText.text = dateFormat.format(it.toDate())
        }

        // 2. Handle Logic Based on YOUR Status strings
        when (activity.status) {
            "STARTED" -> {
                holder.status.text = "ACTIVE"
                holder.status.setTextColor(Color.parseColor("#4CAF50")) // Green

                holder.endTimeText.text = "End: --:--"
                holder.durationText.text = "Total: -- mins"
            }
            "ENDED" -> {
                holder.status.text = "FINISHED"
                holder.status.setTextColor(Color.parseColor("#FF5252")) // Red

                // Safety check for the endTime timestamp
                if (activity.endTime != null) {
                    val endObj = activity.endTime.toDate()
                    holder.endTimeText.text = "End: ${timeFormat.format(endObj)}"

                    // Duration Calculation
                    val diffMs = endObj.time - (activity.startTime?.toDate()?.time ?: endObj.time)
                    val diffMins = diffMs / (1000 * 60)
                    holder.durationText.text = "Total: $diffMins mins"
                } else {
                    holder.endTimeText.text = "End: Error"
                    holder.durationText.text = "Total: --"
                }
            }
        }
    }

    override fun getItemCount() = activityList.size
}