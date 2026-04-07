package com.organation.organation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DonorSelectionDialog {
    
    private Context context;
    private RequestModel request;
    private OnDonorSelectedListener listener;
    private FirebaseFirestore db;
    
    private List<DonorModel> availableDonors;
    private DonorModel selectedDonor;
    private String selectedDate;
    private String selectedTime;
    
    public interface OnDonorSelectedListener {
        void onDonorSelected(DonorModel donor, String transplantDate, String transplantTime);
    }
    
    public DonorSelectionDialog(Context context, RequestModel request, OnDonorSelectedListener listener) {
        this.context = context;
        this.request = request;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.availableDonors = new ArrayList<>();
    }
    
    public void show() {
        // Fetch available donors first
        fetchAvailableDonors();
    }
    
    private void fetchAvailableDonors() {
        db.collection("donors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableDonors.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonorModel donor = convertDocumentToDonor(document);
                        if (donor != null && isDonorCompatible(donor)) {
                            availableDonors.add(donor);
                        }
                    }
                    
                    // Add debug logging
                    System.out.println("DonorSelectionDialog: Found " + availableDonors.size() + " compatible donors");
                    
                    if (availableDonors.isEmpty()) {
                        Toast.makeText(context, "No compatible donors found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    showDonorSelectionDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching donors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private boolean isDonorCompatible(DonorModel donor) {
        // Basic compatibility check
        if (donor.getBloodGroup() == null || request.getBloodType() == null) {
            System.out.println("DonorSelectionDialog: Missing blood data - Donor: " + 
                              (donor.getBloodGroup() != null ? donor.getBloodGroup() : "NULL") + 
                              ", Request: " + (request.getBloodType() != null ? request.getBloodType() : "NULL"));
            return false;
        }
        
        // Check if donor has the required organ
        if (!isOrganCompatible(request.getOrganType(), donor.getOrgansToNDonate())) {
            System.out.println("DonorSelectionDialog: Organ incompatible - Request: " + 
                              request.getOrganType() + ", Donor: " + donor.getOrgansToNDonate());
            return false;
        }
        
        // Check blood group compatibility
        boolean bloodCompatible = isBloodGroupCompatible(request.getBloodType(), donor.getBloodGroup());
        System.out.println("DonorSelectionDialog: Blood compatibility - Request: " + 
                          request.getBloodType() + ", Donor: " + donor.getBloodGroup() + 
                          " -> " + (bloodCompatible ? "COMPATIBLE" : "INCOMPATIBLE"));
        
        return bloodCompatible;
    }
    
    private boolean isBloodGroupCompatible(String recipientBlood, String donorBlood) {
        // CORRECTED: Universal donor rules and proper compatibility
        if (donorBlood == null || recipientBlood == null) return false;
        
        // O+ is universal donor (can donate to all positive types)
        if (donorBlood.equals("O+")) {
            return recipientBlood.equals("A+") || recipientBlood.equals("B+") || 
                   recipientBlood.equals("AB+") || recipientBlood.equals("O+");
        }
        
        // O- is universal donor (can donate to ALL types)
        if (donorBlood.equals("O-")) {
            return true; // Can donate to any blood group
        }
        
        // A+ can donate to A+ and AB+
        if (donorBlood.equals("A+")) {
            return recipientBlood.equals("A+") || recipientBlood.equals("AB+");
        }
        
        // A- can donate to A+, A-, AB+, AB-
        if (donorBlood.equals("A-")) {
            return recipientBlood.equals("A+") || recipientBlood.equals("A-") || 
                   recipientBlood.equals("AB+") || recipientBlood.equals("AB-");
        }
        
        // B+ can donate to B+ and AB+
        if (donorBlood.equals("B+")) {
            return recipientBlood.equals("B+") || recipientBlood.equals("AB+");
        }
        
        // B- can donate to B+, B-, AB+, AB-
        if (donorBlood.equals("B-")) {
            return recipientBlood.equals("B+") || recipientBlood.equals("B-") || 
                   recipientBlood.equals("AB+") || recipientBlood.equals("AB-");
        }
        
        // AB+ can donate to AB+ only
        if (donorBlood.equals("AB+")) {
            return recipientBlood.equals("AB+");
        }
        
        // AB- can donate to AB+ and AB-
        if (donorBlood.equals("AB-")) {
            return recipientBlood.equals("AB+") || recipientBlood.equals("AB-");
        }
        
        return false;
    }
    
    private boolean isOrganCompatible(String recipientOrgan, String donorOrgans) {
        if (recipientOrgan == null || donorOrgans == null) {
            return false;
        }
        
        recipientOrgan = recipientOrgan.toLowerCase().trim();
        donorOrgans = donorOrgans.toLowerCase().trim();
        
        // Handle comma-separated organs like "liver,pancreas,kidney"
        String[] donorOrganList = donorOrgans.split(",");
        
        for (String donorOrgan : donorOrganList) {
            donorOrgan = donorOrgan.trim();
            if (donorOrgan.equals(recipientOrgan)) {
                return true; // Found matching organ
            }
        }
        
        return false; // No matching organ found
    }
    
    private void showDonorSelectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_donor_selection, null);
        
        // Initialize dialog components
        Spinner spinnerDonors = dialogView.findViewById(R.id.spinnerDonors);
        TextView tvRecipientInfo = dialogView.findViewById(R.id.tvRecipientInfo);
        EditText etTransplantDate = dialogView.findViewById(R.id.etTransplantDate);
        EditText etTransplantTime = dialogView.findViewById(R.id.etTransplantTime);
        Button btnSelect = dialogView.findViewById(R.id.btnSelect);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        // Set recipient info
        tvRecipientInfo.setText("Recipient: " + request.getRecipientName() + 
                               "\nOrgan Needed: " + request.getOrganType() + 
                               "\nBlood Group: " + request.getBloodType());
        
        // Setup donor spinner
        List<String> donorNames = new ArrayList<>();
        for (DonorModel donor : availableDonors) {
            donorNames.add(donor.getFullName() + " (" + donor.getBloodGroup() + ")");
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, donorNames);
        spinnerDonors.setAdapter(adapter);
        
        spinnerDonors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDonor = availableDonors.get(position);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDonor = null;
            }
        });
        
        // Setup date picker
        etTransplantDate.setOnClickListener(v -> showDatePicker(etTransplantDate));
        
        // Setup time picker
        etTransplantTime.setOnClickListener(v -> showTimePicker(etTransplantTime));
        
        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Select Donor for Transplant")
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        // Setup button listeners
        btnSelect.setOnClickListener(v -> {
            if (selectedDonor == null) {
                Toast.makeText(context, "Please select a donor", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedDate == null || selectedDate.isEmpty()) {
                Toast.makeText(context, "Please select transplant date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedTime == null || selectedTime.isEmpty()) {
                Toast.makeText(context, "Please select transplant time", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Confirm selection
            showConfirmationDialog(selectedDonor, selectedDate, selectedTime, dialog);
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(formattedDate);
                    selectedDate = formattedDate;
                }, year, month, day);
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void showTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                            selectedHour, selectedMinute);
                    editText.setText(formattedTime);
                    selectedTime = formattedTime;
                }, hour, minute, true);
        
        timePickerDialog.show();
    }
    
    private void showConfirmationDialog(DonorModel donor, String date, String time, AlertDialog parentDialog) {
        String message = "Please confirm the transplant details:\n\n" +
                "📋 Recipient: " + request.getRecipientName() + "\n" +
                "🏥 Donor: " + donor.getFullName() + "\n" +
                "🩸 Blood Group: " + donor.getBloodGroup() + "\n" +
                "📅 Date: " + date + "\n" +
                "⏰ Time: " + time + "\n\n" +
                "This action will:\n" +
                "• Mark request as COMPLETED\n" +
                "• Send confirmation email to donor\n" +
                "• Update transplant records\n\n" +
                "Are you sure you want to proceed?";
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm Transplant Selection")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    parentDialog.dismiss();
                    if (listener != null) {
                        listener.onDonorSelected(donor, date, time);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private DonorModel convertDocumentToDonor(QueryDocumentSnapshot document) {
        try {
            DonorModel donor = new DonorModel();
            // Use getData() to access all fields and avoid special character issues
            Map<String, Object> data = document.getData();
            if (data == null) {
                return null;
            }
            
            // Get fields using the map instead of direct access
            donor.setFullName(getStringFromData(data, "01]Full_name"));
            donor.setAge(getStringFromData(data, "04]Age"));
            donor.setWeight(getStringFromData(data, "08]Weight"));
            donor.setHeight(getStringFromData(data, "07]Height"));
            donor.setBloodGroup(getStringFromData(data, "06]Blood_group"));
            donor.setGender(getStringFromData(data, "05]Gender"));
            donor.setPhone(getStringFromData(data, "09]Phone"));
            donor.setEmail(getStringFromData(data, "10]Email"));
            donor.setCity(getStringFromData(data, "12]City"));
            donor.setState(getStringFromData(data, "11]State"));
            donor.setStreet(getStringFromData(data, "13]Street"));
            donor.setLandmark(getStringFromData(data, "14]Landmark"));
            donor.setOrgansToNDonate(getStringFromData(data, "16]Organs_to_donate"));
            donor.setAadhaarNo(getStringFromData(data, "02]Aadhaar_no"));
            
            return donor;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getStringFromData(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return value != null ? value.toString() : null;
    }
}
