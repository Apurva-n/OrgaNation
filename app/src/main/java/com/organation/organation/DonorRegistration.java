package com.organation.organation;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DonorRegistration extends AppCompatActivity {
    private androidx.activity.result.ActivityResultLauncher<String> mGetContent;
    private ImageView ivProfilePhoto;
    private android.net.Uri profileImageUri = null;

    // Personal Information Fields
    private EditText full_name, donor_age, donor_dob,donor_adhaar,Height,Weight,DonorState,DonorCity,DonorStreet,DonorLandmark;
    private Spinner spinnerGender, spinnerBloodGroup,donor_relation;

    // Contact Information Fields
    private EditText donor_phone, donor_Email;

    // Donation Details Fields
    private MultiAutoCompleteTextView donor_organs;
    private EditText donor_MedicalConditions,donor_PreviousSurgeries,donor_EmergencyPhone,donor_EmergencyName;

    // UI Components
    private LinearLayout btnRegister, llDobPicker;
    private TextView tvLoginNow, tvTermsConditions;
    private CheckBox checkboxTermsConditions;

    // ============================================
    // DATA STORAGE VARIABLES
    // ============================================
    private String donor_adhaar_no,donor_full_name, donor_gender, donor_bloodGroup, donors_dob, donors_age;
    private String donor_height,donor_weight,donor_medical_condition,donor_previous_surgery;
    private String donor_city,donor_street,donor_state,donor_landmark;

    //emergency details
    private String donor_emergency_phno,donor_emergency_name,donor_emergency_relation;

    // Contact Information Storage
    private String donor_phone_no,donor_email_id;

    // Donation Details Storage
    private String organsToDonate;

   // ANDROID LIFECYCLE METHODS //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donor_registration);
        initializeViews();

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        FrameLayout flProfilePhoto = findViewById(R.id.flProfilePhoto);

        mGetContent = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageUri = uri; // Store the URI for validation
                        com.bumptech.glide.Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(ivProfilePhoto);
                    }
                }
        );

        // Set the click listener
        flProfilePhoto.setOnClickListener(v -> {
            // This opens the system file picker for images
            mGetContent.launch("image/*");
        });

        setupSpinners();
        setupOrganSelection();
        setupListenersAndValidation();
    }

    private void initializeViews() {
        // Personal Information Fields
        donor_adhaar=findViewById(R.id.donor_adhaar);
        full_name = findViewById(R.id.full_name);
        donor_age = findViewById(R.id.donor_age);
        donor_dob = findViewById(R.id.donor_dob);
        Height=findViewById(R.id.Height);
        Weight=findViewById(R.id.Weight);

        //spinners
        spinnerGender = findViewById(R.id.select_gender);
        spinnerBloodGroup = findViewById(R.id.donor_BloodGroup);

        // Contact Information Fields
        donor_phone = findViewById(R.id.donor_phone);
        donor_Email = findViewById(R.id.donor_Email);

        // Donation Details Fields
        donor_organs = findViewById(R.id.donor_organs);
        donor_MedicalConditions = findViewById(R.id.donor_MedicalConditions);
        donor_PreviousSurgeries=findViewById(R.id.donor_PreviousSurgeries);

        //emergency details of donor
        donor_EmergencyPhone=findViewById(R.id.donor_EmergencyPhone);
        donor_EmergencyName=findViewById(R.id.donor_EmergencyName);
        donor_relation=findViewById(R.id.donor_relation);

        //Donor Address details
        DonorState=findViewById(R.id.DonorState);
        DonorCity=findViewById(R.id.DonorCity);
        DonorStreet=findViewById(R.id.DonorStreet);
        DonorLandmark=findViewById(R.id.DonorLandmark);

        // UI Components
        btnRegister = findViewById(R.id.btnRegisterDonor);
        llDobPicker = findViewById(R.id.llDobPicker);
        tvLoginNow = findViewById(R.id.tvLoginNow);
        checkboxTermsConditions = findViewById(R.id.checkboxTermsConditions);
        tvTermsConditions = findViewById(R.id.tvTermsConditions);
    }

    private void setupSpinners() {
        // Setup Gender Spinner
        String[] genderOptions = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            genderOptions
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Setup Blood Group Spinner
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            bloodGroups
        );
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodAdapter);
    }


    private void setupOrganSelection() {
        // List of organs available for donation
        String[] organList = {
            "Kidney", "Liver", "Heart", "Lungs", "Pancreas",
            "Eyes", "Skin", "Bone Marrow", "Intestine", "Cornea"
        };

        // Create adapter for multi-select organ dropdown
        ArrayAdapter<String> organAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            organList
        );

        // Set adapter to MultiAutoCompleteTextView
        donor_organs.setAdapter(organAdapter);

        // Show dropdown after typing 1 character
        donor_organs.setThreshold(1);

        // Allow multiple organ selection separated by comma
        donor_organs.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }


    private void setupListenersAndValidation() {
        // Date picker click listener
        llDobPicker.setOnClickListener(v -> openDatePicker());

        // Register button click listener
        btnRegister.setOnClickListener(v -> validateAndSubmitForm());

        // Login link click listener
        tvLoginNow.setOnClickListener(v -> navigateToLogin());

        // Terms and conditions click listener
        tvTermsConditions.setOnClickListener(v -> openTermsAndConditions());

        // Real-time validation for text fields
        setupRealTimeValidation();
    }


    private void setupRealTimeValidation() {

        // Adhaar validation
        donor_adhaar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAdhaar();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // full Name Validation
        full_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFirstName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email Validation
        donor_Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Phone Validation
        donor_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openDatePicker() {
        // Get current date for default selection
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                // Format date as DD/MM/YYYY
                String formattedDate = String.format("%02d/%02d/%d",
                    selectedDayOfMonth, selectedMonth + 1, selectedYear);

                // Set date in EditText
                donor_dob.setText(formattedDate);

                // Calculate and set age automatically
                int calculatedAge = calculateAge(selectedYear, selectedMonth, selectedDayOfMonth);
                donor_age.setText(String.valueOf(calculatedAge));
            },
            year, month, day
        );

        // Restrict date selection to past dates only
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Show the dialog
        datePickerDialog.show();
    }


    private int calculateAge(int birthYear, int birthMonth, int birthDay) {
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - birthYear;

        // Adjust age if birthday hasn't occurred this year yet
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--;
        }

        return age;
    }

    private boolean validateAdhaar() {
        donor_adhaar_no = donor_adhaar.getText().toString().trim();

        if (TextUtils.isEmpty(donor_adhaar_no)) {
            donor_adhaar.setError(" Valid Ahaar no  is required");
            return false;
        }

        if (!donor_adhaar_no.matches("^[2-9]{1}[0-9]{3}\\s?[0-9]{4}\\s?[0-9]{4}$")) {
            donor_adhaar.setError("Enter valid adhaar no as on adhaar card");
            return false;
        }

        donor_adhaar.setError(null);
        return true;
    }

    private boolean validateFirstName() {
        donor_full_name = full_name.getText().toString().trim();

        if (TextUtils.isEmpty(donor_full_name)) {
            full_name.setError("Full name is required");
            return false;
        }

        if (donor_full_name.length() < 6) {
            full_name.setError("Invalid full name");
            return false;
        }

        if (!donor_full_name.matches("^[a-zA-Z]+(?:\\s[a-zA-Z]+)+$")) {
            full_name.setError("Enter valid full name");
            return false;
        }

        full_name.setError(null);
        return true;
    }
    private boolean validateEmergencyName() {
        donor_emergency_name=donor_EmergencyName.getText().toString().trim();

        if (TextUtils.isEmpty(donor_emergency_name)) {
            donor_EmergencyName.setError("Full name is required");
            return false;
        }

        if (donor_emergency_name.length() < 6) {
            donor_EmergencyName.setError("Invalid full name");
            return false;
        }

        if (!donor_emergency_name.matches("^[a-zA-Z]+(?:\\s[a-zA-Z]+)+$")) {
            donor_EmergencyName.setError("Enter valid full name");
            return false;
        }

        donor_EmergencyName.setError(null);
        return true;
    }

    private boolean validateEmail() {
        donor_email_id = donor_Email.getText().toString().trim();

        if (TextUtils.isEmpty(donor_email_id)) {
            donor_Email.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(donor_email_id).matches()) {
            donor_Email.setError("Please enter a valid email address");
            return false;
        }

        donor_Email.setError(null);
        return true;
    }

    private boolean validatePhone() {
        donor_phone_no = donor_phone.getText().toString().trim();

        if (TextUtils.isEmpty(donor_phone_no)) {
            donor_phone.setError("Phone number is required");
            return false;
        }

        if (!donor_phone_no.matches("^[6-9]\\d{9}$")) {
            donor_phone.setError("Please enter a valid 10-digit phone number");
            return false;
        }

        donor_phone.setError(null);
        return true;
    }

    private boolean validateEmergencyPhone() {
        donor_emergency_phno=donor_EmergencyPhone.getText().toString().trim();

        if (TextUtils.isEmpty(donor_emergency_phno)) {
            donor_EmergencyPhone.setError("Phone number is required");
            return false;
        }

        if (!donor_emergency_phno.matches("^[6-9]\\d{9}$")) {
            donor_EmergencyPhone.setError("Please enter a valid 10-digit phone number");
            return false;
        }

        donor_EmergencyPhone.setError(null);
        return true;
    }

    private boolean validateAge() {
        donors_age = donor_age.getText().toString().trim();

        if (TextUtils.isEmpty(donors_age)) {
            donor_age.setError("Age is required");
            return false;
        }

        int ageValue = Integer.parseInt(donors_age);

        if (ageValue < 18 || ageValue > 95) {
            Toast.makeText(this, "Age criteria makes you ineligible for donation! sorry", Toast.LENGTH_SHORT).show();
            return false;
        }

        donor_age.setError(null);
        return true;
    }

    private boolean validateGender() {
        donor_gender = spinnerGender.getSelectedItem().toString();

        if (donor_gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private boolean validateHeight() {
        donor_height=Height.getText().toString().trim();


        if (TextUtils.isEmpty(donor_height)) {
            Height.setError("Height is required");
            return false;
        }

        int HeightValue = Integer.parseInt(donor_height);

        if (HeightValue < 100 || HeightValue > 210) {
            Toast.makeText(this, "Height criteria makes you ineligible for donation! sorry", Toast.LENGTH_SHORT).show();
            return false;
        }

        Height.setError(null);
        return true;
    }
    private boolean validateWeight() {
        donor_weight=Weight.getText().toString().trim();

        if (TextUtils.isEmpty(donor_weight)) {
            Weight.setError("Age is required");
            return false;
        }

        int WeightValue = Integer.parseInt(donor_weight);

        if (WeightValue < 40 || WeightValue > 120) {
            Toast.makeText(this, "Weight criteria makes you ineligible for donation! sorry", Toast.LENGTH_SHORT).show();
            return false;
        }

        Weight.setError(null);
        return true;
    }

    private boolean validateBloodGroup() {
        donor_bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        if (donor_bloodGroup.equals("Select Blood Group")) {
            Toast.makeText(this, "Please select your blood group", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateOrgans() {
        organsToDonate = donor_organs.getText().toString().trim();

        if (TextUtils.isEmpty(organsToDonate)) {
            donor_organs.setError("Please select at least one organ to donate");
            return false;
        }

        donor_organs.setError(null);
        return true;
    }
    private boolean validateRelation() {
        donor_emergency_relation=donor_relation.getSelectedItem().toString();

        if (donor_emergency_relation.equals("Select Relationship")) {
            Toast.makeText(this, "Please select your relation with the emergency contact person", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean validateProfilePicture() {
        if (profileImageUri == null) {
            Toast.makeText(this, "Please upload a profile picture", Toast.LENGTH_SHORT).show();
            // Optional: Add a red tint or shake animation to the FrameLayout
            return false;
        }
        return true;
    }

    private void validateAndSubmitForm() {
        // Store date of birth
         donors_dob = donor_dob.getText().toString().trim();
         donor_previous_surgery =donor_PreviousSurgeries.getText().toString().trim();
         donor_city =DonorCity.getText().toString().trim();
         donor_state=DonorState.getText().toString().trim();
         donor_landmark=DonorLandmark.getText().toString().trim();
         donor_street=DonorStreet.getText().toString().trim();
         donor_medical_condition = donor_MedicalConditions.getText().toString().trim();

        // Validate all required fields
        boolean isValid = true;

        if (!validateProfilePicture()) isValid = false;
        if (!validateAdhaar()) isValid = false;
        if (!validateFirstName()) isValid = false;
        if (!validateEmail()) isValid = false;
        if (!validatePhone()) isValid = false;
        if (!validateAge()) isValid = false;
        if (!validateGender()) isValid = false;
        if (!validateHeight()) isValid = false;
        if (!validateWeight()) isValid = false;
        if (!validateBloodGroup()) isValid = false;
        if (!validateOrgans()) isValid = false;
        if (!validateEmergencyName()) isValid = false;
        if (!validateEmergencyPhone()) isValid = false;
        if (!validateRelation()) isValid = false;
        if (!validateTermsAndConditions()) isValid = false;

        // Validate date of birth
        if (TextUtils.isEmpty(donors_dob)) {
            donor_dob.setError("Please enter your date of birth");
            isValid = false;
        }
        if (TextUtils.isEmpty(donor_state)) {
            DonorState.setError("Please select your state");
            isValid = false;
        }

        if (TextUtils.isEmpty(donor_city)) {
            DonorCity.setError("Please select your city");
            isValid = false;
        }
        if (TextUtils.isEmpty(donor_street)) {
            DonorStreet.setError("Please select your street");
            isValid = false;
        }

        if (TextUtils.isEmpty(donor_landmark)) {
            DonorLandmark.setError("Please select your nearest landmark");
            isValid = false;
        }

        if (TextUtils.isEmpty(donor_medical_condition)) {
            Toast.makeText(this, "Please enter details or NA", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (TextUtils.isEmpty(donor_previous_surgery)) {
            donor_PreviousSurgeries.setError("Please enter details or NA");
            isValid = false;
        }

        // If all validations pass, proceed with form submission
        if (isValid) {
            submitDonorRegistration();
        }
    }

    private void submitDonorRegistration() {
        // Show confirmation dialog before submission
        showConfirmationDialog();
    }


    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm Registration")
            .setMessage("Are you sure you want to register as an organ donor?\n\n" +
                       "Your information will help save lives.")
            .setPositiveButton("Confirm", (dialog, which) -> {
                // Process the registration
                processRegistration();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void processRegistration() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Get the current user's UID from Firebase Auth
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> donorData = new HashMap<>();
        // Personal Info
        donorData.put("01]Full_name", donor_full_name);
        donorData.put("02]Aadhaar_no", donor_adhaar_no);
        donorData.put("03]Dob", donors_dob);
        donorData.put("04]Age", donors_age);
        donorData.put("05]Gender", donor_gender);
        donorData.put("06]Blood_group", donor_bloodGroup);
        donorData.put("07]Height", donor_height);
        donorData.put("08]Weight", donor_weight);

        // Contact & Address
        donorData.put("09]Phone", donor_phone_no);
        donorData.put("10]Email", donor_email_id);
        donorData.put("11]State", donor_state);
        donorData.put("12]City", donor_city);
        donorData.put("13]Street", donor_street);
        donorData.put("14]Landmark", donor_landmark);

        // Emergency Contact
        Map<String, String> emergency = new HashMap<>();
        emergency.put("01]Name", donor_emergency_name);
        emergency.put("02]Phone", donor_emergency_phno);
        emergency.put("03]Relation", donor_emergency_relation);
        donorData.put("15]Emergency_contact", emergency);

        // Medical Info
        donorData.put("16]Organs_to_donate", organsToDonate);
        donorData.put("17]Medical_conditions", donor_medical_condition);
        donorData.put("18]Previous_surgeries", donor_previous_surgery);

        donorData.put("19]registration_timestamp", com.google.firebase.Timestamp.now());

        // IMPORTANT: Add the flag that the login page looks for
        donorData.put("20]isProfileComplete", true);

        // 1. Save to "donors" collection for public/admin records
        db.collection("donors").document(donor_adhaar_no).set(donorData);

        // 2. Update the "users" collection document (the one used for login)
        // We use update() so we don't overwrite the email/password/type already there
        db.collection("users").document(uid)
                .update("isProfileComplete", true)
                .addOnSuccessListener(aVoid -> {
                    showRegistrationSuccess();
                    btnRegister.setEnabled(true);
                    btnRegister.setAlpha(1f);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(true);
                });
    }

    private void showRegistrationSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage("Thank you " + donor_full_name + " for becoming an organ donor!")
                .setPositiveButton("OK", (dialog, which) -> {
                    sendRegisterationSMS();

                    Intent intent = new Intent(DonorRegistration.this, Donor_main_page.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void sendRegisterationSMS() {

        // 1. Check if we have permission RIGHT NOW
        if (!checkPermission(android.Manifest.permission.SEND_SMS)) {
            // If no permission, request it and STOP here.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 1);
            Toast.makeText(this, "Please allow SMS permission and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. If we reach here, we definitely have permission
        String msg = "Dear " + donor_full_name + ",\nYou have been successfully registered as an Organ Donor!";

        try {
            SmsManager smsManager = this.getSystemService(SmsManager.class);

            if (smsManager != null) {
                // Send to Donor
                smsManager.sendTextMessage(donor_phone_no, null, msg, null, null);
                // Send to Emergency Contact
                smsManager.sendTextMessage(donor_emergency_phno, null, msg, null, null);

                Toast.makeText(this, "Confirmation SMS sent!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // ============================================
    //  NAVIGATION METHODS
    private void navigateToLogin() {
        // Navigate to login screen
        Intent intent = new Intent(this, Donor_login.class);
        startActivity(intent);
        finish(); // Close current activity
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private boolean validateTermsAndConditions() {
        if (!checkboxTermsConditions.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions to proceed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void openTermsAndConditions() {
        Intent intent = new Intent(this, DonorTermsConditionsActivity.class);
        startActivityForResult(intent, 1001); // Request code for terms and conditions
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) { // Terms and conditions request code
            if (resultCode == RESULT_OK) {
                // User accepted terms and conditions
                checkboxTermsConditions.setChecked(true);
                Toast.makeText(this, "Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User declined terms and conditions
                checkboxTermsConditions.setChecked(false);
                Toast.makeText(this, "Terms and Conditions declined", Toast.LENGTH_SHORT).show();
            }
        }
    }
}