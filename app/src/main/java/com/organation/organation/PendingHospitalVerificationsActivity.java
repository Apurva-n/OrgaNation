package com.organation.organation;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingHospitalVerificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout tvNoPending;  // ✅ Fixed: was TextView
    private PendingHospitalAdapter adapter;
    private List<HospitalVerificationModel> pendingHospitals;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_hospital_verifications);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewPendingHospitals);
        tvNoPending = findViewById(R.id.tvNoPending);  // ✅ Fixed: now maps to LinearLayout

        // Setup RecyclerView
        pendingHospitals = new ArrayList<>();
        adapter = new PendingHospitalAdapter(this, pendingHospitals, new OnHospitalActionListener() {
            @Override
            public void onHospitalAction(HospitalVerificationModel hospital, String action) {
                if (action.equals("approve")) {
                    showApproveDialog(hospital);
                } else if (action.equals("reject")) {
                    showRejectDialog(hospital);
                } else if (action.equals("details")) {
                    showHospitalDetails(hospital);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load pending hospitals
        loadPendingHospitals();
    }

    private void loadPendingHospitals() {
        Query query = db.collection("hospitals")
                .whereEqualTo("verificationStatus", "pending")
                .orderBy("registrationDate", Query.Direction.DESCENDING);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingHospitals.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            HospitalVerificationModel hospital = documentToHospitalModel(document);
                            if (hospital != null) {
                                hospital.setId(document.getId());
                                pendingHospitals.add(hospital);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (pendingHospitals.isEmpty()) {
                            tvNoPending.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvNoPending.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("PendingHospitals", "Error loading pending hospitals", task.getException());
                        Toast.makeText(this, "Error loading pending hospitals", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Utility method for safe string extraction from Firestore
    private String getSafeString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            Log.w("PendingHospitals", "Field " + key + " is null");
            return "";
        }
        String result = value.toString();
        Log.d("PendingHospitals", "Field " + key + ": " + result);
        return result;
    }

    private HospitalVerificationModel documentToHospitalModel(DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        if (data == null) {
            Log.e("PendingHospitals", "Document data is null");
            return null;
        }

        HospitalVerificationModel hospital = new HospitalVerificationModel(data);

        hospital.setHospitalName(getSafeString(data, "01]Hospital_Name"));
        hospital.setAuthorityName(getSafeString(data, "02]Authority_Name"));
        hospital.setRegistrationNumber(getSafeString(data, "09]Gov_Reg_Number"));
        hospital.setEmail(getSafeString(data, "04]Official_Email"));
        hospital.setPhone(getSafeString(data, "03]Contact_Number"));
        hospital.setState(getSafeString(data, "07]State"));
        hospital.setCity(getSafeString(data, "06]City"));
        hospital.setStreet(getSafeString(data, "05]Street"));
        hospital.setLandmark(getSafeString(data, "08]Landmark"));
        hospital.setPincode(getSafeString(data, "18]Pincode"));
        hospital.setHospitalType(getSafeString(data, "12]Hospital_Type"));
        hospital.setFacilities(getSafeString(data, "21]Facilities"));
        hospital.setStatus(getSafeString(data, "verificationStatus"));

        return hospital;
    }

    private void showApproveDialog(HospitalVerificationModel hospital) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Hospital")
                .setMessage("Are you sure you want to approve " + hospital.getHospitalName() + "?")
                .setPositiveButton("Approve", (dialog, which) -> approveHospital(hospital))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRejectDialog(HospitalVerificationModel hospital) {
        new AlertDialog.Builder(this)
                .setTitle("Reject Hospital")
                .setMessage("Are you sure you want to reject " + hospital.getHospitalName() + "?")
                .setPositiveButton("Reject", (dialog, which) -> rejectHospital(hospital))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveHospital(HospitalVerificationModel hospital) {
        db.collection("hospitals")
                .document(hospital.getId())
                .update(
                        "verificationStatus", "approved",
                        "verificationDate", new java.util.Date(),
                        "verifiedBy", mAuth.getCurrentUser().getEmail()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Hospital approved successfully!", Toast.LENGTH_SHORT).show();
                    loadPendingHospitals();
                })
                .addOnFailureListener(e -> {
                    Log.e("Approval", "Error updating verification status", e);
                    Toast.makeText(this, "Error approving hospital", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectHospital(HospitalVerificationModel hospital) {
        db.collection("hospitals")
                .document(hospital.getId())
                .update(
                        "verificationStatus", "rejected",
                        "verificationDate", new java.util.Date(),
                        "verifiedBy", mAuth.getCurrentUser().getEmail()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Hospital rejected", Toast.LENGTH_SHORT).show();
                    loadPendingHospitals();
                })
                .addOnFailureListener(e -> {
                    Log.e("Rejection", "Error updating verification status", e);
                    Toast.makeText(this, "Error rejecting hospital", Toast.LENGTH_SHORT).show();
                });
    }

    private void showHospitalDetails(HospitalVerificationModel hospital) {
        StringBuilder details = new StringBuilder();
        details.append("Hospital Name: ").append(hospital.getHospitalName()).append("\n");
        details.append("Authority Name: ").append(hospital.getAuthorityName()).append("\n");
        details.append("Registration Number: ").append(hospital.getRegistrationNumber()).append("\n");
        details.append("Email: ").append(hospital.getEmail()).append("\n");
        details.append("Phone: ").append(hospital.getPhone()).append("\n");
        details.append("Address: ").append(hospital.getStreet()).append(", ")
                .append(hospital.getLandmark()).append(", ")
                .append(hospital.getCity()).append(", ")
                .append(hospital.getState()).append(" - ")
                .append(hospital.getPincode()).append("\n");
        details.append("Hospital Type: ").append(hospital.getHospitalType()).append("\n");
        details.append("Facilities: ").append(hospital.getFacilities()).append("\n");
        details.append("Status: ").append(hospital.getStatus()).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Hospital Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}

interface OnHospitalActionListener {
    void onHospitalAction(HospitalVerificationModel hospital, String action);
}