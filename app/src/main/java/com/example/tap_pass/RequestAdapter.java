package com.example.tap_pass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requestList;
    private Context context;

    public RequestAdapter(Context context, List<Request> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Corrected to use the admin request card layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);

        // Populate the views based on item_request_card.xml
        // Note: You will need to ensure your Request model class has getters for this data.
        holder.txtName.setText("Name: " + request.getUserName());
        holder.txtAmount.setText("Amount: ₱500.00"); // Placeholder - you can get this from the request object
        holder.txtRfid.setText("RFID: 123456789");   // Placeholder - you can get this from the request object

        // Use a placeholder for the receipt image
        holder.imgReceipt.setImageResource(android.R.drawable.ic_menu_gallery); // A standard placeholder

        // Set click listeners for the Send (approve) and Reject buttons
        holder.btnSend.setOnClickListener(v -> {
            String message = "Request for " + request.getUserName() + " approved.";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            // Logic to handle approval would go here
        });

        holder.btnReject.setOnClickListener(v -> {
            String message = "Request for " + request.getUserName() + " rejected.";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            // Logic to handle rejection would go here
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    // ViewHolder updated to match the views in item_request_card.xml
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReceipt;
        TextView txtName;
        TextView txtAmount;
        TextView txtRfid;
        Button btnSend;
        Button btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceipt = itemView.findViewById(R.id.imgReceipt);
            txtName = itemView.findViewById(R.id.txtName);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtRfid = itemView.findViewById(R.id.txtRfid);
            btnSend = itemView.findViewById(R.id.btnSend);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
