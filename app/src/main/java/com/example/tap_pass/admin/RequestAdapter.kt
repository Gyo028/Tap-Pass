package com.example.tap_pass.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tap_pass.R

class RequestAdapter(
    private val context: Context,
    private val requestList: List<TopUpRequest>
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgReceipt: ImageView = view.findViewById(R.id.imgReceipt)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtAmount: TextView = view.findViewById(R.id.txtAmount)
        val txtRfid: TextView = view.findViewById(R.id.txtRfid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_request_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestList[position]

        holder.txtName.text = "Name: ${request.fullName}"
        holder.txtAmount.text = "Amount: ₱${request.amount}"
        holder.txtRfid.text = "RFID: ${request.rfidUid}"

        // Load receipt preview using Glide
        if (request.proof.isNotEmpty()) {
            Glide.with(context)
                .load(request.proof)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgReceipt)
        }

        // REDIRECT LOGIC
        holder.itemView.setOnClickListener {
            val detailFragment = RequestDetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("selected_request", request)
            detailFragment.arguments = bundle

            if (context is AppCompatActivity) {
                // Using R.id.nav_host_fragment to match your RelativeLayout XML
                context.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.nav_host_fragment, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun getItemCount() = requestList.size
}