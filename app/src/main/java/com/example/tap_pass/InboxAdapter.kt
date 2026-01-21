package com.example.tap_pass

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class InboxAdapter(private var messages: List<InboxMessage>) :
    RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.msgTitle)
        val body: TextView = view.findViewById(R.id.msgBody)
        val time: TextView = view.findViewById(R.id.msgTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]

        holder.title.text = msg.title
        holder.body.text = msg.message

        // --- ORANGE COLOR LOGIC ---
        if (msg.type == "LOAD") {
            holder.title.setTextColor(Color.parseColor("#FF9800"))
        } else {
            holder.title.setTextColor(Color.WHITE)
        }

        // --- READ/UNREAD STYLE ---
        if (!msg.isRead) {
            holder.title.setTypeface(null, Typeface.BOLD)
            holder.body.setTextColor(Color.WHITE)
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL)
            holder.body.setTextColor(Color.parseColor("#888888"))
        }

        // --- TIMESTAMP ---
        val date = msg.timestamp?.toDate()
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            holder.time.text = sdf.format(date)
        }
    }

    override fun getItemCount() = messages.size

    // ADD THIS FUNCTION: It tells the adapter to refresh the UI
    fun updateList(newList: List<InboxMessage>) {
        this.messages = newList
        notifyDataSetChanged()
    }
}