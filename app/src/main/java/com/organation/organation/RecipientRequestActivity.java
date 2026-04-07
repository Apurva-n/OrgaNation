package com.organation.organation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RecipientRequestActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    // UI Components
    private Spinner spinnerOrganType, spinnerBloodType, spinnerUrgency, spinnerHospital;
    private EditText etHospitalName, etHospitalCity, etHospitalLocation, etTreatingDoctor;
    private EditText etMedicalDetails, etAdditionalNotes;
    private TextView tvRequestDate;
    private Button btnSubmitRequest, btnCancel;
    
    // Data
    private String selectedDate = "";
    private String recipientUid = "";
    private String recipientName = "";
    private String recipientAadhaar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_request);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            recipientUid = mAuth.getCurrentUser().getUid();
        }
        
        initializeViews();
        setupSpinners();
        setupDatePicker();
        setupListeners();
        loadRecipientData();
        loadHospitals();
    }

    private void initializeViews() {
        // Spinners
        spinnerOrganType = findViewById(R.id.spinnerOrganType);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        spinnerUrgency = findViewById(R.id.spinnerUrgency);
        spinnerHospital = findViewById(R.id.spinnerHospital);
        
        // EditText fields
        etHospitalName = findViewById(R.id.etHospitalName);
        etHospitalCity = findViewById(R.id.etHospitalCity);
        etHospitalLocation = findViewById(R.id.etHospitalLocation);
        etTreatingDoctor = findViewById(R.id.etTreatingDoctor);
        etMedicalDetails = findViewById(R.id.etMedicalDetails);
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        
        // TextView
        tvRequestDate = findViewById(R.id.tvRequestDate);
        
        // Buttons
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupSpinners() {
        // Organ Types
        ArrayAdapter<CharSequence> organAdapter = ArrayAdapter.createFromResource(this,
                R.array.organ_types, android.R.layout.simple_spinner_item);
        organAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrganType.setAdapter(organAdapter);

        // Blood Types
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(bloodAdapter);

        // Urgency Levels
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.urgency_levels, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgency.setAdapter(urgencyAdapter);

        // Hospital spinner (will be populated from Firestore)
        ArrayAdapter<CharSequence> hospitalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        hospitalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHospital.setAdapter(hospitalAdapter);
    }

    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = dateFormat.format(calendar.getTime());
                    tvRequestDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        tvRequestDate.setOnClickListener(v -> datePickerDialog.show());
        
        // Set current date as default
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        tvRequestDate.setText(selectedDate);
    }

    private void setupListeners() {
        btnSubmitRequest.setOnClickListener(v -> submitRequest());
        btnCancel.setOnClickListener(v -> finish());
        
        // Auto-fill hospital details when hospital is selected
        spinnerHospital.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Select Hospital" item
                    String selectedHospital = (String) parent.getItemAtPosition(position);
                    loadHospitalDetails(selectedHospital);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadRecipientData() {
        db.collection("users").document(recipientUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        recipientName = documentSnapshot.getString("fullName");
                        recipientAadhaar = documentSnapshot.getString("adhaarNumber");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipient data", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHospitals() {
        db.collection("hospitals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerHospital.getAdapter();
                    adapter.clear();
                    adapter.add("Select Hospital");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Object hospitalNameObj = document.get(FieldPath.of("01]Hospital_Name"));
                        String hospitalName = hospitalNameObj != null ? hospitalNameObj.toString() : "";
                        if (hospitalName != null && !hospitalName.isEmpty()) {
                            adapter.add(hospitalName);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading hospitals", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHospitalDetails(String hospitalName) {
        db.collection("hospitals")
                .whereEqualTo(FieldPath.of("01]Hospital_Name"), hospitalName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        etHospitalName.setText(hospitalName);
                        
                        Object cityObj = document.get(FieldPath.of("06]City"));
                        Object streetObj = document.get(FieldPath.of("05]Street"));
                        
                        etHospitalCity.setText(cityObj != null ? cityObj.toString() : "");
                        etHospitalLocation.setText(streetObj != null ? streetObj.toString() : "");
                        break; // Take first match
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading hospital details", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitRequest() {
        if (!validateForm()) {
            return;
        }

        // Generate unique request ID
        String requestId = "REQ_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestId", requestId);
        requestData.put("recipientUid", recipientUid);
        requestData.put("recipientName", recipientName);
        requestData.put("recipientAadhaar", recipientAadhaar);
        requestData.put("organType", spinnerOrganType.getSelectedItem().toString());
        requestData.put("bloodType", spinnerBloodType.getSelectedItem().toString());
        requestData.put("urgency", spinnerUrgency.getSelectedItem().toString());
        requestData.put("hospitalName", etHospitalName.getText().toString());
        requestData.put("hospitalCity", etHospitalCity.getText().toString());
        requestData.put("hospitalLocation", etHospitalLocation.getText().toString());
        requestData.put("treatingDoctor", etTreatingDoctor.getText().toString());
        requestData.put("medicalDetails", etMedicalDetails.getText().toString());
        requestData.put("additionalNotes", etAdditionalNotes.getText().toString());
        requestData.put("requestDate", selectedDate);
        requestData.put("status", "pending");
        requestData.put("hospitalNotes", "");
        requestData.put("approvedDate", null);
        requestData.put("processedDate", null);
        requestData.put("completedDate", null);
        requestData.put("declinedDate", null);
        requestData.put("declinedReason", "");

        // Save to organ_requests collection
        db.collection("organ_requests").document(requestId)
                .set(requestData)
                .addOnSuccessListener(aVoid -> {
                    updateRecipientRequestCount();
                    Toast.makeText(this, "Organ request submitted successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error submitting request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateRecipientRequestCount() {
        db.collection("recipient_requests").document(recipientUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> updateData;
                    
                    if (documentSnapshot.exists()) {
                        // Update existing record
                        long currentTotal = documentSnapshot.getLong("totalRequests");
                        long currentPending = documentSnapshot.getLong("pendingRequests");
                        
                        updateData = new HashMap<>();
                        updateData.put("totalRequests", currentTotal + 1);
                        updateData.put("pendingRequests", currentPending + 1);
                        updateData.put("lastUpdated", new java.util.Date());
                    } else {
                        // Create new record
                        updateData = new HashMap<>();
                        updateData.put("totalRequests", 1L);
                        updateData.put("pendingRequests", 1L);
                        updateData.put("approvedRequests", 0L);
                        updateData.put("processedRequests", 0L);
                        updateData.put("completedRequests", 0L);
                        updateData.put("declinedRequests", 0L);
                        updateData.put("lastUpdated", new java.util.Date());
                    }
                    
                    db.collection("recipient_requests").document(recipientUid)
                            .update(updateData);
                });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (spinnerOrganType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select organ type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spinnerBloodType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select blood type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spinnerUrgency.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select urgency level", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (etHospitalName.getText().toString().trim().isEmpty()) {
            etHospitalName.setError("Hospital name required");
            isValid = false;
        }

        if (etHospitalCity.getText().toString().trim().isEmpty()) {
            etHospitalCity.setError("Hospital city required");
            isValid = false;
        }

        if (etHospitalLocation.getText().toString().trim().isEmpty()) {
            etHospitalLocation.setError("Hospital location required");
            isValid = false;
        }

        if (etTreatingDoctor.getText().toString().trim().isEmpty()) {
            etTreatingDoctor.setError("Treating doctor name required");
            isValid = false;
        }

        if (etMedicalDetails.getText().toString().trim().isEmpty()) {
            etMedicalDetails.setError("Medical details required");
            isValid = false;
        }

        return isValid;
    }
}
