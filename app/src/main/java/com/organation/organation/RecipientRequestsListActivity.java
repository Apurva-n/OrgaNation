package com.organation.organation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RecipientRequestsListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private RecyclerView rvRequests;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private RequestAdapter requestAdapter;
    private List<RequestModel> requestList;
    
    private String recipientUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_requests_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            recipientUid = mAuth.getCurrentUser().getUid();
        }
        
        initializeViews();
        setupRecyclerView();
        loadRecipientRequests();
    }

    private void initializeViews() {
        rvRequests = findViewById(R.id.rvRequests);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        
        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Organ Requests");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestList, "recipient");
        
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);
        
        // Set click listener for request items
        requestAdapter.setOnItemClickListener(position -> {
            RequestModel request = requestList.get(position);
            Intent intent = new Intent(this, RequestDetailsActivity.class);
            intent.putExtra("requestId", request.getRequestId());
            intent.putExtra("userType", "recipient");
            startActivity(intent);
        });
    }

    private void loadRecipientRequests() {
        db.collection("organ_requests")
                .whereEqualTo("recipientUid", recipientUid)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                        return;
                    }
                    
                    requestList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RequestModel request = documentToRequest(document);
                        if (request != null) {
                            requestList.add(request);
                        }
                    }
                    
                    // Sort by date (newest first)
                    requestList.sort((r1, r2) -> r2.getRequestDate().compareTo(r1.getRequestDate()));
                    
                    requestAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    // Show message only on initial load
                    if (queryDocumentSnapshots.getMetadata().isFromCache()) {
                        Toast.makeText(this, "Loaded " + requestList.size() + " requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyState() {
        if (requestList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvRequests.setVisibility(View.GONE);
            tvEmptyMessage.setText("You haven't made any organ requests yet.\n\nClick 'New Request' to get started.");
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvRequests.setVisibility(View.VISIBLE);
        }
    }

    private RequestModel documentToRequest(QueryDocumentSnapshot document) {
        try {
            RequestModel request = new RequestModel();
            
            // Safe string retrieval with null checks
            request.setRequestId(getSafeString(document, "requestId"));
            request.setRecipientUid(getSafeString(document, "recipientUid"));
            request.setRecipientName(getSafeString(document, "recipientName"));
            request.setRecipientAadhaar(getSafeString(document, "recipientAadhaar"));
            request.setOrganType(getSafeString(document, "organType"));
            request.setBloodType(getSafeString(document, "bloodType"));
            request.setUrgency(getSafeString(document, "urgency"));
            request.setHospitalName(getSafeString(document, "hospitalName"));
            request.setHospitalCity(getSafeString(document, "hospitalCity"));
            request.setHospitalLocation(getSafeString(document, "hospitalLocation"));
            request.setTreatingDoctor(getSafeString(document, "treatingDoctor"));
            request.setMedicalDetails(getSafeString(document, "medicalDetails"));
            request.setAdditionalNotes(getSafeString(document, "additionalNotes"));
            request.setRequestDate(getSafeString(document, "requestDate"));
            request.setStatus(getSafeString(document, "status"));
            request.setHospitalNotes(getSafeString(document, "hospitalNotes"));
            request.setApprovedDate(getSafeString(document, "approvedDate"));
            request.setProcessedDate(getSafeString(document, "processedDate"));
            request.setCompletedDate(getSafeString(document, "completedDate"));
            request.setDeclinedDate(getSafeString(document, "declinedDate"));
            request.setDeclinedReason(getSafeString(document, "declinedReason"));
            
            // Validate essential fields
            if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
                return null;
            }
            
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSafeString(QueryDocumentSnapshot document, String fieldName) {
        try {
            String value = document.getString(fieldName);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
