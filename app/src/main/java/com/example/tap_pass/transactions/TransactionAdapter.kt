package com.example.tap_pass.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<com.example.tap_pass.transactions.Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtType: TextView = view.findViewById(R.id.txtType)
        val txtRfid: TextView = view.findViewById(R.id.txtRfid)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtAmount: TextView = view.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]

        // Name/RFID Display
        if (!tx.otherUserName.isNullOrEmpty()) {
            holder.txtRfid.text = tx.otherUserName
        } else {
            holder.txtRfid.text = "RFID: ${tx.otherUserRfid}"
        }

        holder.txtType.text = tx.type

        // --- UPDATED DATE & TIME FORMAT ---
        val date = tx.createdAt?.toDate()
        if (date != null) {
            // "MMM dd, yyyy" = Jan 20, 2026
            // "hh:mm a" = 09:15 AM
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            holder.txtDate.text = sdf.format(date)
        } else {
            holder.txtDate.text = "Just now"
        }

        // Amount and Color Logic
        if (tx.type == "RECEIVE") {
            holder.txtAmount.text = "+ ₱${String.format("%.2f", tx.amount)}"
            holder.txtAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.txtAmount.text = "- ₱${String.format("%.2f", tx.amount)}"
            holder.txtAmount.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount() = transactions.size
}