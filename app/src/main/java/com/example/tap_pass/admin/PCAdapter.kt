package com.example.tap_pass.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R

class PCAdapter(private val pcList: List<PCUnit>) : RecyclerView.Adapter<PCAdapter.PCViewHolder>() {

    class PCViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.pcCardView)
        val nameText: TextView = view.findViewById(R.id.pcNameText)
        val statusText: TextView = view.findViewById(R.id.pcStatusText)
        val icon: ImageView = view.findViewById(R.id.pcIcon)
        val userFullNameText: TextView = view.findViewById(R.id.userFullNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PCViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pc_grid, parent, false)
        return PCViewHolder(view)
    }

    override fun onBindViewHolder(holder: PCViewHolder, position: Int) {
        val pc = pcList[position]
        holder.nameText.text = pc.docId

        if (pc.status == "BUSY" || pc.status == "OCCUPIED") {
            holder.statusText.text = "OCCUPIED"
            holder.card.setCardBackgroundColor(Color.parseColor("#FFD700")) // Gold

            holder.statusText.setTextColor(Color.BLACK)
            holder.nameText.setTextColor(Color.BLACK)
            holder.icon.setColorFilter(Color.BLACK)

            holder.userFullNameText.visibility = View.VISIBLE
            holder.userFullNameText.text = pc.userFullName
            holder.userFullNameText.setTextColor(Color.BLACK)
        } else {
            holder.statusText.text = "AVAILABLE"
            holder.card.setCardBackgroundColor(Color.parseColor("#1E1E1E")) // Dark

            holder.statusText.setTextColor(Color.parseColor("#4CAF50")) // Green
            holder.nameText.setTextColor(Color.WHITE)
            holder.icon.setColorFilter(Color.parseColor("#4CAF50"))

            holder.userFullNameText.visibility = View.GONE
        }
    }

    override fun getItemCount() = pcList.size
}