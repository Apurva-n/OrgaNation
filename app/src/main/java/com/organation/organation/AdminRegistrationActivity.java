package com.organation.organation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class AdminRegistrationActivity extends AppCompatActivity {

    // UI Components - Personal Information
    private EditText etFullName, etContactNumber, etEmail;
    private Spinner spinnerGender;
    
    // UI Components - Admin Information
    private EditText etAdminId, etAdminRegion;
    private Spinner spinnerAdminLevel;
    
    // Terms and conditions
    private CheckBox checkboxAdminTermsConditions;
    private TextView tvAdminTermsConditions;
    
    // Verification Information
    private EditText etGovIdNumber, etOfficialEmail;
    private Button btnUploadIdProof;

    // UI Components - Account Details
    private EditText etUsername, etPassword, etConfirmPassword, etSecurityAnswer;
    private Spinner spinnerSecurityQuestion;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    
    // UI Components - Action Buttons
    private Button btnRegister, btnReset, btnBackToLogin;
    
    // File Upload
    private Uri idProofUri;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    
    // Form Data
    private String adminId;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;


    //private data varibales
    String fullname;
    String contactNo;
    String email;
    String admin_Id;
    String adminLevel;
    String adminRegion;
    String GovIdNumber;
    String userName;
    String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_registration);

        btnRegister=findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(AdminRegistrationActivity.this,Donor_login.class);
                startActivity(i);

            }
        });
        
        initializeViews();
        setupSpinners();
        setupFilePicker();
        setupFormValidation();
        setupClickListeners();
        generateAdminId();
    }
    
    private void initializeViews() {
        // Personal Information
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);

        spinnerGender = findViewById(R.id.spinnerGender);
        etContactNumber = findViewById(R.id.etContactNumber);
        etEmail = findViewById(R.id.etEmail);
        
        // Admin Information
        etAdminId = findViewById(R.id.etAdminId);
        spinnerAdminLevel = findViewById(R.id.spinnerAdminLevel);
        etAdminRegion = findViewById(R.id.etAdminRegion);
        
        // Verification Information
        etGovIdNumber = findViewById(R.id.etGovIdNumber);
        btnUploadIdProof = findViewById(R.id.btnUploadIdProof);

        
        // Account Details

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        
        // Action Buttons
        btnRegister = findViewById(R.id.btnRegister);
        btnReset = findViewById(R.id.btnReset);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        
        // Terms and conditions
        checkboxAdminTermsConditions = findViewById(R.id.checkboxAdminTermsConditions);
        tvAdminTermsConditions = findViewById(R.id.tvAdminTermsConditions);
    }
    
    private void setupSpinners() {
        // Gender Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
            this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        
        // Admin Level Spinner
        ArrayAdapter<CharSequence> adminLevelAdapter = ArrayAdapter.createFromResource(
            this, R.array.admin_levels, android.R.layout.simple_spinner_item);
        adminLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdminLevel.setAdapter(adminLevelAdapter);
        
        // Security Question Spinner
        ArrayAdapter<CharSequence> securityQuestionAdapter = ArrayAdapter.createFromResource(
            this, R.array.security_questions, android.R.layout.simple_spinner_item);
        securityQuestionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecurityQuestion.setAdapter(securityQuestionAdapter);
    }
    
    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        handleIdProofSelection(uri);
                    }
                }
            });
    }
    
    private void setupFormValidation() {
        // Add text watchers for real-time validation
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFirstName();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etContactNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
                if (!TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
                    validatePasswordMatch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnUploadIdProof.setOnClickListener(v -> openFilePicker());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        btnRegister.setOnClickListener(v -> validateAndSubmit());
        btnReset.setOnClickListener(v -> resetForm());
        btnBackToLogin.setOnClickListener(v -> navigateToLogin());
        
        // Admin terms and conditions click listener
        tvAdminTermsConditions.setOnClickListener(v -> {
            openAdminTermsAndConditions();
        });
    }
    
    private void generateAdminId() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        adminId = "ADM-" + timestamp + "-" + random;
        
        // Set the EditText and ensure it's displayed
        etAdminId.setText(adminId);
        etAdminId.setSelection(etAdminId.getText().length()); // Move cursor to end
        
        android.util.Log.d("AdminRegistration", "Generated Admin ID: " + adminId);
    }
    
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(129); // textPassword
            btnTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(144); // text
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(129); // textPassword
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye);
        } else {
            etConfirmPassword.setInputType(144); // text
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select ID Proof"));
    }
    
    private void handleIdProofSelection(Uri uri) {
        idProofUri = uri;
        btnUploadIdProof.setText("ID Proof Uploaded");
        Toast.makeText(this, "ID proof selected successfully", Toast.LENGTH_SHORT).show();
    }
    
    private boolean validateFirstName() {
        String firstName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            etFullName.setError("First name is required");
            return false;
        }
        if (firstName.length() < 2) {
            etFullName.setError("First name must be at least 2 characters");
            return false;
        }
        if (!firstName.matches("^[a-zA-Z]+(?:\\s[a-zA-Z]+)+$")) {
            etFullName.setError("First name can only contain letters");
            return false;
        }
        return true;
    }
    

    
    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }
    
    private boolean validatePhoneNumber() {
        String phone = etContactNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            etContactNumber.setError("Contact number is required");
            return false;
        }
        if (phone.length() < 10) {
            etContactNumber.setError("Please enter a valid phone number");
            return false;
        }
        if (!phone.matches("^[0-9]+$")) {
            etContactNumber.setError("Phone number can only contain digits");
            return false;
        }
        return true;
    }
    
    private boolean validatePassword() {
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return false;
        }
        if (!Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$")
                .matcher(password).matches()) {
            etPassword.setError("Password must contain uppercase, lowercase, number and special character");
            return false;
        }
        return true;
    }
    
    private boolean validatePasswordMatch() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        
        return true;
    }
    
    private boolean validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return false;
        }
        if (username.length() < 4) {
            etUsername.setError("Username must be at least 4 characters");
            return false;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            etUsername.setError("Username can only contain letters, numbers and underscore");
            return false;
        }
        return true;
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate personal information
        if (!validateFirstName()) isValid = false;
        if (!validateEmail()) isValid = false;
        if (!validatePhoneNumber()) isValid = false;
        
        // Validate gender selection
        if (spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate admin information
        if (spinnerAdminLevel.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select admin level", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (TextUtils.isEmpty(etAdminRegion.getText().toString().trim())) {
            etAdminRegion.setError("Admin region is required");
            isValid = false;
        }
        
        // Validate verification information
        if (TextUtils.isEmpty(etGovIdNumber.getText().toString().trim())) {
            etGovIdNumber.setError("Government ID number is required");
            isValid = false;
        }

        
        if (idProofUri == null) {
            Toast.makeText(this, "Please upload ID proof", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate account details
        if (!validateUsername()) isValid = false;
        if (!validatePassword()) isValid = false;
        if (!validatePasswordMatch()) isValid = false;
        
        if (spinnerSecurityQuestion.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a security question", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (TextUtils.isEmpty(etSecurityAnswer.getText().toString().trim())) {
            etSecurityAnswer.setError("Security answer is required");
            isValid = false;
        }
        
        // Validate terms and conditions
        if (!checkboxAdminTermsConditions.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions for administrators", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private void validateAndSubmit() {
        if (!validateForm()) {
            return;
        }
        
        // IMPORTANT: Populate variables from UI fields before showing dialog
        populateFieldsFromVariables();
        
        android.util.Log.d("AdminRegistration", "Form validated, admin_Id is: " + admin_Id);
        
        new AlertDialog.Builder(this)
            .setTitle("Confirm Registration")
            .setMessage("Are you sure you want to register this admin account? The admin ID is " + adminId)
            .setPositiveButton("Confirm", (dialog, which) -> {
                android.util.Log.d("AdminRegistration", "User confirmed, calling saveDataToFirestore with admin_Id: " + admin_Id);
                saveDataToFirestore();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void populateFieldsFromVariables() {
        contactNo = etContactNumber.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        admin_Id = etAdminId.getText().toString().trim();
        adminLevel = spinnerAdminLevel.getSelectedItem().toString();
        adminRegion = etAdminRegion.getText().toString().trim();
        GovIdNumber = etGovIdNumber.getText().toString().trim();
        userName = etUsername.getText().toString().trim(); // Fixed
        pwd = etPassword.getText().toString(); // Fixed: Get from Password field, not Username
    }


    private void saveDataToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        android.util.Log.d("AdminRegistration", "Saving admin with ID: " + admin_Id + " and UID: " + uid);

        Map<String, Object> data = new HashMap<>();
        data.put("01] Admin Name", fullname);
        data.put("02] Contact No", contactNo);
        data.put("03] Email", email);
        data.put("04] Gender", spinnerGender.getSelectedItem().toString());
        data.put("05] Admin_Id", admin_Id);
        data.put("06] Admin_Level", adminLevel);
        data.put("07] Admin_Region", adminRegion);
        data.put("08] GovIdNumber", GovIdNumber);
        data.put("10] Username", userName);
        data.put("11] Password", pwd);
        data.put("12] Security_Question", spinnerSecurityQuestion.getSelectedItem().toString());
        data.put("13] Security_Answer", etSecurityAnswer.getText().toString().trim());
        data.put("14] registration_timestamp", com.google.firebase.Timestamp.now());
        data.put("15] isProfileComplete", true);

        db.collection("Admin").document(admin_Id).set(data)
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(uid)
                            .update("isProfileComplete", true, "userType", "Admin")
                            .addOnSuccessListener(aVoid2 -> showRegistrationSuccess())
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving admin data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showRegistrationSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage("Thank you " + fullname + " for becoming an Admin of "+adminRegion+"!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate to admin dashboard immediately
                    Intent intent = new Intent(AdminRegistrationActivity.this, admin_main_page.class);
                    startActivity(intent);
                    finish();
                    
                    // Send SMS in background (non-blocking)
                    sendRegisterationSMS();
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
        String msg = "Dear " + fullname + ",\nYou have been successfully registered as an Admin!";

        try {
            SmsManager smsManager = this.getSystemService(SmsManager.class);

            if (smsManager != null) {

                smsManager.sendTextMessage(contactNo, null, msg, null, null);

                Toast.makeText(this, "Confirmation SMS sent!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }


    private void resetForm() {
        new AlertDialog.Builder(this)
            .setTitle("Reset Form")
            .setMessage("Are you sure you want to reset all form data?")
            .setPositiveButton("Reset", (dialog, which) -> {
                // Clear all form fields
                etFullName.setText("");

                spinnerGender.setSelection(0);
                etContactNumber.setText("");
                etEmail.setText("");
                
                spinnerAdminLevel.setSelection(0);
                etAdminRegion.setText("");
                
                etGovIdNumber.setText("");

                idProofUri = null;
                btnUploadIdProof.setText("Upload ID Proof");
                
                etUsername.setText("");
                etPassword.setText("");
                etConfirmPassword.setText("");
                spinnerSecurityQuestion.setSelection(0);
                etSecurityAnswer.setText("");
                
                // Reset password visibility
                if (isPasswordVisible) {
                    togglePasswordVisibility();
                }
                if (isConfirmPasswordVisible) {
                    toggleConfirmPasswordVisibility();
                }
                
                // Generate new admin ID
                generateAdminId();
                
                Toast.makeText(this, "Form has been reset", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void navigateToLogin() {
        // Navigate back to login screen
        Intent intent = new Intent(this, Donor_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openAdminTermsAndConditions() {
        Intent intent = new Intent(this, AdminTermsConditionsActivity.class);
        startActivityForResult(intent, 1006); // Request code for admin terms and conditions
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1006) { // Admin terms and conditions request code
            if (resultCode == RESULT_OK) {
                // User accepted terms and conditions
                checkboxAdminTermsConditions.setChecked(true);
                Toast.makeText(this, "Admin Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User declined terms and conditions
                checkboxAdminTermsConditions.setChecked(false);
                Toast.makeText(this, "Admin Terms and Conditions declined", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
