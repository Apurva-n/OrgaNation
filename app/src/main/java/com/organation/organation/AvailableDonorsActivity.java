package com.organation.organation;

import android.os.Bundle;
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

public class AvailableDonorsActivity extends AppCompatActivity {
    private RecyclerView rvDonors;
    private DonorAdapter donorAdapter;
    private List<DonorModel> donorList;
    private List<DonorModel> allDonors;
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
        setContentView(R.layout.activity_available_donors);

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Donors");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvDonors = findViewById(R.id.rvDonors);
        donorList = new ArrayList<>();
        allDonors = new ArrayList<>();
        donorAdapter = new DonorAdapter(this, donorList);

        rvDonors.setLayoutManager(new LinearLayoutManager(this));
        rvDonors.setAdapter(donorAdapter);

        db = FirebaseFirestore.getInstance();

        loadDonors();

        // Setup filter button
        findViewById(R.id.fabFilter).setOnClickListener(v -> showFilterDialog());
    }

    private void loadDonors() {
        db.collection("donors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allDonors.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonorModel donor = parseDocumentToDonor(document);
                        if (donor != null && hasAvailableOrgans(donor)) {
                            allDonors.add(donor);
                        }
                    }

                    applyFilters();

                    if (donorList.isEmpty()) {
                        Toast.makeText(AvailableDonorsActivity.this,
                                "No available donors found",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AvailableDonorsActivity.this,
                                "Found " + donorList.size() + " available donors",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AvailableDonorsActivity.this,
                            "Error fetching donors: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Check if donor has any available organs for donation
     * Filters out donors with N/A or empty organ lists
     */
    private boolean hasAvailableOrgans(DonorModel donor) {
        String organs = donor.getOrgansToNDonate();
        if (organs == null || organs.trim().isEmpty() || organs.equals("N/A")) {
            return false;
        }
        return true;
    }

    private void applyFilters() {
        donorList.clear();
        for (DonorModel donor : allDonors) {
            if (!matchesFilter(donor)) continue;
            donorList.add(donor);
        }
        donorAdapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(DonorModel donor) {
        if (!filterState.isEmpty() && !donor.getState().toLowerCase().contains(filterState.toLowerCase())) {
            return false;
        }
        if (!filterCity.isEmpty() && !donor.getCity().toLowerCase().contains(filterCity.toLowerCase())) {
            return false;
        }
        if (!filterBloodGroup.isEmpty() && !donor.getBloodGroup().toLowerCase().contains(filterBloodGroup.toLowerCase())) {
            return false;
        }
        if (!filterOrgans.isEmpty()) {
            String organs = donor.getOrgansToNDonate();
            if (organs == null || !organs.toLowerCase().contains(filterOrgans.toLowerCase())) {
                return false;
            }
        }

        if (filterMinHeight != null) {
            try {
                int height = Integer.parseInt(donor.getHeight());
                if (height < filterMinHeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMaxHeight != null) {
            try {
                int height = Integer.parseInt(donor.getHeight());
                if (height > filterMaxHeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMinWeight != null) {
            try {
                int weight = Integer.parseInt(donor.getWeight());
                if (weight < filterMinWeight) return false;
            } catch (Exception ignored) {
            }
        }

        if (filterMaxWeight != null) {
            try {
                int weight = Integer.parseInt(donor.getWeight());
                if (weight > filterMaxWeight) return false;
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    private DonorModel parseDocumentToDonor(QueryDocumentSnapshot document) {
        try {
            Map<String, Object> data = document.getData();

            DonorModel donor = new DonorModel();
            donor.setAadhaarNo(getString(data, "02]Aadhaar_no"));
            donor.setFullName(getString(data, "01]Full_name"));
            donor.setAge(getString(data, "04]Age"));
            donor.setGender(getString(data, "05]Gender"));
            donor.setBloodGroup(getString(data, "06]Blood_group"));
            donor.setHeight(getString(data, "07]Height"));
            donor.setWeight(getString(data, "08]Weight"));
            donor.setPhone(getString(data, "09]Phone"));
            donor.setEmail(getString(data, "10]Email"));
            donor.setState(getString(data, "11]State"));
            donor.setCity(getString(data, "12]City"));
            donor.setStreet(getString(data, "13]Street"));
            donor.setLandmark(getString(data, "14]Landmark"));
            donor.setOrgansToNDonate(getString(data, "16]Organs_to_donate"));
            donor.setMedicalConditions(getString(data, "17]Medical_conditions"));
            donor.setPreviousSurgeries(getString(data, "18]Previous_surgeries"));
            donor.setDateOfBirth(getString(data, "03]Dob"));

            // Parse emergency contact
            if (data.containsKey("15]Emergency_contact")) {
                Map<String, Object> emergencyMap = (Map<String, Object>) data.get("15]Emergency_contact");
                if (emergencyMap != null) {
                    Map<String, String> emergency = new HashMap<>();
                    emergency.put("name", getString(emergencyMap, "01]Name"));
                    emergency.put("phone", getString(emergencyMap, "02]Phone"));
                    emergency.put("relation", getString(emergencyMap, "03]Relation"));
                    donor.setEmergencyContact(emergency);
                }
            }

            return donor;
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
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_filter_donors, null);

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
                .setTitle("Filter Donors")
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
