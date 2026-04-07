package com.organation.organation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvailableHospitalsActivity extends AppCompatActivity {
    private RecyclerView rvHospitals;
    private HospitalAdapter hospitalAdapter;
    private List<HospitalModel> hospitalList;
    private List<HospitalModel> allHospitals;
    private FirebaseFirestore db;
    private EditText etSearchHospital;
    private LinearLayout llEmptyState;

    // Enhanced filters
    private String filterHospitalName = "";
    private String filterAuthorityName = "";
    private String filterRegistrationNumber = "";
    private String filterState = "";
    private String filterCity = "";
    private String filterStreet = "";
    private String filterLandmark = "";
    private String filterHospitalType = "";
    private String filterPincode = "";
    private String filterFacility = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_hospitals);

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Hospitals");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupSearchFunctionality();

        rvHospitals = findViewById(R.id.rvHospitals);
        hospitalList = new ArrayList<>();
        allHospitals = new ArrayList<>();
        hospitalAdapter = new HospitalAdapter(this, hospitalList);

        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        rvHospitals.setAdapter(hospitalAdapter);

        db = FirebaseFirestore.getInstance();

        // Setup filter button
        findViewById(R.id.fabFilter).setOnClickListener(v -> showFilterDialog());

        // Fetch hospitals from Firestore
        fetchAvailableHospitals();
    }

    private void initializeViews() {
        etSearchHospital = findViewById(R.id.etSearchHospital);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupSearchFunctionality() {
        etSearchHospital.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHospitalsBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchAvailableHospitals() {
        // First try to get all hospitals to debug
        db.collection("hospitals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Toast.makeText(this, "Found " + queryDocumentSnapshots.size() + " hospitals in database", Toast.LENGTH_LONG).show();
                    
                    allHospitals.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if hospital has isProfileComplete field using document.get() with FieldPath.of()
                        Object isCompleteObj = document.get(FieldPath.of("14]isProfileComplete"));
                        Boolean isComplete = null;
                        if (isCompleteObj instanceof Boolean) {
                            isComplete = (Boolean) isCompleteObj;
                        }
                        
                        // ✅ FIXED: Use FieldPath.of() for hospital name
                        Object hospitalNameObj = document.get(FieldPath.of("01]Hospital_Name"));
                        String hospitalName = hospitalNameObj != null ? hospitalNameObj.toString() : "Unknown Hospital";
                        
                        Toast.makeText(this, "Hospital: " + hospitalName + ", Complete: " + isComplete, 
                                Toast.LENGTH_SHORT).show();
                        
                        // Only add hospitals that are complete OR if field doesn't exist
                        if (isComplete == null || isComplete) {
                            HospitalModel hospital = parseDocumentToHospital(document);
                            if (hospital != null) {
                                allHospitals.add(hospital);
                            }
                        }
                    }

                    Toast.makeText(this, "Added " + allHospitals.size() + " hospitals to list", 
                            Toast.LENGTH_LONG).show();

                    applyFilters();

                    if (hospitalList.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AvailableHospitalsActivity.this,
                            "Error fetching hospitals: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void applyFilters() {
        hospitalList.clear();
        for (HospitalModel hospital : allHospitals) {
            if (!matchesFilter(hospital)) continue;
            hospitalList.add(hospital);
        }
        hospitalAdapter.notifyDataSetChanged();

        if (hospitalList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private boolean matchesFilter(HospitalModel hospital) {
        // Basic filters
        if (!filterHospitalName.isEmpty() && !hospital.getHospitalName().toLowerCase().contains(filterHospitalName.toLowerCase())) {
            return false;
        }
        if (!filterAuthorityName.isEmpty() && !hospital.getAuthorityName().toLowerCase().contains(filterAuthorityName.toLowerCase())) {
            return false;
        }
        if (!filterRegistrationNumber.isEmpty() && !hospital.getGovRegNumber().toLowerCase().contains(filterRegistrationNumber.toLowerCase())) {
            return false;
        }
        if (!filterState.isEmpty() && !hospital.getState().toLowerCase().contains(filterState.toLowerCase())) {
            return false;
        }
        if (!filterCity.isEmpty() && !hospital.getCity().toLowerCase().contains(filterCity.toLowerCase())) {
            return false;
        }
        if (!filterStreet.isEmpty() && !hospital.getStreet().toLowerCase().contains(filterStreet.toLowerCase())) {
            return false;
        }
        if (!filterLandmark.isEmpty() && !hospital.getLandmark().toLowerCase().contains(filterLandmark.toLowerCase())) {
            return false;
        }
        if (!filterHospitalType.isEmpty() && !hospital.getHospitalType().toLowerCase().contains(filterHospitalType.toLowerCase())) {
            return false;
        }
        
        // Additional filters
        if (!filterPincode.isEmpty() && !hospital.getPincode().toLowerCase().contains(filterPincode.toLowerCase())) {
            return false;
        }
        
        // Facility filter
        if (!filterFacility.isEmpty()) {
            boolean hasFacility = false;
            if (filterFacility.equalsIgnoreCase("Organ Transplant") && hospital.hasOrganTransplant()) {
                hasFacility = true;
            }
            if (filterFacility.equalsIgnoreCase("ICU") && hospital.hasICU()) {
                hasFacility = true;
            }
            if (filterFacility.equalsIgnoreCase("Emergency") && hospital.hasEmergency()) {
                hasFacility = true;
            }
            if (filterFacility.equalsIgnoreCase("Organ Storage") && hospital.hasOrganStorage()) {
                hasFacility = true;
            }
            if (filterFacility.equalsIgnoreCase("Laboratory") && hospital.hasLaboratory()) {
                hasFacility = true;
            }
            if (filterFacility.equalsIgnoreCase("Ambulance") && hospital.hasAmbulance()) {
                hasFacility = true;
            }
            
            if (!hasFacility) {
                return false;
            }
        }
        
        return true;
    }

    private void filterHospitalsBySearch(String query) {
        hospitalList.clear();
        for (HospitalModel hospital : allHospitals) {
            // First check if it matches current filters
            if (!matchesFilter(hospital)) continue;

            // Then check if it matches search query
            if (query.isEmpty()) {
                hospitalList.add(hospital);
            } else {
                String lowerQuery = query.toLowerCase();
                if (hospital.getHospitalName().toLowerCase().contains(lowerQuery) ||
                    hospital.getAuthorityName().toLowerCase().contains(lowerQuery) ||
                    hospital.getCity().toLowerCase().contains(lowerQuery) ||
                    hospital.getState().toLowerCase().contains(lowerQuery) ||
                    hospital.getHospitalType().toLowerCase().contains(lowerQuery) ||
                    hospital.getGovRegNumber().toLowerCase().contains(lowerQuery) ||
                    hospital.getStreet().toLowerCase().contains(lowerQuery) ||
                    hospital.getLandmark().toLowerCase().contains(lowerQuery) ||
                    hospital.getPincode().toLowerCase().contains(lowerQuery)) {
                    hospitalList.add(hospital);
                }
            }
        }
        hospitalAdapter.notifyDataSetChanged();

        if (hospitalList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private HospitalModel parseDocumentToHospital(QueryDocumentSnapshot document) {
        try {
            HospitalModel hospital = new HospitalModel();
            
            // Basic Information - use getString method which handles FieldPath
            hospital.setHospitalName(getStringFromFieldPath(document, "01]Hospital_Name"));
            hospital.setAuthorityName(getStringFromFieldPath(document, "02]Authority_Name"));
            hospital.setContactNumber(getStringFromFieldPath(document, "03]Contact_Number"));
            hospital.setOfficialEmail(getStringFromFieldPath(document, "04]Official_Email"));
            hospital.setStreet(getStringFromFieldPath(document, "05]Street"));
            hospital.setCity(getStringFromFieldPath(document, "06]City"));
            hospital.setState(getStringFromFieldPath(document, "07]State"));
            hospital.setLandmark(getStringFromFieldPath(document, "08]Landmark"));
            hospital.setGovRegNumber(getStringFromFieldPath(document, "09]Gov_Reg_Number"));
            hospital.setAuthorityContact(getStringFromFieldPath(document, "10]Authority_Contact"));
            hospital.setAuthorityEmail(getStringFromFieldPath(document, "11]Authority_Email"));
            hospital.setHospitalType(getStringFromFieldPath(document, "12]Hospital_Type"));
            
            // Additional Hospital Details
            hospital.setHospitalId(getStringFromFieldPath(document, "15]Hospital_ID"));
            hospital.setEstYear(getStringFromFieldPath(document, "16]Establishment_Year"));
            hospital.setWebsiteUrl(getStringFromFieldPath(document, "17]Website_URL"));
            hospital.setPincode(getStringFromFieldPath(document, "18]Pincode"));
            hospital.setLatitude(getStringFromFieldPath(document, "19]Latitude"));
            hospital.setLongitude(getStringFromFieldPath(document, "20]Longitude"));
            
            // Facilities
            Object facilitiesObj = document.get(FieldPath.of("21]Facilities"));
            if (facilitiesObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Boolean> facilities = (Map<String, Boolean>) facilitiesObj;
                hospital.setFacilities(facilities);
            }
            
            return hospital;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getStringFromFieldPath(QueryDocumentSnapshot document, String key) {
        try {
            // Use FieldPath.of() for all field access to handle special characters
            Object value = document.get(FieldPath.of(key));
            return value != null ? value.toString() : "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void showFilterDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_filter_hospitals, null);

        android.widget.EditText etHospitalName = view.findViewById(R.id.etFilterHospitalName);
        android.widget.EditText etAuthorityName = view.findViewById(R.id.etFilterAuthorityName);
        android.widget.EditText etRegistrationNumber = view.findViewById(R.id.etFilterRegistrationNumber);
        android.widget.EditText etState = view.findViewById(R.id.etFilterState);
        android.widget.EditText etCity = view.findViewById(R.id.etFilterCity);
        android.widget.EditText etStreet = view.findViewById(R.id.etFilterStreet);
        android.widget.EditText etLandmark = view.findViewById(R.id.etFilterLandmark);
        android.widget.EditText etPincode = view.findViewById(R.id.etFilterPincode);
        android.widget.Spinner spinnerHospitalType = view.findViewById(R.id.spinnerFilterHospitalType);
        android.widget.Spinner spinnerFilterFacility = view.findViewById(R.id.spinnerFilterFacility);

        // Set up hospital type spinner
        ArrayAdapter<CharSequence> hospitalAdapter = ArrayAdapter.createFromResource(this,
                R.array.hospital_types_filter, android.R.layout.simple_spinner_item);
        hospitalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHospitalType.setAdapter(hospitalAdapter);

        // Set up facility spinner
        ArrayAdapter<CharSequence> facilityAdapter = ArrayAdapter.createFromResource(this,
                R.array.facility_types_filter, android.R.layout.simple_spinner_item);
        facilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterFacility.setAdapter(facilityAdapter);

        // Set current filter values
        etHospitalName.setText(filterHospitalName);
        etAuthorityName.setText(filterAuthorityName);
        etRegistrationNumber.setText(filterRegistrationNumber);
        etState.setText(filterState);
        etCity.setText(filterCity);
        etStreet.setText(filterStreet);
        etLandmark.setText(filterLandmark);
        etPincode.setText(filterPincode);

        // Set spinner selections
        if (!filterHospitalType.isEmpty()) {
            int position = hospitalAdapter.getPosition(filterHospitalType);
            if (position >= 0) {
                spinnerHospitalType.setSelection(position);
            }
        }

        if (!filterFacility.isEmpty()) {
            int position = facilityAdapter.getPosition(filterFacility);
            if (position >= 0) {
                spinnerFilterFacility.setSelection(position);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Filter Hospitals")
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> {
                    filterHospitalName = etHospitalName.getText().toString().trim();
                    filterAuthorityName = etAuthorityName.getText().toString().trim();
                    filterRegistrationNumber = etRegistrationNumber.getText().toString().trim();
                    filterState = etState.getText().toString().trim();
                    filterCity = etCity.getText().toString().trim();
                    filterStreet = etStreet.getText().toString().trim();
                    filterLandmark = etLandmark.getText().toString().trim();
                    filterPincode = etPincode.getText().toString().trim();
                    
                    String selectedHospitalType = spinnerHospitalType.getSelectedItem().toString();
                    filterHospitalType = selectedHospitalType.equals("All Types") ? "" : selectedHospitalType;
                    
                    String selectedFacility = spinnerFilterFacility.getSelectedItem().toString();
                    filterFacility = selectedFacility.equals("All Facilities") ? "" : selectedFacility;
                    
                    applyFilters();
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    filterHospitalName = "";
                    filterAuthorityName = "";
                    filterRegistrationNumber = "";
                    filterState = "";
                    filterCity = "";
                    filterStreet = "";
                    filterLandmark = "";
                    filterHospitalType = "";
                    filterPincode = "";
                    filterFacility = "";
                    applyFilters();
                });

        builder.show();
    }

    private void showEmptyState() {
        llEmptyState.setVisibility(View.VISIBLE);
        rvHospitals.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        llEmptyState.setVisibility(View.GONE);
        rvHospitals.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
