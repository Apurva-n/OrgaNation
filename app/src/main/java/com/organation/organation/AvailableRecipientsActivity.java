package com.organation.organation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailableRecipientsActivity extends AppCompatActivity {
    private RecyclerView rvRecipients;
    private RecipientAdapter recipientAdapter;
    private List<RecipientModel> recipientList;
    private List<RecipientModel> allRecipients;
    private FirebaseFirestore db;

    // Filters
    private String filterState = "";
    private String filterCity = "";
    private String filterOrgans = "";
    private String filterBloodGroup = "";
    private Integer filterMinHeight = null;
    private Integer filterMaxHeight = null;
    private Integer filterMinWeight = null;
    private Integer filterMaxWeight = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_recipients);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Recipients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvRecipients = findViewById(R.id.rvRecipients);
        recipientList = new ArrayList<>();
        allRecipients = new ArrayList<>();
        recipientAdapter = new RecipientAdapter(this, recipientList);

        rvRecipients.setLayoutManager(new LinearLayoutManager(this));
        rvRecipients.setAdapter(recipientAdapter);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.fabFilter).setOnClickListener(v -> showFilterDialog());

        loadRecipients();
    }

    private void loadRecipients() {
        allRecipients.clear();
        recipientList.clear();
        
        Log.d("RECIPIENT_LOAD", "=== STARTING TO LOAD RECIPIENTS ===");
        
        db.collection("Recepients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("RECIPIENT_LOAD", "Found " + queryDocumentSnapshots.size() + " recipient documents in Firebase");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("RECIPIENT_LOAD", "Processing recipient document: " + document.getId());
                        Log.d("RECIPIENT_LOAD", "Document data: " + document.getData());
                        
                        RecipientModel recipient = document.toObject(RecipientModel.class);
                        
                        // Fallback: Try manual parsing if automatic mapping fails
                        if (recipient.getFullName() == null || recipient.getFullName().trim().isEmpty()) {
                            Log.d("RECIPIENT_LOAD", "Automatic mapping failed, trying manual parsing");
                            recipient = parseDocumentToRecipient(document);
                        }
                        
                        // Debug: Check what fields are actually loaded
                        Log.d("RECIPIENT_LOAD", "RecipientModel - Aadhaar: " + recipient.getAadhaarNo());
                        Log.d("RECIPIENT_LOAD", "RecipientModel - Name: " + recipient.getFullName());
                        Log.d("RECIPIENT_LOAD", "RecipientModel - Age: " + recipient.getAge());
                        Log.d("RECIPIENT_LOAD", "RecipientModel - City: " + recipient.getCity());
                        Log.d("RECIPIENT_LOAD", "RecipientModel - Organs: " + recipient.getOrgansNeeded());
                        
                        if (recipient.getFullName() == null || recipient.getFullName().trim().isEmpty()) {
                            Log.w("RECIPIENT_LOAD", "Recipient has null or empty name - skipping");
                            continue;
                        }
                        
                        allRecipients.add(recipient);
                        Log.d("RECIPIENT_LOAD", "Added recipient to list: " + recipient.getFullName());
                    }
                    
                    Log.d("RECIPIENT_LOAD", "Total recipients loaded: " + allRecipients.size());
                    
                    // Now filter recipients asynchronously
                    filterRecipientsAsync();
                })
                .addOnFailureListener(e -> {
                    Log.e("RECIPIENT_LOAD", "Error fetching recipients from Firebase", e);
                    Toast.makeText(AvailableRecipientsActivity.this,
                            "Error fetching recipients: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Check if recipient is still active (not completed transplant)
     * Filters out recipients with completed requests or completed status
     * NOTE: Now properly implemented with async checking
     */
    private void isActiveRecipient(RecipientModel recipient, RecipientFilterCallback callback) {
        try {
            // Check if recipient has valid Aadhaar number
            String aadhaarNo = recipient.getAadhaarNo();
            if (aadhaarNo == null || aadhaarNo.trim().isEmpty()) {
                Log.d("RECIPIENT_FILTER", "Recipient " + recipient.getFullName() + " has no Aadhaar number - showing as active");
                callback.onResult(true); // Assume active if no Aadhaar
                return;
            }
            
            // Method 1: Check if this recipient has any completed organ requests
            db.collection("organ_requests")
                .whereEqualTo("recipientAadhaar", aadhaarNo)
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No completed requests - check recipient's own status
                        db.collection("Recepients").document(aadhaarNo)
                            .get()
                            .addOnSuccessListener(recipientDoc -> {
                                if (recipientDoc.exists()) {
                                    String status = recipientDoc.getString("status");
                                    String transplantStatus = recipientDoc.getString("transplantStatus");
                                    
                                    if (status != null && (status.equalsIgnoreCase("completed") || 
                                                          status.equalsIgnoreCase("inactive") || 
                                                          status.equalsIgnoreCase("transplanted"))) {
                                        Log.d("RECIPIENT_FILTER", "Recipient " + recipient.getFullName() + " has status: " + status + " - filtering out");
                                        callback.onResult(false); // Not active
                                        return;
                                    }
                                    
                                    if (transplantStatus != null && (transplantStatus.equalsIgnoreCase("completed") || 
                                                               transplantStatus.equalsIgnoreCase("inactive"))) {
                                        Log.d("RECIPIENT_FILTER", "Recipient " + recipient.getFullName() + " has transplantStatus: " + transplantStatus + " - filtering out");
                                        callback.onResult(false); // Not active
                                        return;
                                    }
                                }
                                
                                Log.d("RECIPIENT_FILTER", "Recipient " + recipient.getFullName() + " is active - showing in list");
                                callback.onResult(true); // Active
                            })
                            .addOnFailureListener(e -> {
                                Log.e("RECIPIENT_FILTER", "Error checking recipient document for " + recipient.getFullName(), e);
                                callback.onResult(true); // Assume active on error
                            });
                    } else {
                        // Has completed requests - filter out
                        Log.d("RECIPIENT_FILTER", "Recipient " + recipient.getFullName() + " has completed organ request - filtering out");
                        callback.onResult(false); // Not active
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RECIPIENT_FILTER", "Error checking organ requests for " + recipient.getFullName(), e);
                    callback.onResult(true); // Assume active on error
                });
        } catch (Exception e) {
            Log.e("RECIPIENT_FILTER", "Error checking recipient activity for " + recipient.getFullName(), e);
            callback.onResult(true); // Assume active on error
        }
    }
    
    // Callback interface for async recipient filtering
    private interface RecipientFilterCallback {
        void onResult(boolean isActive);
    }

    private void applyFilters() {
        recipientList.clear();
        for (RecipientModel recipient : allRecipients) {
            if (!matchesFilter(recipient)) continue;
            recipientList.add(recipient);
        }
        recipientAdapter.notifyDataSetChanged();
    }
    
    private void filterRecipientsAsync() {
        if (allRecipients.isEmpty()) {
            recipientAdapter.notifyDataSetChanged();
            return;
        }
        
        // Filter recipients one by one asynchronously
        final int[] processedCount = {0};
        final int totalCount = allRecipients.size();
        
        for (RecipientModel recipient : allRecipients) {
            isActiveRecipient(recipient, new RecipientFilterCallback() {
                @Override
                public void onResult(boolean isActive) {
                    processedCount[0]++;
                    
                    if (isActive && matchesFilter(recipient)) {
                        recipientList.add(recipient);
                    }
                    
                    // Check if all recipients have been processed
                    if (processedCount[0] == totalCount) {
                        runOnUiThread(() -> {
                            recipientAdapter.notifyDataSetChanged();
                            Toast.makeText(AvailableRecipientsActivity.this,
                                    "Found " + recipientList.size() + " active recipients",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private boolean matchesFilter(RecipientModel recipient) {
        if (!filterState.isEmpty() && !recipient.getState().toLowerCase().contains(filterState.toLowerCase())) {
            return false;
        }
        if (!filterCity.isEmpty() && !recipient.getCity().toLowerCase().contains(filterCity.toLowerCase())) {
            return false;
        }
        if (!filterBloodGroup.isEmpty() && !recipient.getBloodGroup().toLowerCase().contains(filterBloodGroup.toLowerCase())) {
            return false;
        }
        if (!filterOrgans.isEmpty()) {
            String organs = recipient.getOrgansNeeded();
            if (organs == null || !organs.toLowerCase().contains(filterOrgans.toLowerCase())) {
                return false;
            }
        }

        if (filterMinHeight != null) {
            try {
                int height = Integer.parseInt(recipient.getHeight());
                if (height < filterMinHeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMaxHeight != null) {
            try {
                int height = Integer.parseInt(recipient.getHeight());
                if (height > filterMaxHeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMinWeight != null) {
            try {
                int weight = Integer.parseInt(recipient.getWeight());
                if (weight < filterMinWeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMaxWeight != null) {
            try {
                int weight = Integer.parseInt(recipient.getWeight());
                if (weight > filterMaxWeight) return false;
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    private RecipientModel parseDocumentToRecipient(QueryDocumentSnapshot document) {
        try {
            Map<String, Object> data = document.getData();

            RecipientModel recipient = new RecipientModel();
            recipient.setAadhaarNo(getString(data, "02]Aadhaar_no"));
            recipient.setFullName(getString(data, "01]Full_name"));
            recipient.setAge(getString(data, "04]Age"));
            recipient.setGender(getString(data, "05]Gender"));
            recipient.setBloodGroup(getString(data, "06]Blood_group"));
            recipient.setHeight(getString(data, "07]Height"));
            recipient.setWeight(getString(data, "08]Weight"));
            recipient.setPhone(getString(data, "09]Phone"));
            recipient.setEmail(getString(data, "10]Email"));
            recipient.setState(getString(data, "11]State"));
            recipient.setCity(getString(data, "12]City"));
            recipient.setStreet(getString(data, "13]Street"));
            recipient.setLandmark(getString(data, "14]Landmark"));
            recipient.setOrgansNeeded(getString(data, "16]Organs_to_donate"));
            recipient.setUrgency(getString(data, "17]Urgency"));

            // Parse hospital details
            if (data.containsKey("15]Hospital_details")) {
                Map<String, Object> hospitalMap = (Map<String, Object>) data.get("15]Hospital_details");
                if (hospitalMap != null) {
                    Map<String, String> hospital = new HashMap<>();
                    hospital.put("01]Hospital Name", getString(hospitalMap, "01]Hospital Name"));
                    hospital.put("02]Doctor Name", getString(hospitalMap, "02]Doctor Name"));
                    hospital.put("03]Hospital state", getString(hospitalMap, "03]Hospital state"));
                    hospital.put("04]Hospital city", getString(hospitalMap, "04]Hospital city"));
                    hospital.put("05]Hospital street", getString(hospitalMap, "05]Hospital street"));
                    hospital.put("06]Hospital landmark", getString(hospitalMap, "06]Hospital landmark"));
                    recipient.setHospitalDetails(hospital);
                }
            }

            return recipient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "N/A";
    }

    private void showFilterDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_filter_recipients, null);

        android.widget.EditText etState = view.findViewById(R.id.etFilterState);
        android.widget.EditText etCity = view.findViewById(R.id.etFilterCity);
        android.widget.EditText etOrgans = view.findViewById(R.id.etFilterOrgans);
        android.widget.EditText etBloodGroup = view.findViewById(R.id.etFilterBloodGroup);
        android.widget.EditText etMinHeight = view.findViewById(R.id.etMinHeight);
        android.widget.EditText etMaxHeight = view.findViewById(R.id.etMaxHeight);
        android.widget.EditText etMinWeight = view.findViewById(R.id.etMinWeight);
        android.widget.EditText etMaxWeight = view.findViewById(R.id.etMaxWeight);

        // Populate existing filters
        etState.setText(filterState);
        etCity.setText(filterCity);
        etOrgans.setText(filterOrgans);
        etBloodGroup.setText(filterBloodGroup);
        if (filterMinHeight != null) etMinHeight.setText(String.valueOf(filterMinHeight));
        if (filterMaxHeight != null) etMaxHeight.setText(String.valueOf(filterMaxHeight));
        if (filterMinWeight != null) etMinWeight.setText(String.valueOf(filterMinWeight));
        if (filterMaxWeight != null) etMaxWeight.setText(String.valueOf(filterMaxWeight));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter Recipients")
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> {
                    filterState = etState.getText().toString().trim();
                    filterCity = etCity.getText().toString().trim();
                    filterOrgans = etOrgans.getText().toString().trim();
                    filterBloodGroup = etBloodGroup.getText().toString().trim();
                    filterMinHeight = parseInteger(etMinHeight.getText().toString().trim());
                    filterMaxHeight = parseInteger(etMaxHeight.getText().toString().trim());
                    filterMinWeight = parseInteger(etMinWeight.getText().toString().trim());
                    filterMaxWeight = parseInteger(etMaxWeight.getText().toString().trim());

                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear", (dialog, which) -> {
                    filterState = "";
                    filterCity = "";
                    filterOrgans = "";
                    filterBloodGroup = "";
                    filterMinHeight = null;
                    filterMaxHeight = null;
                    filterMinWeight = null;
                    filterMaxWeight = null;
                    applyFilters();
                })
                .show();
    }

    private Integer parseInteger(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
