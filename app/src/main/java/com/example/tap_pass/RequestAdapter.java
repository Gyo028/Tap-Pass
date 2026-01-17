package com.example.tap_pass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.requesterName.setText(request.getUserName());
        holder.requestStatus.setText(request.getStatus());

        // Set a click listener for the send button
        holder.sendButton.setOnClickListener(v -> {
            String amount = holder.amountToSend.getText().toString();
            if (amount.isEmpty()) {
                Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            // In a real app, you would send this data to a server or database.
            String message = "Sent " + amount + " to " + request.getUserName();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView requesterName;
        TextView requestStatus;
        EditText amountToSend;
        Button sendButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterName = itemView.findViewById(R.id.requesterName);
            requestStatus = itemView.findViewById(R.id.requestStatus);
            amountToSend = itemView.findViewById(R.id.amountToSend);
            sendButton = itemView.findViewById(R.id.sendButton);
        }
    }
}
