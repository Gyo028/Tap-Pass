package com.example.tap_pass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(private val historyList: List<SessionHistory>) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pcName: TextView = view.findViewById(R.id.itemPcName)
        val startTime: TextView = view.findViewById(R.id.itemStartTime)
        val endTime: TextView = view.findViewById(R.id.itemEndTime)
        val duration: TextView = view.findViewById(R.id.itemDuration)
        val cost: TextView = view.findViewById(R.id.itemCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sessions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = historyList[position]
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

        holder.pcName.text = session.pcNumber

        // 1. Format Start and End Times
        val startStr = session.startTime?.toDate()?.let { sdf.format(it) } ?: "N/A"
        val endStr = session.endTime?.toDate()?.let { sdf.format(it) } ?: "N/A"
        holder.startTime.text = "Start: $startStr"
        holder.endTime.text = "End: $endStr"

        // 2. Calculate and Format Duration (HH:mm:ss)
        if (session.startTime != null && session.endTime != null) {
            val diffInMs = session.endTime.toDate().time - session.startTime.toDate().time
            val seconds = (diffInMs / 1000) % 60
            val minutes = (diffInMs / (1000 * 60)) % 60
            val hours = (diffInMs / (1000 * 60 * 60))
            holder.duration.text = String.format("Duration: %02d:%02d:%02d", hours, minutes, seconds)
        } else {
            holder.duration.text = "Duration: N/A"
        }

        // 3. Format Cost
        holder.cost.text = "₱${String.format("%.2f", session.totalCost)}"
    }

    override fun getItemCount() = historyList.size
}