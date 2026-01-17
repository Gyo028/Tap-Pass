package com.example.tap_pass;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UpcomingRequestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_requests);

        RecyclerView requestsRecyclerView = findViewById(R.id.requestsRecyclerView);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create placeholder data
        List<Request> requestList = new ArrayList<>();
        requestList.add(new Request("Juan Dela Cruz", "Pending"));
        requestList.add(new Request("Maria Santos", "Pending"));
        requestList.add(new Request("Alex Reyes", "Pending"));

        // In a real app, you would fetch this data from a database or API.

        RequestAdapter adapter = new RequestAdapter(this, requestList);
        requestsRecyclerView.setAdapter(adapter);
    }
}
