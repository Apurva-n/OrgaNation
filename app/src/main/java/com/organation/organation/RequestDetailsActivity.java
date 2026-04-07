package com.organation.organation;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    
    // UI Components
    private TextView tvRequestID, tvRecipientName, tvRecipientAadhaar;
    private TextView tvOrganType, tvBloodType, tvUrgency;
    private TextView tvHospitalName, tvHospitalCity, tvHospitalLocation, tvTreatingDoctor;
    private TextView tvMedicalDetails, tvAdditionalNotes, tvRequestDate, tvStatus;
    private TextView tvHospitalNotes, tvApprovedDate, tvProcessedDate, tvCompletedDate, tvDeclinedDate, tvDeclinedReason;
    
    // Hospital action components
    private LinearLayout llHospitalActions;
    private EditText etHospitalNotes, etDeclinedReason;
    private Button btnApprove, btnProcess, btnComplete, btnDecline, btnSaveNotes;
    
    // Donor selection
    private DonorSelectionDialog donorSelectionDialog;
    private DonorNotificationService notificationService;
    
    // Data
    private String requestId = "";
    private String userType = ""; // "recipient" or "hospital"
    private String hospitalId = "";
    private RequestModel currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();
        
        // Initialize services
        notificationService = new DonorNotificationService(this);
        
        // Get data from intent
        requestId = getIntent().getStringExtra("requestId");
        userType = getIntent().getStringExtra("userType");
        if (userType == null) userType = "recipient";
        hospitalId = getIntent().getStringExtra("hospitalId");
        
        initializeViews();
        setupUI();
        loadRequestDetails();
    }

    private void initializeViews() {
        // Basic Information
        tvRequestID = findViewById(R.id.tvRequestID);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientAadhaar = findViewById(R.id.tvRecipientAadhaar);
        
        // Organ Information
        tvOrganType = findViewById(R.id.tvOrganType);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvUrgency = findViewById(R.id.tvUrgency);
        
        // Hospital Information
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvHospitalCity = findViewById(R.id.tvHospitalCity);
        tvHospitalLocation = findViewById(R.id.tvHospitalLocation);
        tvTreatingDoctor = findViewById(R.id.tvTreatingDoctor);
        
        // Medical Information
        tvMedicalDetails = findViewById(R.id.tvMedicalDetails);
        tvAdditionalNotes = findViewById(R.id.tvAdditionalNotes);
        
        // Request Details
        tvRequestDate = findViewById(R.id.tvRequestDate);
        tvStatus = findViewById(R.id.tvStatus);
        
        // Status History
        tvHospitalNotes = findViewById(R.id.tvHospitalNotes);
        tvApprovedDate = findViewById(R.id.tvApprovedDate);
        tvProcessedDate = findViewById(R.id.tvProcessedDate);
        tvCompletedDate = findViewById(R.id.tvCompletedDate);
        tvDeclinedDate = findViewById(R.id.tvDeclinedDate);
        tvDeclinedReason = findViewById(R.id.tvDeclinedReason);
        
        // Hospital Action Components
        llHospitalActions = findViewById(R.id.llHospitalActions);
        etHospitalNotes = findViewById(R.id.etHospitalNotes);
        etDeclinedReason = findViewById(R.id.etDeclinedReason);
        btnApprove = findViewById(R.id.btnApprove);
        btnProcess = findViewById(R.id.btnProcess);
        btnComplete = findViewById(R.id.btnComplete);
        btnDecline = findViewById(R.id.btnDecline);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        
        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Request Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupUI() {
        // Show/hide hospital actions based on user type
        if ("hospital".equals(userType)) {
            llHospitalActions.setVisibility(View.VISIBLE);
            setupHospitalActions();
        } else {
            llHospitalActions.setVisibility(View.GONE);
        }
    }

    private void setupHospitalActions() {
        btnApprove.setOnClickListener(v -> showApproveDialog());
        btnProcess.setOnClickListener(v -> updateRequestStatus("processed"));
        btnComplete.setOnClickListener(v -> showDonorSelectionDialog());
        btnDecline.setOnClickListener(v -> showDeclineDialog());
        btnSaveNotes.setOnClickListener(v -> saveHospitalNotes());
        
        // Enable/disable buttons based on current status
        updateActionButtons();
    }

    private void updateActionButtons() {
        if (currentRequest == null) return;
        
        String status = currentRequest.getStatus();
        
        btnApprove.setEnabled("pending".equals(status));
        btnProcess.setEnabled("approved".equals(status));
        btnComplete.setEnabled("processed".equals(status));
        btnDecline.setEnabled(!"completed".equals(status) && !"declined".equals(status));
    }

    private void loadRequestDetails() {
        if (requestId.isEmpty()) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("organ_requests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentRequest = documentToRequest(documentSnapshot);
                        displayRequestDetails();
                        updateActionButtons();
                    } else {
                        Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayRequestDetails() {
        if (currentRequest == null) return;

        // Basic Information
        tvRequestID.setText("Request ID: " + currentRequest.getRequestId());
        tvRecipientName.setText(currentRequest.getRecipientName());
        tvRecipientAadhaar.setText(currentRequest.getRecipientAadhaar());
        
        // Organ Information
        tvOrganType.setText(currentRequest.getOrganType());
        tvBloodType.setText(currentRequest.getBloodType());
        tvUrgency.setText(currentRequest.getUrgency());
        
        // Hospital Information
        tvHospitalName.setText(currentRequest.getHospitalName());
        tvHospitalCity.setText(currentRequest.getHospitalCity());
        tvHospitalLocation.setText(currentRequest.getHospitalLocation());
        tvTreatingDoctor.setText(currentRequest.getTreatingDoctor());
        
        // Medical Information
        tvMedicalDetails.setText(currentRequest.getMedicalDetails());
        tvAdditionalNotes.setText(currentRequest.getAdditionalNotes() != null ? 
                currentRequest.getAdditionalNotes() : "No additional notes");
        
        // Request Details
        tvRequestDate.setText(currentRequest.getRequestDate());
        tvStatus.setText(currentRequest.getStatusDisplay());
        tvStatus.setTextColor(getResources().getColor(currentRequest.getStatusColor()));
        
        // Status History
        tvHospitalNotes.setText(currentRequest.getHospitalNotes() != null ? 
                currentRequest.getHospitalNotes() : "No notes from hospital");
        tvApprovedDate.setText(currentRequest.getApprovedDate() != null ? 
                currentRequest.getApprovedDate() : "Not yet approved");
        tvProcessedDate.setText(currentRequest.getProcessedDate() != null ? 
                currentRequest.getProcessedDate() : "Not yet processed");
        tvCompletedDate.setText(currentRequest.getCompletedDate() != null ? 
                currentRequest.getCompletedDate() : "Not yet completed");
        tvDeclinedDate.setText(currentRequest.getDeclinedDate() != null ? 
                currentRequest.getDeclinedDate() : "Not declined");
        tvDeclinedReason.setText(currentRequest.getDeclinedReason() != null ? 
                currentRequest.getDeclinedReason() : "No decline reason");
        
        // Load hospital notes for editing
        if ("hospital".equals(userType)) {
            etHospitalNotes.setText(currentRequest.getHospitalNotes() != null ? 
                    currentRequest.getHospitalNotes() : "");
        }
    }

    private void showApproveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Approve Request")
                .setMessage("Are you sure you want to approve this organ request?")
                .setPositiveButton("Approve", (dialog, which) -> updateRequestStatus("approved"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeclineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Decline Request")
                .setMessage("Please provide a reason for declining this request:");
        
        final EditText input = new EditText(this);
        input.setHint("Enter reason for declining");
        builder.setView(input);
        
        builder.setPositiveButton("Decline", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please provide a reason for declining", Toast.LENGTH_SHORT).show();
                return;
            }
            
            updateRequestStatus("declined", reason);
        })
        .setNegativeButton("Cancel", null)
        .show();
    }

    private void updateRequestStatus(String newStatus) {
        updateRequestStatus(newStatus, null);
    }

    private void updateRequestStatus(String newStatus, String declineReason) {
        if (currentRequest == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        
        switch (newStatus) {
            case "approved":
                updates.put("approvedDate", currentDate);
                break;
            case "processed":
                updates.put("processedDate", currentDate);
                break;
            case "completed":
                updates.put("completedDate", currentDate);
                break;
            case "declined":
                updates.put("declinedDate", currentDate);
                if (declineReason != null) {
                    updates.put("declinedReason", declineReason);
                }
                break;
        }
        
        db.collection("organ_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    updateRecipientRequestCount(newStatus);
                    loadRequestDetails(); // Refresh the display
                    
                    // Auto-close for completed requests to refresh the list
                    if ("completed".equals(newStatus)) {
                        finish(); // This will trigger refresh when user goes back
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecipientRequestCount(String newStatus) {
        if (currentRequest == null) return;

        String recipientUid = currentRequest.getRecipientUid();
        String oldStatus = currentRequest.getStatus();
        
        db.collection("recipient_requests").document(recipientUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> updates = new HashMap<>();
                        
                        // Decrease old status count
                        switch (oldStatus) {
                            case "pending":
                                updates.put("pendingRequests", documentSnapshot.getLong("pendingRequests") - 1);
                                break;
                            case "approved":
                                updates.put("approvedRequests", documentSnapshot.getLong("approvedRequests") - 1);
                                break;
                            case "processed":
                                updates.put("processedRequests", documentSnapshot.getLong("processedRequests") - 1);
                                break;
                        }
                        
                        // Increase new status count
                        switch (newStatus) {
                            case "approved":
                                updates.put("approvedRequests", documentSnapshot.getLong("approvedRequests") + 1);
                                break;
                            case "processed":
                                updates.put("processedRequests", documentSnapshot.getLong("processedRequests") + 1);
                                break;
                            case "completed":
                                updates.put("completedRequests", documentSnapshot.getLong("completedRequests") + 1);
                                break;
                            case "declined":
                                updates.put("declinedRequests", documentSnapshot.getLong("declinedRequests") + 1);
                                break;
                        }
                        
                        updates.put("lastUpdated", new Date());
                        
                        db.collection("recipient_requests").document(recipientUid)
                                .update(updates);
                    }
                });
    }

    private void saveHospitalNotes() {
        if (currentRequest == null) return;
        
        String notes = etHospitalNotes.getText().toString().trim();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("hospitalNotes", notes);
        
        db.collection("organ_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Notes saved successfully", Toast.LENGTH_SHORT).show();
                    loadRequestDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDonorSelectionDialog() {
        if (currentRequest == null) {
            Toast.makeText(this, "Request data not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        donorSelectionDialog = new DonorSelectionDialog(this, currentRequest, new DonorSelectionDialog.OnDonorSelectedListener() {
            @Override
            public void onDonorSelected(DonorModel donor, String transplantDate, String transplantTime) {
                // Update request status to completed with donor information
                completeRequestWithDonor(donor, transplantDate, transplantTime);
            }
        });
        
        donorSelectionDialog.show();
    }
    
    private void completeRequestWithDonor(DonorModel donor, String transplantDate, String transplantTime) {
        if (currentRequest == null) return;
        
        // Update request with donor information and completion details
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedDate", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        updates.put("selectedDonorName", donor.getFullName());
        updates.put("selectedDonorAadhaar", donor.getAadhaarNo());
        updates.put("selectedDonorPhone", donor.getPhone());
        updates.put("selectedDonorEmail", donor.getEmail());
        updates.put("selectedDonorBloodGroup", donor.getBloodGroup());
        updates.put("transplantDate", transplantDate);
        updates.put("transplantTime", transplantTime);
        updates.put("lastUpdated", new Date());
        
        db.collection("organ_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request completed with donor selection", Toast.LENGTH_SHORT).show();
                    
                    // Update donor organ status (CRITICAL BUSINESS LOGIC)
                    updateDonorOrganStatus(donor, currentRequest.getOrganType());
                    
                    // Update recipient status to inactive
                    updateRecipientStatus(currentRequest.getRecipientUid(), "completed");
                    
                    // Send notification to donor
                    sendDonorNotification(donor, transplantDate, transplantTime);
                    
                    // Show success message
                    new AlertDialog.Builder(RequestDetailsActivity.this)
                        .setTitle("Request Completed Successfully")
                        .setMessage("The organ request has been marked as completed with donor selection:\n\n" +
                                "Donor: " + donor.getFullName() + "\n" +
                                "Transplant Date: " + transplantDate + "\n" +
                                "Transplant Time: " + transplantTime + "\n\n" +
                                "The request status will be updated in the recipient requests list.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Go back to refresh the list
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                    
                    // Update UI
                    loadRequestDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateDonorOrganStatus(DonorModel donor, String selectedOrgan) {
        try {
            Log.d("DONOR_UPDATE", "=== STARTING DONOR ORGAN STATUS UPDATE ===");
            Log.d("DONOR_UPDATE", "Donor: " + donor.getFullName());
            Log.d("DONOR_UPDATE", "Donor Aadhaar: " + donor.getAadhaarNo());
            Log.d("DONOR_UPDATE", "Selected Organ: " + selectedOrgan);
            
            // Get donor document - TRY MULTIPLE FIELD NAMES
            Log.d("DONOR_UPDATE", "=== TRYING TO FIND DONOR DOCUMENT ===");
            
            // First try with regular field name
            db.collection("donors")
                .whereEqualTo(com.google.firebase.firestore.FieldPath.of("02]Aadhaar_no"), donor.getAadhaarNo())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("DONOR_UPDATE", "Query with '02]Aadhaar_no' found: " + queryDocumentSnapshots.size() + " documents");
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        processDonorDocument(queryDocumentSnapshots.getDocuments().get(0), selectedOrgan);
                    } else {
                        // Try with alternative field names
                        Log.d("DONOR_UPDATE", "=== TRYING ALTERNATIVE FIELD NAMES ===");
                        
                        // Try with "aadhaarNo"
                        db.collection("donors")
                            .whereEqualTo("aadhaarNo", donor.getAadhaarNo())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                Log.d("DONOR_UPDATE", "Query with 'aadhaarNo' found: " + queryDocumentSnapshots2.size() + " documents");
                                
                                if (!queryDocumentSnapshots2.isEmpty()) {
                                    processDonorDocument(queryDocumentSnapshots2.getDocuments().get(0), selectedOrgan);
                                } else {
                                    Log.e("DONOR_UPDATE", "❌ ERROR: Donor document not found with any field name");
                                    Log.e("DONOR_UPDATE", "❌ Donor Aadhaar: " + donor.getAadhaarNo());
                                    Log.e("DONOR_UPDATE", "❌ Trying to list all donors for debugging");
                                    
                                    // Debug: List all donors to see what's available
                                    db.collection("donors").get()
                                        .addOnSuccessListener(allDonors -> {
                                            Log.d("DONOR_UPDATE", "=== ALL DONORS IN DATABASE ===");
                                            for (QueryDocumentSnapshot doc : allDonors) {
                                                Log.d("DONOR_UPDATE", "Donor ID: " + doc.getId() + " Data: " + doc.getData());
                                            }
                                        });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DONOR_UPDATE", "❌ ERROR: Error finding donor document with 'aadhaarNo'", e);
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DONOR_UPDATE", "❌ ERROR: Error finding donor document with regular field name", e);
                });
        } catch (Exception e) {
            Log.e("DONOR_UPDATE", "❌ ERROR: Exception in updateDonorOrganStatus", e);
            Log.e("DONOR_UPDATE", "❌ Exception details: " + e.getMessage());
        }
    }
    
    private void processDonorDocument(com.google.firebase.firestore.DocumentSnapshot donorDoc, String selectedOrgan) {
        try {
            String donorId = donorDoc.getId();
            
            Log.d("DONOR_UPDATE", "Found donor document with ID: " + donorId);
            Log.d("DONOR_UPDATE", "Donor document data: " + donorDoc.getData());
            
            // DEBUG: Show all available fields
            Log.d("DONOR_UPDATE", "Available fields in donor document: " + donorDoc.getData().keySet());
            
            // Get current organs to donate - TRY MULTIPLE FIELD NAMES
            String currentOrgans = getSafeStringFromDocument(donorDoc, "16]Organs_to_donate");
            if (currentOrgans == null || currentOrgans.isEmpty()) {
                currentOrgans = getSafeStringFromDocument(donorDoc, "organsToDonate");
                Log.d("DONOR_UPDATE", "Trying organsToDonate: " + currentOrgans);
            }
            if (currentOrgans == null || currentOrgans.isEmpty()) {
                currentOrgans = getSafeStringFromDocument(donorDoc, "organs_to_donate");
                Log.d("DONOR_UPDATE", "Trying organs_to_donate: " + currentOrgans);
            }
            if (currentOrgans == null || currentOrgans.isEmpty()) {
                currentOrgans = getSafeStringFromDocument(donorDoc, "organs");
                Log.d("DONOR_UPDATE", "Trying organs: " + currentOrgans);
            }
            if (currentOrgans == null || currentOrgans.isEmpty()) {
                currentOrgans = getSafeStringFromDocument(donorDoc, "aadhaarNo");
                Log.d("DONOR_UPDATE", "Trying aadhaarNo: " + currentOrgans);
            }
            
            Log.d("DONOR_UPDATE", "Current organs before update: " + currentOrgans);
            
            // Remove selected organ from the list
            String updatedOrgans = removeOrganFromList(currentOrgans, selectedOrgan);
            Log.d("DONOR_UPDATE", "Updated organs after removal: " + updatedOrgans);
            
            // Update donor document - UPDATE THE SPECIAL FIELD DIRECTLY
            Map<String, Object> donorUpdates = new HashMap<>();
            donorUpdates.put("organsToDonate", updatedOrgans);
            donorUpdates.put("organs_to_donate", updatedOrgans);
            donorUpdates.put("organs", updatedOrgans);
            donorUpdates.put("lastUpdated", new Date());
            
            // Check if donor still has organs to donate
            if (updatedOrgans.isEmpty() || updatedOrgans.equals("N/A")) {
                donorUpdates.put("status", "completed"); // No more organs to donate
                donorUpdates.put("availability", "inactive");
                Log.d("DONOR_UPDATE", "Donor marked as completed - no more organs");
            } else {
                Log.d("DONOR_UPDATE", "Donor still has organs available: " + updatedOrgans);
            }
            
            Log.d("DONOR_UPDATE", "Updating donor document with: " + donorUpdates);
            
            // Update with regular field names first
            db.collection("donors").document(donorId)
                    .update(donorUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DONOR_UPDATE", "✅ SUCCESS: Donor organ status updated successfully");
                        Log.d("DONOR_UPDATE", "✅ Removed organ: " + selectedOrgan + " from donor: " + getSafeStringFromDocument(donorDoc, "01]Full_name"));
                        Log.d("DONOR_UPDATE", "✅ Updated organs list: " + updatedOrgans);
                        
                        // Now update the special field using FieldPath.of()
                        Map<String, Object> specialFieldUpdate = new HashMap<>();
                        specialFieldUpdate.put("16]Organs_to_donate", updatedOrgans);
                        
                        db.collection("donors").document(donorId)
                            .update(com.google.firebase.firestore.FieldPath.of("16]Organs_to_donate"), updatedOrgans)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("DONOR_UPDATE", "✅ SUCCESS: Special field '16]Organs_to_donate' updated too");
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e("DONOR_UPDATE", "❌ ERROR: Failed to update special field", e2);
                            });
                        
                        // Verify update by reading back
                        db.collection("donors").document(donorId).get()
                            .addOnSuccessListener(verifyDoc -> {
                                Log.d("DONOR_UPDATE", "=== VERIFICATION ===");
                                Log.d("DONOR_UPDATE", "Organs after update (special field): " + getSafeStringFromDocument(verifyDoc, "16]Organs_to_donate"));
                                Log.d("DONOR_UPDATE", "Regular organs field: " + verifyDoc.getString("organsToDonate"));
                                Log.d("DONOR_UPDATE", "✅ VERIFICATION COMPLETE - All fields updated successfully!");
                            })
                            .addOnFailureListener(e3 -> {
                                Log.e("DONOR_UPDATE", "Error during verification: " + e3.getMessage());
                            });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DONOR_UPDATE", "❌ ERROR: Failed to update donor organ status", e);
                        Log.e("DONOR_UPDATE", "❌ Error details: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e("DONOR_UPDATE", "❌ ERROR: Exception in processDonorDocument", e);
            Log.e("DONOR_UPDATE", "❌ Exception details: " + e.getMessage());
        }
    }
    
    private void updateRecipientStatus(String recipientAadhaar, String status) {
        try {
            Log.d("RECIPIENT_UPDATE", "=== STARTING RECIPIENT STATUS UPDATE ===");
            Log.d("RECIPIENT_UPDATE", "Recipient Aadhaar: " + recipientAadhaar);
            Log.d("RECIPIENT_UPDATE", "New Status: " + status);
            
            // Use FieldPath.of() for field names with special characters
            db.collection("Recepients")
                .whereEqualTo(com.google.firebase.firestore.FieldPath.of("02]Aadhaar_no"), recipientAadhaar)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot recipientDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String recipientId = recipientDoc.getId();
                        
                        Log.d("RECIPIENT_UPDATE", "Found recipient document with ID: " + recipientId);
                        Log.d("RECIPIENT_UPDATE", "Recipient Aadhaar: " + recipientAadhaar);
                        
                        // Update recipient status
                        Map<String, Object> recipientUpdates = new HashMap<>();
                        recipientUpdates.put("status", status);
                        recipientUpdates.put("transplantStatus", status);
                        recipientUpdates.put("lastUpdated", new Date());
                        
                        Log.d("RECIPIENT_UPDATE", "Updating recipient document with: " + recipientUpdates);
                        
                        db.collection("Recepients").document(recipientId)
                                .update(recipientUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("RECIPIENT_UPDATE", "✅ SUCCESS: Recipient status updated successfully");
                                    Log.d("RECIPIENT_UPDATE", "✅ Recipient " + recipientAadhaar + " status: " + status);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("RECIPIENT_UPDATE", "❌ ERROR: Failed to update recipient status", e);
                                    Log.e("RECIPIENT_UPDATE", "❌ Error details: " + e.getMessage());
                                });
                    } else {
                        Log.e("RECIPIENT_UPDATE", "❌ ERROR: Recipient document not found with Aadhaar: " + recipientAadhaar);
                        Log.e("RECIPIENT_UPDATE", "❌ Available recipients count: " + queryDocumentSnapshots.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RECIPIENT_UPDATE", "❌ ERROR: Error finding recipient document", e);
                    Log.e("RECIPIENT_UPDATE", "❌ Error details: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e("RECIPIENT_UPDATE", "❌ ERROR: Exception in updateRecipientStatus", e);
            Log.e("RECIPIENT_UPDATE", "❌ Exception details: " + e.getMessage());
        }
    }
    
    private String removeOrganFromList(String organsList, String organToRemove) {
        if (organsList == null || organsList.trim().isEmpty()) {
            return "N/A";
        }
        
        // Split by comma and remove the specified organ
        String[] organs = organsList.split(",");
        StringBuilder updatedOrgans = new StringBuilder();
        
        for (String organ : organs) {
            organ = organ.trim();
            if (!organ.equalsIgnoreCase(organToRemove) && !organ.isEmpty()) {
                if (updatedOrgans.length() > 0) {
                    updatedOrgans.append(", ");
                }
                updatedOrgans.append(organ);
            }
        }
        
        return updatedOrgans.length() > 0 ? updatedOrgans.toString() : "N/A";
    }
    
    private void sendDonorNotification(DonorModel donor, String transplantDate, String transplantTime) {
        if (currentRequest == null) return;
        
        String hospitalName = currentRequest.getHospitalName();
        
        // Load hospital details directly
        Map<String, String> hospitalDetails = new HashMap<>();
        try {
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("hospitals")
                .whereEqualTo("userId", currentUserUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot hospitalDoc = queryDocumentSnapshots.getDocuments().get(0);
                        
                        // Add hospital details
                        hospitalDetails.put("hospitalName", hospitalName);
                        hospitalDetails.put("contactNumber", getSafeStringFromDocument(hospitalDoc, "02]Contact_Number"));
                        hospitalDetails.put("officialEmail", getSafeStringFromDocument(hospitalDoc, "03]Official_Email"));
                        hospitalDetails.put("street", getSafeStringFromDocument(hospitalDoc, "04]Street"));
                        hospitalDetails.put("city", getSafeStringFromDocument(hospitalDoc, "05]City"));
                        hospitalDetails.put("state", getSafeStringFromDocument(hospitalDoc, "06]State"));
                        hospitalDetails.put("websiteUrl", getSafeStringFromDocument(hospitalDoc, "17]Website_URL"));
                        hospitalDetails.put("treatingDoctor", currentRequest.getTreatingDoctor());
                        hospitalDetails.put("hospitalLocation", currentRequest.getHospitalLocation());
                        
                        // Send notification with loaded hospital details
                        sendNotificationWithDetails(donor, transplantDate, transplantTime, hospitalName, hospitalDetails);
                    } else {
                        // Fallback to basic info
                        hospitalDetails.put("hospitalName", hospitalName);
                        hospitalDetails.put("treatingDoctor", currentRequest.getTreatingDoctor());
                        hospitalDetails.put("hospitalLocation", currentRequest.getHospitalLocation());
                        sendNotificationWithDetails(donor, transplantDate, transplantTime, hospitalName, hospitalDetails);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to basic info
                    hospitalDetails.put("hospitalName", hospitalName);
                    hospitalDetails.put("treatingDoctor", currentRequest.getTreatingDoctor());
                    hospitalDetails.put("hospitalLocation", currentRequest.getHospitalLocation());
                    sendNotificationWithDetails(donor, transplantDate, transplantTime, hospitalName, hospitalDetails);
                });
        } catch (Exception e) {
            // Fallback to basic info
            hospitalDetails.put("hospitalName", hospitalName);
            hospitalDetails.put("treatingDoctor", currentRequest.getTreatingDoctor());
            hospitalDetails.put("hospitalLocation", currentRequest.getHospitalLocation());
            sendNotificationWithDetails(donor, transplantDate, transplantTime, hospitalName, hospitalDetails);
        }
    }
    
    private void sendNotificationWithDetails(DonorModel donor, String transplantDate, String transplantTime, 
                                           String hospitalName, Map<String, String> hospitalDetails) {
        notificationService.sendDonorNotification(
            currentRequest, 
            donor, 
            transplantDate, 
            transplantTime, 
            hospitalName,
            hospitalDetails,
            new DonorNotificationService.NotificationCallback() {
                @Override
                public void onNotificationOpened() {
                    Toast.makeText(RequestDetailsActivity.this, 
                        "Email opened - Please click SEND to notify donor", Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onNotificationCancelled() {
                    Toast.makeText(RequestDetailsActivity.this, 
                        "Email notification cancelled", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onNotificationCopied() {
                    Toast.makeText(RequestDetailsActivity.this, 
                        "Email details copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onNotificationSent(boolean success) {
                    if (success) {
                        Toast.makeText(RequestDetailsActivity.this, 
                            "Donor notification process initiated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RequestDetailsActivity.this, 
                            "Failed to initiate donor notification", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    
    private String getSafeStringFromDocument(com.google.firebase.firestore.DocumentSnapshot document, String fieldName) {
        try {
            Object value = document.get(FieldPath.of(fieldName));
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private RequestModel documentToRequest(com.google.firebase.firestore.DocumentSnapshot document) {
        try {
            RequestModel request = new RequestModel();
            request.setRequestId(document.getString("requestId"));
            request.setRecipientUid(document.getString("recipientUid"));
            request.setRecipientName(document.getString("recipientName"));
            request.setRecipientAadhaar(document.getString("recipientAadhaar"));
            request.setOrganType(document.getString("organType"));
            request.setBloodType(document.getString("bloodType"));
            request.setUrgency(document.getString("urgency"));
            request.setHospitalName(document.getString("hospitalName"));
            request.setHospitalCity(document.getString("hospitalCity"));
            request.setHospitalLocation(document.getString("hospitalLocation"));
            request.setTreatingDoctor(document.getString("treatingDoctor"));
            request.setMedicalDetails(document.getString("medicalDetails"));
            request.setAdditionalNotes(document.getString("additionalNotes"));
            request.setRequestDate(document.getString("requestDate"));
            request.setStatus(document.getString("status"));
            request.setHospitalNotes(document.getString("hospitalNotes"));
            request.setApprovedDate(document.getString("approvedDate"));
            request.setProcessedDate(document.getString("processedDate"));
            request.setCompletedDate(document.getString("completedDate"));
            request.setDeclinedDate(document.getString("declinedDate"));
            request.setDeclinedReason(document.getString("declinedReason"));
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
