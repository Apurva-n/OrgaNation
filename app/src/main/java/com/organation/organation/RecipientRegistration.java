package com.organation.organation;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class RecipientRegistration extends AppCompatActivity {
    private androidx.activity.result.ActivityResultLauncher<String> mGetContent;
    private ImageView ivProfilePhoto;
    private android.net.Uri profileImageUri = null;

    // Personal Information Fields
    private EditText RecipientName, Age,adhaar_no,Height,Weight,HospitalState,HospitalCity,HospitalStreet,HospitaLandmark;
    private EditText HomeState,HomeCity,HomeStreet,HomeLandmark;
    private Spinner spinnerGender, spinnerRecipientBloodGroup,spinnerUrgency;

    // Contact Information Fields
    private EditText RecipientPhone, RecipientEmail,RecipientDob;

    // Donation Details Fields
    private MultiAutoCompleteTextView OrganNeeded;
    private EditText HospitalName,DoctorName;

    // Terms and conditions
    private CheckBox checkboxRecipientTermsConditions;
    private TextView tvRecipientTermsConditions;

    // UI Components
    private LinearLayout btnRegisterRecipient, llRecipientDobPicker;
    private TextView tvRecipientLoginLoginNow;

    // ============================================
    // DATA STORAGE VARIABLES
    // ============================================
    private String rec_name,rec_adhaar,rec_age,rec_dob,rec_height,rec_weight,rec_gender,rec_bloodgroup;
    private String rec_phone,rec_email,rec_hosp_name,rec_doctor_name;
    private String organ_needed,organ_urgency,rec_state,rec_city,rec_street,rec_landmark,hosp_state,hosp_city,hosp_street,hosp_landmark;


    // ANDROID LIFECYCLE METHODS //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipient_registration);
        initializeViews();

        ivProfilePhoto = findViewById(R.id.RecipientPhoto);
        FrameLayout flProfilePhoto = findViewById(R.id.flRecipientPhoto);

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

        setupOrganSelection();
        setupListenersAndValidation();
    }

    private void initializeViews() {
        // Personal Information Fields
        adhaar_no=findViewById(R.id.adhaar_no);
        RecipientName = findViewById(R.id.RecipientName);
        Age = findViewById(R.id.Age);
        RecipientDob = findViewById(R.id.RecipientDob);
        Height=findViewById(R.id.Height);
        Weight=findViewById(R.id.Weight);

        //spinners
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerRecipientBloodGroup = findViewById(R.id.spinnerRecipientBloodGroup);
        spinnerUrgency=findViewById(R.id.spinnerUrgency);

        // Contact Information Fields
        RecipientPhone = findViewById(R.id.RecipientPhone);
        RecipientEmail = findViewById(R.id.RecipientEmail);

        // Donation request Fields
        OrganNeeded = findViewById(R.id.OrganNeeded);

        //hosp address detials
        HospitalState=findViewById(R.id.HospitalState);
        HospitalCity=findViewById(R.id.HospitalCity);
        HospitalStreet=findViewById(R.id.HospitalStreet);
        HospitaLandmark=findViewById(R.id.HospitaLandmark);

        HospitalName=findViewById(R.id.HospitalName);
        DoctorName=findViewById(R.id.DoctorName);

        //Home Address details
        HomeState=findViewById(R.id.HomeState);
        HomeCity=findViewById(R.id.HomeCity);
        HomeStreet=findViewById(R.id.HomeStreet);
        HomeLandmark=findViewById(R.id.HomeLandmark);

        // UI Components
        btnRegisterRecipient = findViewById(R.id.btnRegisterRecipient);
        llRecipientDobPicker = findViewById(R.id.llRecipientDobPicker);
        tvRecipientLoginLoginNow = findViewById(R.id.tvRecipientLoginLoginNow);
        
        // Terms and conditions
        checkboxRecipientTermsConditions = findViewById(R.id.checkboxRecipientTermsConditions);
        tvRecipientTermsConditions = findViewById(R.id.tvRecipientTermsConditions);
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
        OrganNeeded.setAdapter(organAdapter);

        // Show dropdown after typing 1 character
        OrganNeeded.setThreshold(1);

        // Allow multiple organ selection separated by comma
        OrganNeeded.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }


    private void setupListenersAndValidation() {
        // Date picker click listener
        llRecipientDobPicker.setOnClickListener(v -> openDatePicker());

        // Register button click listener
        btnRegisterRecipient.setOnClickListener(v -> validateAndSubmitForm());

        // Recipient terms and conditions click listener
        tvRecipientTermsConditions.setOnClickListener(v -> {
            openRecipientTermsAndConditions();
        });

        // Age field validation - clear error when manually changed
        Age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user manually changes age
                Age.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Login link click listener
        tvRecipientLoginLoginNow.setOnClickListener(v -> navigateToLogin());

        // Real-time validation for text fields
        setupRealTimeValidation();
    }


    private void setupRealTimeValidation() {

        // Adhaar validation
        adhaar_no.addTextChangedListener(new TextWatcher() {
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
        RecipientName.addTextChangedListener(new TextWatcher() {
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
        RecipientEmail.addTextChangedListener(new TextWatcher() {
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
        RecipientPhone.addTextChangedListener(new TextWatcher() {
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date and set it to the EditText
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    RecipientDob.setText(formattedDate);
                    
                    // Calculate and set age automatically
                    calculateAndSetAge(selectedDay, selectedMonth, selectedYear);
                },
                year, month, day
        );

        // Set maximum date to today (can't select future dates)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void calculateAndSetAge(int day, int month, int year) {
        // Get current date
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        
        // Create calendar for birth date
        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.set(Calendar.YEAR, year);
        birthCalendar.set(Calendar.MONTH, month);
        birthCalendar.set(Calendar.DAY_OF_MONTH, day);
        
        // Calculate age
        int calculatedAge = currentYear - year;
        
        // Adjust if birthday hasn't occurred yet this year
        if (currentMonth < month || (currentMonth == month && currentDay < day)) {
            calculatedAge--;
        }
        
        // Set the age in the Age EditText
        Age.setText(String.valueOf(calculatedAge));
        
        // Clear any previous error
        Age.setError(null);
    }

    // ============================================
    //  VALIDATION METHODS
    // ============================================

    private boolean validateAdhaar() {
        rec_adhaar = adhaar_no.getText().toString().trim();

        if (TextUtils.isEmpty(rec_adhaar)) {
            adhaar_no.setError("Aadhaar number is required");
            return false;
        }

        if (!rec_adhaar.matches("^\\d{12}$")) {
            adhaar_no.setError("Aadhaar number must be exactly 12 digits");
            return false;
        }

        adhaar_no.setError(null);
        return true;
    }

    private boolean validateFirstName() {
        rec_name = RecipientName.getText().toString().trim();

        if (TextUtils.isEmpty(rec_name)) {
            RecipientName.setError("Full name is required");
            return false;
        }

        if (!rec_name.matches("^[a-zA-Z\\s]+$")) {
            RecipientName.setError("Name should contain only letters and spaces");
            return false;
        }

        RecipientName.setError(null);
        return true;
    }

    private boolean validateEmail() {
        rec_email = RecipientEmail.getText().toString().trim();

        if (TextUtils.isEmpty(rec_email)) {
            RecipientEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(rec_email).matches()) {
            RecipientEmail.setError("Please enter a valid email address");
            return false;
        }

        RecipientEmail.setError(null);
        return true;
    }

    private boolean validatePhone() {
        rec_phone = RecipientPhone.getText().toString().trim();

        if (TextUtils.isEmpty(rec_phone)) {
            RecipientPhone.setError("Phone number is required");
            return false;
        }

        if (!rec_phone.matches("^[6-9]\\d{9}$")) {
            RecipientPhone.setError("Please enter a valid 10-digit phone number");
            return false;
        }

        RecipientPhone.setError(null);
        return true;
    }

    private boolean validateAge() {
        rec_age = Age.getText().toString().trim();

        if (TextUtils.isEmpty(rec_age)) {
            Age.setError("Age is required");
            return false;
        }
        if (!rec_age.matches("^\\d{1,3}$")) {
            Age.setError("Please enter a valid age");
            return false;
        }

        int ageValue=Integer.parseInt(rec_age);
        if(ageValue<=0)
        {
            Age.setError("Invalid age entered");
            return false;
        }

        Age.setError(null);
        return true;
    }

    private boolean validateGender() {
        rec_gender = spinnerGender.getSelectedItem().toString();

        if (rec_gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private boolean validateHeight() {
        rec_height=Height.getText().toString().trim();


        if (TextUtils.isEmpty(rec_height)) {
            Height.setError("Height is required");
            return false;
        }

        int HeightValue = Integer.parseInt(rec_height);

        if (HeightValue <=20 || HeightValue > 210) {
            Toast.makeText(this, "Invalid height entered", Toast.LENGTH_SHORT).show();
            return false;
        }

        Height.setError(null);
        return true;
    }
    private boolean validateWeight() {
        rec_weight=Weight.getText().toString().trim();

        if (TextUtils.isEmpty(rec_weight)) {
            Weight.setError("Age is required");
            return false;
        }

        int WeightValue = Integer.parseInt(rec_weight);

        if (WeightValue <=0 || WeightValue > 210) {
            Toast.makeText(this, "Invalid weight entered\"", Toast.LENGTH_SHORT).show();
            return false;
        }

        Weight.setError(null);
        return true;
    }

    private boolean validateBloodGroup() {
        rec_bloodgroup = spinnerRecipientBloodGroup.getSelectedItem().toString();

        if (rec_bloodgroup.equals("Select Blood Group")) {
            Toast.makeText(this, "Please select your blood group", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateOrgans() {
        organ_needed = OrganNeeded.getText().toString().trim();

        if (TextUtils.isEmpty(organ_needed)) {
            OrganNeeded.setError("Please select at least one organ for request");
            return false;
        }

        OrganNeeded.setError(null);
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

    private boolean validateDoctorName() {
        rec_doctor_name = DoctorName.getText().toString().trim();

        if (TextUtils.isEmpty(rec_doctor_name)) {
            DoctorName.setError("Doctor name is required");
            return false;
        }

        if (!rec_doctor_name.matches("^[a-zA-Z\\s\\.]+$")) {
            DoctorName.setError("Doctor name should contain only letters, spaces, and dots");
            return false;
        }

        DoctorName.setError(null);
        return true;
    }

    private boolean validateHospitalName() {
        rec_hosp_name = HospitalName.getText().toString().trim();

        if (TextUtils.isEmpty(rec_hosp_name)) {
            HospitalName.setError("Hospital name is required");
            return false;
        }

        HospitalName.setError(null);
        return true;
    }

    private void validateAndSubmitForm() {
        // Store date of birth
        rec_dob = RecipientDob.getText().toString().trim().trim();

        rec_state =HomeState.getText().toString().trim();
        rec_city=HomeCity.getText().toString().trim();
        rec_street=HomeStreet.getText().toString().trim();
        rec_landmark=HomeLandmark.getText().toString().trim();

        hosp_state =HospitalState.getText().toString().trim();
        hosp_city=HospitalCity.getText().toString().trim();
        hosp_street=HospitalStreet.getText().toString().trim();
        hosp_landmark=HospitaLandmark.getText().toString().trim();

        organ_urgency=spinnerUrgency.getSelectedItem().toString().trim();



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
        if (!validateDoctorName()) isValid = false;
        if (!validateHospitalName()) isValid = false;
        if (!validateRecipientTermsConditions()) isValid = false;



        // Validate date of birth
        if (TextUtils.isEmpty(rec_dob)) {
            RecipientDob.setError("Please enter your date of birth");
            isValid = false;
        }
        if (TextUtils.isEmpty(rec_state)) {
            HomeState.setError("Please select your state");
            isValid = false;
        }

        if (TextUtils.isEmpty(rec_city)) {
            HomeCity.setError("Please select your city");
            isValid = false;
        }

        if (TextUtils.isEmpty(rec_street)) {
            HomeStreet.setError("Please select your street");
            isValid = false;
        }

        if (TextUtils.isEmpty(rec_landmark)) {
            HomeLandmark.setError("Please select your nearest landmark");
            isValid = false;
        }

        if (TextUtils.isEmpty(hosp_state)) {
            HospitalState.setError("Please select your state");
            isValid = false;
        }

        if (TextUtils.isEmpty(hosp_city)) {
            HospitalCity.setError("Please select your city");
            isValid = false;
        }

        if (TextUtils.isEmpty(hosp_street)) {
            HospitalStreet.setError("Please select your street");
            isValid = false;
        }

        if (TextUtils.isEmpty(hosp_landmark)) {
            HospitaLandmark.setError("Please select your nearest landmark");
            isValid = false;
        }

        if (organ_urgency.equals("Select Urgency Level") || organ_urgency.isEmpty()) {
            Toast.makeText(this, "Please select an urgency level", Toast.LENGTH_SHORT).show();
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
                .setMessage("Are you sure you want to register as an organ recepeint?\n")
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
        donorData.put("01]Full_name", rec_name);
        donorData.put("02]Aadhaar_no", rec_adhaar);
        donorData.put("03]Dob", rec_dob);
        donorData.put("04]Age", rec_age);
        donorData.put("05]Gender", rec_gender);
        donorData.put("06]Blood_group", rec_bloodgroup);
        donorData.put("07]Height", rec_height);
        donorData.put("08]Weight", rec_weight);

        // Contact & Address
        donorData.put("09]Phone", rec_phone);
        donorData.put("10]Email", rec_email);
        donorData.put("11]State", rec_state);
        donorData.put("12]City", rec_city);
        donorData.put("13]Street", rec_street);
        donorData.put("14]Landmark", rec_landmark);

        // Emergency Contact
        Map<String, String> emergency = new HashMap<>();
        emergency.put("01]Hospital Name", rec_hosp_name);
        emergency.put("02]Doctor Name", rec_doctor_name);
        emergency.put("03]Hospital state", hosp_state);
        emergency.put("04]Hospital city", hosp_city);
        emergency.put("05]Hospital street", hosp_street);
        emergency.put("06]Hospital landmark", hosp_landmark);
        donorData.put("15]Hospital_details", emergency);

        // Medical Info
        donorData.put("16]Organs_to_donate", organ_needed);
        donorData.put("17]Urgency", organ_urgency != null ? organ_urgency : "Medium");

        donorData.put("17]registration_timestamp", com.google.firebase.Timestamp.now());

        // IMPORTANT: Add the flag that the login page looks for
        donorData.put("18]isProfileComplete", true);

        // 1. Save to "Recepients" collection for public/admin records
        Log.d("RECIPIENT_REG", "Attempting to save recipient to Recepients collection...");
        Log.d("RECIPIENT_REG", "Recipient Aadhaar: " + rec_adhaar);
        Log.d("RECIPIENT_REG", "Recipient Name: " + rec_name);
        
        db.collection("Recepients").document(rec_adhaar).set(donorData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RECIPIENT_REG", "✅ SUCCESS: Successfully saved to Recepients collection");
                    Log.d("RECIPIENT_REG", "✅ Collection should now exist in Firebase");
                    // Create FIRST ORGAN REQUEST automatically
                    createFirstOrganRequest(db, uid, rec_adhaar);
                    
                    // Update the "users" collection document (the one used for login)
                    // We use update() so we don't overwrite the email/password/type already there
                    db.collection("users").document(uid)
                            .update("isProfileComplete", true)
                            .addOnSuccessListener(successVoid -> {
                                Log.d("RECIPIENT_REG", "✅ SUCCESS: User profile updated");
                                showRegistrationSuccess();
                                btnRegisterRecipient.setEnabled(true);
                                btnRegisterRecipient.setAlpha(1f);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("RECIPIENT_REG", "❌ ERROR: Failed to update user profile: " + e.getMessage());
                                Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnRegisterRecipient.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("RECIPIENT_REG", "❌ ERROR: Failed to save to Recepients collection: " + e.getMessage());
                    Log.e("RECIPIENT_REG", "❌ ERROR DETAILS: " + e.toString());
                    Toast.makeText(this, "Error saving recipient data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnRegisterRecipient.setEnabled(true);
                    btnRegisterRecipient.setAlpha(1f);
                });
    }

    private void createFirstOrganRequest(FirebaseFirestore db, String uid, String aadhaar) {
        // Generate unique request ID
        String requestId = "REQ_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Get current date
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        
        // Parse organ needed - take first organ if multiple
        String primaryOrgan = organ_needed;
        if (organ_needed.contains(",")) {
            primaryOrgan = organ_needed.split(",")[0].trim();
        }
        
        // Create organ request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestId", requestId);
        requestData.put("recipientUid", uid);  // Firebase Auth UID
        requestData.put("recipientAadhaar", aadhaar);  // Primary Key
        requestData.put("recipientName", rec_name);
        requestData.put("organType", primaryOrgan);
        requestData.put("bloodType", rec_bloodgroup);
        requestData.put("urgency", organ_urgency != null ? organ_urgency : "Medium");
        requestData.put("hospitalName", rec_hosp_name);
        requestData.put("hospitalCity", hosp_city);
        requestData.put("hospitalLocation", hosp_street + ", " + hosp_city);
        requestData.put("treatingDoctor", rec_doctor_name);
        requestData.put("medicalDetails", "Initial request during registration");
        requestData.put("additionalNotes", "First organ request created during registration");
        requestData.put("requestDate", currentDate);
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
                    // Initialize recipient request count
                    initializeRecipientRequestCount(db, uid);
                })
                .addOnFailureListener(e -> {
                    // Log error but don't fail registration
                    System.err.println("Error creating first organ request: " + e.getMessage());
                });
    }

    private void initializeRecipientRequestCount(FirebaseFirestore db, String uid) {
        Map<String, Object> countData = new HashMap<>();
        countData.put("totalRequests", 1L);
        countData.put("pendingRequests", 1L);
        countData.put("approvedRequests", 0L);
        countData.put("processedRequests", 0L);
        countData.put("completedRequests", 0L);
        countData.put("declinedRequests", 0L);
        countData.put("lastUpdated", new Date());

        db.collection("recipient_requests").document(uid)
                .set(countData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully initialized request count
                })
                .addOnFailureListener(e -> {
                    // Log error but don't fail registration
                    System.err.println("Error initializing request count: " + e.getMessage());
                });
    }

    private void showRegistrationSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage("Thank you " + rec_name + " for registering as a recipient!\n\nYour first organ request has been created automatically.")
                .setPositiveButton("OK", (dialog, which) -> {
                    sendRegisterationSMS();

                    Intent intent = new Intent(RecipientRegistration.this, Recepeint_main_page.class);
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
        String msg = "Dear " + rec_name + ",\nYou have been successfully registered as an Organ Recepient!";

        try {
            SmsManager smsManager = this.getSystemService(SmsManager.class);

            if (smsManager != null) {
                // Send to Donor
                smsManager.sendTextMessage(rec_phone, null, msg, null, null);
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

    private boolean validateRecipientTermsConditions() {
        if (!checkboxRecipientTermsConditions.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions for organ recipients", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void openRecipientTermsAndConditions() {
        Intent intent = new Intent(this, RecipientTermsConditionsActivity.class);
        startActivityForResult(intent, 1005); // Request code for recipient terms and conditions
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1005) { // Recipient terms and conditions request code
            if (resultCode == RESULT_OK) {
                // User accepted terms and conditions
                checkboxRecipientTermsConditions.setChecked(true);
                Toast.makeText(this, "Recipient Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User declined terms and conditions
                checkboxRecipientTermsConditions.setChecked(false);
                Toast.makeText(this, "Recipient Terms and Conditions declined", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
