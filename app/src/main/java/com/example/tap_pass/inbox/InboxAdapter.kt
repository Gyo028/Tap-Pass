package com.example.tap_pass.inbox

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
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

        // --- COLOR LOGIC ---
        // "topup_update" is the type sent by the Admin Transaction
        if (msg.type == "LOAD" || msg.type == "topup_update") {
            holder.title.setTextColor(Color.parseColor("#FF9800")) // TapPass Orange
        } else {
            holder.title.setTextColor(Color.WHITE)
        }

        // --- READ/UNREAD STYLE ---
        // This is where the @PropertyName("isRead") in your Data Class works its magic
        if (!msg.isRead) {
            holder.title.setTypeface(null, Typeface.BOLD)
            holder.body.setTextColor(Color.WHITE)
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL)
            holder.body.setTextColor(Color.parseColor("#888888")) // Muted Gray for read messages
        }

        // --- TIMESTAMP ---
        val date = msg.timestamp
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            holder.time.text = sdf.format(date)
        } else {
            holder.time.text = "Just now"
        }
    }

    override fun getItemCount() = messages.size

    fun updateList(newList: List<InboxMessage>) {
        this.messages = newList
        notifyDataSetChanged()
    }
}