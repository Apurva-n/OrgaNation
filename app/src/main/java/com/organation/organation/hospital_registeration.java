package com.organation.organation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultCallback;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class hospital_registeration extends AppCompatActivity {

    // File picker launcher
    private ActivityResultLauncher<String> filePickerLauncher;
    
    // Form fields
    private EditText etHospitalName, etAuthorityName, etContactNumber, etOfficialEmail;
    private EditText etStreet, etCity, etState, etLandmark;
    private EditText etGovRegNumber, etAuthorityContact, etAuthorityEmail;
    private EditText etEstYear, etWebsiteUrl, etPincode, etLatitude, etLongitude;
    private Spinner spinnerHospitalType;
    private Button btnRegisterHospital;
    
    // Facility checkboxes
    private android.widget.CheckBox cbOrganTransplant, cbICU, cbEmergency, cbOrganStorage, cbLaboratory, cbAmbulance;
    
    // Terms and conditions
    private android.widget.CheckBox checkboxHospitalTermsConditions;
    private TextView tvHospitalTermsConditions;
    
    // Terms acceptance flag
    private boolean termsAccepted = false;
    
    // ID proof
    private Button btnUploadCertificate;
    private android.net.Uri certificateUri = null;

    // Data variables
    private String hospitalName, authorityName, contactNumber, officialEmail;
    private String street, city, state, landmark;
    private String govRegNumber, authorityContact, authorityEmail, hospitalType;
    private String hospitalId, estYear, websiteUrl, pincode, latitude, longitude;
    private Map<String, Boolean> facilities;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Validation patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern GOV_REG_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");

    private void initializeFilePicker() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                certificateUri = uri;
                btnUploadCertificate.setText("Certificate Uploaded");
                Toast.makeText(hospital_registeration.this, "Certificate selected successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_registeration);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize file picker
        initializeFilePicker();

        initializeViews();
        setupSpinner();
        setupListeners();
        setupRealTimeValidation();
        
        // Check if this is a profile update or new registration
        checkAndUpdateUI();
    }

    private void checkAndUpdateUI() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isProfileComplete = documentSnapshot.getBoolean("isProfileComplete");
                            if (isProfileComplete != null && isProfileComplete) {
                                // This is a profile update - load existing data
                                loadExistingHospitalData(uid);
                                btnRegisterHospital.setText("Update Profile");
                            } else {
                                // This is new registration - generate hospital ID
                                generateHospitalId();
                                btnRegisterHospital.setText("Register Hospital");
                            }
                        } else {
                            // No user document - generate new hospital ID
                            generateHospitalId();
                            btnRegisterHospital.setText("Register Hospital");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error checking profile - generate new hospital ID
                        generateHospitalId();
                        btnRegisterHospital.setText("Register Hospital");
                    });
        } else {
            // No current user - generate new hospital ID
            generateHospitalId();
            btnRegisterHospital.setText("Register Hospital");
        }
    }

    private void loadExistingHospitalData(String uid) {
        // First get the hospital ID from user document
        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        // Try to get hospital ID from user document or search for it
                        String hospitalId = userDocument.getString("hospitalId");
                        if (hospitalId == null) {
                            // If not in user document, search for hospital document
                            searchForHospitalDocument(uid);
                        } else {
                            loadHospitalData(hospitalId);
                        }
                    } else {
                        // No user document - search for hospital by UID
                        searchForHospitalDocument(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error - search for hospital by UID
                    searchForHospitalDocument(uid);
                });
    }

    private void searchForHospitalDocument(String uid) {
        // Search for hospital document by user UID
        db.collection("hospitals")
                .whereEqualTo("userUid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Found the hospital document
                        String hospitalDocId = querySnapshot.getDocuments().get(0).getId();
                        loadHospitalData(hospitalDocId);
                    } else {
                        // No hospital found - generate new ID
                        generateHospitalId();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error - generate new ID
                    generateHospitalId();
                });
    }

    private void loadHospitalData(String hospitalId) {
        db.collection("hospitals").document(hospitalId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load all hospital data into form fields
                        populateFormFields(documentSnapshot);
                        this.hospitalId = hospitalId;
                        
                        // Update the hospital ID field
                        EditText etHospitalId = findViewById(R.id.etHospitalId);
                        if (etHospitalId != null) {
                            etHospitalId.setText(hospitalId);
                            etHospitalId.setEnabled(false);
                            etHospitalId.setFocusable(false);
                            etHospitalId.setClickable(false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Error loading - generate new ID
                    generateHospitalId();
                });
    }

    private void populateFormFields(com.google.firebase.firestore.DocumentSnapshot document) {
        // Basic Information
        etHospitalName.setText(getStringFromDocument(document, "01]Hospital_Name"));
        etAuthorityName.setText(getStringFromDocument(document, "02]Authority_Name"));
        etContactNumber.setText(getStringFromDocument(document, "03]Contact_Number"));
        etOfficialEmail.setText(getStringFromDocument(document, "04]Official_Email"));
        etStreet.setText(getStringFromDocument(document, "05]Street"));
        etCity.setText(getStringFromDocument(document, "06]City"));
        etState.setText(getStringFromDocument(document, "07]State"));
        etLandmark.setText(getStringFromDocument(document, "08]Landmark"));
        etGovRegNumber.setText(getStringFromDocument(document, "09]Gov_Reg_Number"));
        etAuthorityContact.setText(getStringFromDocument(document, "10]Authority_Contact"));
        etAuthorityEmail.setText(getStringFromDocument(document, "11]Authority_Email"));
        
        // Hospital Type
        String hospitalType = getStringFromDocument(document, "12]Hospital_Type");
        if (spinnerHospitalType != null && hospitalType != null && !hospitalType.equals("N/A")) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerHospitalType.getAdapter();
            int position = adapter.getPosition(hospitalType);
            if (position >= 0) {
                spinnerHospitalType.setSelection(position);
            }
        }
        
        // Additional Details
        estYear = getStringFromDocument(document, "16]Establishment_Year");
        websiteUrl = getStringFromDocument(document, "17]Website_URL");
        pincode = getStringFromDocument(document, "18]Pincode");
        latitude = getStringFromDocument(document, "19]Latitude");
        longitude = getStringFromDocument(document, "20]Longitude");
        
        etEstYear.setText(estYear);
        etWebsiteUrl.setText(websiteUrl);
        etPincode.setText(pincode);
        etLatitude.setText(latitude);
        etLongitude.setText(longitude);
        
        // Facilities
        @SuppressWarnings("unchecked")
        Map<String, Boolean> facilities = getMapFromDocument(document, "21]Facilities");
        if (facilities != null && facilities instanceof Map) {
            cbOrganTransplant.setChecked(facilities.getOrDefault("organ_transplant", false));
            cbICU.setChecked(facilities.getOrDefault("icu", false));
            cbEmergency.setChecked(facilities.getOrDefault("emergency", false));
            cbOrganStorage.setChecked(facilities.getOrDefault("organ_storage", false));
            cbLaboratory.setChecked(facilities.getOrDefault("laboratory", false));
            cbAmbulance.setChecked(facilities.getOrDefault("ambulance", false));
        }
    }

    private Map<String, Boolean> getMapFromDocument(com.google.firebase.firestore.DocumentSnapshot document, String key) {
        Object value = document.get(FieldPath.of(key));
        if (value instanceof Map) {
            return (Map<String, Boolean>) value;
        }
        return new HashMap<>();
    }

    private String getStringFromDocument(com.google.firebase.firestore.DocumentSnapshot document, String key) {
        Object value = document.get(FieldPath.of(key));
        return value != null ? value.toString() : "";
    }

    private void initializeViews() {
        etHospitalName = findViewById(R.id.etHospitalName);
        etAuthorityName = findViewById(R.id.etAuthorityName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etOfficialEmail = findViewById(R.id.etOfficialEmail);
        etStreet = findViewById(R.id.etStreet);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etLandmark = findViewById(R.id.etLandmark);
        etGovRegNumber = findViewById(R.id.etGovRegNumber);
        etAuthorityContact = findViewById(R.id.etAuthorityContact);
        etAuthorityEmail = findViewById(R.id.etAuthorityEmail);
        
        // Additional fields (excluding hospital ID as it's auto-generated)
        etEstYear = findViewById(R.id.etEstYear);
        etWebsiteUrl = findViewById(R.id.etWebsiteUrl);
        etPincode = findViewById(R.id.etPincode);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        
        spinnerHospitalType = findViewById(R.id.spinnerHospitalType);
        btnRegisterHospital = findViewById(R.id.btnRegisterHospital);
        btnUploadCertificate = findViewById(R.id.btnUploadCertificate);
        
        // Facility checkboxes
        cbOrganTransplant = findViewById(R.id.cbOrganTransplant);
        cbICU = findViewById(R.id.cbICU);
        cbEmergency = findViewById(R.id.cbEmergency);
        cbOrganStorage = findViewById(R.id.cbOrganStorage);
        cbLaboratory = findViewById(R.id.cbLaboratory);
        cbAmbulance = findViewById(R.id.cbAmbulance);
        
        // Terms and conditions
        checkboxHospitalTermsConditions = findViewById(R.id.checkboxHospitalTermsConditions);
        tvHospitalTermsConditions = findViewById(R.id.tvHospitalTermsConditions);
        
        // Initialize facilities map
        facilities = new HashMap<>();
    }

    private void generateHospitalId() {
        // Generate unique hospital ID
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        hospitalId = "HOSP_" + uuid;
        
        // Display the generated ID (read-only)
        EditText etHospitalId = findViewById(R.id.etHospitalId);
        if (etHospitalId != null) {
            etHospitalId.setText(hospitalId);
            etHospitalId.setEnabled(false); // Make it read-only
            etHospitalId.setFocusable(false);
            etHospitalId.setClickable(false);
        }
    }

    private void setupSpinner() {
        // Create adapter for hospital type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.hospital_types_filter,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHospitalType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRegisterHospital.setOnClickListener(v -> validateAndSubmit());
        
        // Terms and conditions checkbox listener
        checkboxHospitalTermsConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            termsAccepted = isChecked;
        });
        
        // Terms and conditions text click listener
        tvHospitalTermsConditions.setOnClickListener(v -> {
            Intent intent = new Intent(hospital_registeration.this, HospitalTermsConditionsActivity.class);
            startActivityForResult(intent, 1004); // Request code for terms
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1004) { // Hospital terms and conditions request code
            if (resultCode == RESULT_OK) {
                // User accepted terms and conditions
                checkboxHospitalTermsConditions.setChecked(true);
                termsAccepted = true;
                Toast.makeText(this, "Hospital Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User declined terms and conditions
                checkboxHospitalTermsConditions.setChecked(false);
                termsAccepted = false;
                Toast.makeText(this, "You must accept the terms and conditions to register", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openFilePicker() {
        filePickerLauncher.launch("application/pdf");
    }

    private void setupRealTimeValidation() {
        // Hospital name validation
        etHospitalName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3) {
                    etHospitalName.setError("Hospital name must be at least 3 characters");
                } else {
                    etHospitalName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Contact number validation
        etContactNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !PHONE_PATTERN.matcher(s).matches()) {
                    etContactNumber.setError("Mobile number must start with 6-9 and be 10 digits");
                } else {
                    etContactNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Official email validation
        etOfficialEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    etOfficialEmail.setError("Please enter a valid email address");
                } else {
                    etOfficialEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Government registration number validation
        etGovRegNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !GOV_REG_PATTERN.matcher(s).matches()) {
                    etGovRegNumber.setError("Registration number must be 6-20 alphanumeric characters");
                } else {
                    etGovRegNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // City validation
        etCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !s.toString().matches("^[a-zA-Z\\s]+$")) {
                    etCity.setError("City can only contain letters and spaces");
                } else {
                    etCity.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // State validation
        etState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !s.toString().matches("^[a-zA-Z\\s]+$")) {
                    etState.setError("State can only contain letters and spaces");
                } else {
                    etState.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Authority contact validation
        etAuthorityContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !PHONE_PATTERN.matcher(s).matches()) {
                    etAuthorityContact.setError("Mobile number must start with 6-9 and be 10 digits");
                } else {
                    etAuthorityContact.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Authority email validation
        etAuthorityEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    etAuthorityEmail.setError("Please enter a valid email address");
                } else {
                    etAuthorityEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Hospital name validation
        String hospitalNameText = etHospitalName.getText().toString().trim();
        if (TextUtils.isEmpty(hospitalNameText)) {
            etHospitalName.setError("Hospital name is required");
            errorMessage.append("• Hospital name is required\n");
            isValid = false;
        } else if (hospitalNameText.length() < 3) {
            etHospitalName.setError("Hospital name must be at least 3 characters");
            errorMessage.append("• Hospital name must be at least 3 characters\n");
            isValid = false;
        } else if (!hospitalNameText.matches("^[a-zA-Z0-9\\s]+$")) {
            etHospitalName.setError("Hospital name can only contain letters, numbers, and spaces");
            errorMessage.append("• Hospital name contains invalid characters\n");
            isValid = false;
        }

        // Authority name validation
        String authorityNameText = etAuthorityName.getText().toString().trim();
        if (TextUtils.isEmpty(authorityNameText)) {
            etAuthorityName.setError("Authority name is required");
            errorMessage.append("• Authority name is required\n");
            isValid = false;
        } else if (authorityNameText.length() < 3) {
            etAuthorityName.setError("Authority name must be at least 3 characters");
            errorMessage.append("• Authority name must be at least 3 characters\n");
            isValid = false;
        } else if (!authorityNameText.matches("^[a-zA-Z\\s]+$")) {
            etAuthorityName.setError("Authority name can only contain letters and spaces");
            errorMessage.append("• Authority name can only contain letters and spaces\n");
            isValid = false;
        }

        // Contact number validation
        String contactNumberText = etContactNumber.getText().toString().trim();
        if (TextUtils.isEmpty(contactNumberText)) {
            etContactNumber.setError("Contact number is required");
            errorMessage.append("• Contact number is required\n");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(contactNumberText).matches()) {
            etContactNumber.setError("Mobile number must start with 6-9 and be 10 digits");
            errorMessage.append("• Contact number must be a valid 10-digit mobile number\n");
            isValid = false;
        }

        // Official email validation
        String officialEmailText = etOfficialEmail.getText().toString().trim();
        if (TextUtils.isEmpty(officialEmailText)) {
            etOfficialEmail.setError("Official email is required");
            errorMessage.append("• Official email is required\n");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(officialEmailText).matches()) {
            etOfficialEmail.setError("Please enter a valid email address");
            errorMessage.append("• Official email format is invalid\n");
            isValid = false;
        }

        // Government registration number validation
        String govRegNumberText = etGovRegNumber.getText().toString().trim();
        if (TextUtils.isEmpty(govRegNumberText)) {
            etGovRegNumber.setError("Government registration number is required");
            errorMessage.append("• Government registration number is required\n");
            isValid = false;
        } else if (!GOV_REG_PATTERN.matcher(govRegNumberText).matches()) {
            etGovRegNumber.setError("Registration number must be 6-20 alphanumeric characters");
            errorMessage.append("• Registration number must be 6-20 alphanumeric characters\n");
            isValid = false;
        }

        // City validation
        String cityText = etCity.getText().toString().trim();
        if (TextUtils.isEmpty(cityText)) {
            etCity.setError("City is required");
            errorMessage.append("• City is required\n");
            isValid = false;
        } else if (!cityText.matches("^[a-zA-Z\\s]+$")) {
            etCity.setError("City can only contain letters and spaces");
            errorMessage.append("• City can only contain letters and spaces\n");
            isValid = false;
        }

        // State validation
        String stateText = etState.getText().toString().trim();
        if (TextUtils.isEmpty(stateText)) {
            etState.setError("State is required");
            errorMessage.append("• State is required\n");
            isValid = false;
        } else if (!stateText.matches("^[a-zA-Z\\s]+$")) {
            etState.setError("State can only contain letters and spaces");
            errorMessage.append("• State can only contain letters and spaces\n");
            isValid = false;
        }

        // Hospital type validation
        if (spinnerHospitalType.getSelectedItem() == null || 
            spinnerHospitalType.getSelectedItemPosition() == 0) {
            errorMessage.append("• Please select a hospital type\n");
            isValid = false;
        }

        // Authority contact validation (optional but if provided, must be valid)
        String authorityContactText = etAuthorityContact.getText().toString().trim();
        if (!TextUtils.isEmpty(authorityContactText) && !PHONE_PATTERN.matcher(authorityContactText).matches()) {
            etAuthorityContact.setError("Mobile number must start with 6-9 and be 10 digits");
            errorMessage.append("• Authority contact number is invalid\n");
            isValid = false;
        }

        // Authority email validation (optional but if provided, must be valid)
        String authorityEmailText = etAuthorityEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(authorityEmailText) && !Patterns.EMAIL_ADDRESS.matcher(authorityEmailText).matches()) {
            etAuthorityEmail.setError("Please enter a valid email address");
            errorMessage.append("• Authority email format is invalid\n");
            isValid = false;
        }

        // Establishment year validation (optional but if provided, must be valid)
        String estYearText = etEstYear.getText().toString().trim();
        if (!TextUtils.isEmpty(estYearText)) {
            try {
                int year = Integer.parseInt(estYearText);
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (year < 1900 || year > currentYear) {
                    etEstYear.setError("Please enter a valid year");
                    errorMessage.append("• Establishment year must be between 1900 and " + currentYear + "\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etEstYear.setError("Please enter a valid year");
                errorMessage.append("• Establishment year format is invalid\n");
                isValid = false;
            }
        }

        // Pincode validation (optional but if provided, must be valid)
        String pincodeText = etPincode.getText().toString().trim();
        if (!TextUtils.isEmpty(pincodeText) && !pincodeText.matches("^[1-9][0-9]{5}$")) {
            etPincode.setError("Please enter a valid 6-digit pincode");
            errorMessage.append("• Pincode must be a valid 6-digit number\n");
            isValid = false;
        }

        // Coordinates validation (optional but if provided, must be valid)
        String latitudeText = etLatitude.getText().toString().trim();
        String longitudeText = etLongitude.getText().toString().trim();
        
        if (!TextUtils.isEmpty(latitudeText)) {
            try {
                double lat = Double.parseDouble(latitudeText);
                if (lat < -90 || lat > 90) {
                    etLatitude.setError("Latitude must be between -90 and 90");
                    errorMessage.append("• Latitude must be between -90 and 90\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etLatitude.setError("Please enter a valid latitude");
                errorMessage.append("• Latitude format is invalid\n");
                isValid = false;
            }
        }
        
        if (!TextUtils.isEmpty(longitudeText)) {
            try {
                double lon = Double.parseDouble(longitudeText);
                if (lon < -180 || lon > 180) {
                    etLongitude.setError("Longitude must be between -180 and 180");
                    errorMessage.append("• Longitude must be between -180 and 180\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etLongitude.setError("Please enter a valid longitude");
                errorMessage.append("• Longitude format is invalid\n");
                isValid = false;
            }
        }

        // Certificate upload is now optional
        // Commented out the compulsory validation
        // if (certificateUri == null) {
        //     errorMessage.append("• Please upload hospital certificate/identity proof\n");
        //     isValid = false;
        // }

        // Validate terms and conditions
        if (!checkboxHospitalTermsConditions.isChecked()) {
            errorMessage.append("• Please accept the Hospital Terms and Conditions\n");
            isValid = false;
        }

        // Show specific error message if validation fails
        if (!isValid) {
            new AlertDialog.Builder(this)
                .setTitle("Validation Errors")
                .setMessage(errorMessage.toString())
                .setPositiveButton("OK", null)
                .show();
        }

        return isValid;
    }

    private void validateAndSubmit() {
        if (!validateForm()) {
            return;
        }

        // Collect form data
        collectFormData();

        // Show confirmation dialog
        String buttonText = btnRegisterHospital.getText().toString();
        String title = buttonText.equals("Update Profile") ? "Confirm Update" : "Confirm Registration";
        String message = buttonText.equals("Update Profile") ? 
                "Are you sure you want to update " + hospitalName + "'s profile?" :
                "Are you sure you want to register " + hospitalName + "?";
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", (dialog, which) -> saveDataToFirestore())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openHospitalTermsAndConditions() {
        Intent intent = new Intent(this, HospitalTermsConditionsActivity.class);
        startActivityForResult(intent, 1004); // Request code for hospital terms and conditions
    }

    private void collectFormData() {
        hospitalName = etHospitalName.getText().toString().trim();
        authorityName = etAuthorityName.getText().toString().trim();
        contactNumber = etContactNumber.getText().toString().trim();
        officialEmail = etOfficialEmail.getText().toString().trim();
        street = etStreet.getText().toString().trim();
        city = etCity.getText().toString().trim();
        state = etState.getText().toString().trim();
        landmark = etLandmark.getText().toString().trim();
        govRegNumber = etGovRegNumber.getText().toString().trim();
        authorityContact = etAuthorityContact.getText().toString().trim();
        authorityEmail = etAuthorityEmail.getText().toString().trim();
        
        // Additional fields
        estYear = etEstYear.getText().toString().trim();
        websiteUrl = etWebsiteUrl.getText().toString().trim();
        pincode = etPincode.getText().toString().trim();
        latitude = etLatitude.getText().toString().trim();
        longitude = etLongitude.getText().toString().trim();
        
        // Safe spinner selection - FIXED THE NULL POINTER EXCEPTION
        if (spinnerHospitalType.getSelectedItem() != null) {
            hospitalType = spinnerHospitalType.getSelectedItem().toString();
        } else {
            hospitalType = "Unknown";
        }
        
        // Collect facilities
        facilities.clear();
        facilities.put("organ_transplant", cbOrganTransplant.isChecked());
        facilities.put("icu", cbICU.isChecked());
        facilities.put("emergency", cbEmergency.isChecked());
        facilities.put("organ_storage", cbOrganStorage.isChecked());
        facilities.put("laboratory", cbLaboratory.isChecked());
        facilities.put("ambulance", cbAmbulance.isChecked());
    }

    private void saveDataToFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> hospitalData = new HashMap<>();
        
        // Basic Information
        hospitalData.put("01]Hospital_Name", hospitalName);
        hospitalData.put("02]Authority_Name", authorityName);
        hospitalData.put("03]Contact_Number", contactNumber);
        hospitalData.put("04]Official_Email", officialEmail);
        hospitalData.put("05]Street", street);
        hospitalData.put("06]City", city);
        hospitalData.put("07]State", state);
        hospitalData.put("08]Landmark", landmark);
        hospitalData.put("09]Gov_Reg_Number", govRegNumber);
        hospitalData.put("10]Authority_Contact", authorityContact);
        hospitalData.put("11]Authority_Email", authorityEmail);
        hospitalData.put("12]Hospital_Type", hospitalType);
        
        // Additional Hospital Details
        hospitalData.put("15]Hospital_ID", hospitalId); // Auto-generated ID
        hospitalData.put("16]Establishment_Year", estYear);
        hospitalData.put("17]Website_URL", websiteUrl);
        hospitalData.put("18]Pincode", pincode);
        hospitalData.put("19]Latitude", latitude);
        hospitalData.put("20]Longitude", longitude);
        
        // Facilities Available (stored as map only to avoid duplication)
        hospitalData.put("21]Facilities", facilities);
        
        // Metadata
        hospitalData.put("13]registration_timestamp", com.google.firebase.Timestamp.now());
        hospitalData.put("14]isProfileComplete", true);
        hospitalData.put("userUid", uid); // Add user UID for profile updates
        hospitalData.put("verificationStatus", "pending"); // NEW: Add verification status
        hospitalData.put("registrationDate", new java.util.Date()); // NEW: Add registration date

        // Save to "hospitals" collection using hospitalId as primary key
        db.collection("hospitals").document(hospitalId).set(hospitalData)
                .addOnSuccessListener(aVoid -> {
                    // Update the "users" collection document
                    db.collection("users").document(uid)
                            .update("isProfileComplete", true, "userType", "Hospital", "hospitalId", hospitalId)
                            .addOnSuccessListener(aVoid2 -> {
                                showRegistrationPendingSuccess();
                                sendConfirmationSms();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Hospital registered successfully, but profile update failed", Toast.LENGTH_LONG).show();
                                showRegistrationSuccess();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving hospital data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendConfirmationSms() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.SEND_SMS}, 1);
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "Your hospital " + hospitalName + " has been successfully registered with OrgaNation. Hospital ID: " + hospitalId;
            smsManager.sendTextMessage(contactNumber, null, message, null, null);
        } catch (Exception e) {
            // SMS failed but registration succeeded
        }
    }

    private void showRegistrationPendingSuccess() {
        String title = "Registration Submitted Successfully!";
        String message = "Hospital " + hospitalName + " has been registered successfully.\n\n" +
                "Your registration is now pending verification by our admin team.\n\n" +
                "You will receive email confirmation once your account is approved.\n\n" +
                "Please wait for admin approval before accessing the dashboard.\n\n" +
                "Hospital ID: " + hospitalId;
        
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false) // Prevent dialog from being dismissed
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to login or stay on registration
                    Intent intent = new Intent(hospital_registeration.this, Donor_login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showRegistrationSuccess() {
        String buttonText = btnRegisterHospital.getText().toString();
        boolean isUpdate = buttonText.equals("Update Profile");
        
        String title = isUpdate ? "Profile Updated Successfully!" : "Registration Successful!";
        String message = isUpdate ? 
                "Hospital " + hospitalName + "'s profile has been successfully updated.\n\nHospital ID: " + hospitalId :
                "Hospital " + hospitalName + " has been successfully registered.\n\nHospital ID: " + hospitalId + "\n\nThis ID will be used to identify your hospital in the system.";
        
        String buttonLabel = isUpdate ? "Go to Dashboard" : "Go to Dashboard";
        
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonLabel, (dialog, which) -> {
                    Intent intent = new Intent(hospital_registeration.this, hospital_main_page.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
