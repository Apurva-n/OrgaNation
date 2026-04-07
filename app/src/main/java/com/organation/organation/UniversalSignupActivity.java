package com.organation.organation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UniversalSignupActivity extends AppCompatActivity {

    // UI Components for user type selection and input fields
    private CardView cardDonor, cardRecipient, cardHospital, cardAdmin, selectedTypeCard;
    private TextView selectedTypeText, tvLoginLink;
    private ImageView selectedTypeIcon;
    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private ImageButton btnTogglePass, btnToggleConfirmPass;
    private CheckBox checkboxTerms;
    private TextView tvTermsConditions;
    private Button btnSignUp;

    // Variables to track state
    private String selectedUserType = "";
    private boolean isPassVisible = false, isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge layout support
        setContentView(R.layout.activity_universal_signup);

        initializeViews();       // Link Java variables to XML IDs
        setupUserTypeSelection(); // Handle the logic for clicking Donor/Recipient cards
        setupClickListeners();   // Set up buttons for signup and password toggles
    }

    private void initializeViews() {
        // Mapping UI components to their respective XML IDs
        cardDonor = findViewById(R.id.cardDonor);
        cardRecipient = findViewById(R.id.cardRecipient);
        cardHospital = findViewById(R.id.cardHospital);
        cardAdmin = findViewById(R.id.cardAdmin);
        selectedTypeCard = findViewById(R.id.selectedTypeCard);
        selectedTypeText = findViewById(R.id.selectedTypeText);
        selectedTypeIcon = findViewById(R.id.selectedTypeIcon);

        etFullName = findViewById(R.id.UserFullName);
        etEmail = findViewById(R.id.UserEmail);
        etPhone = findViewById(R.id.UserPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPassword1);

        btnTogglePass = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPass = findViewById(R.id.btnTogglePassword1);
        checkboxTerms = findViewById(R.id.checkboxTerms);
        tvTermsConditions = findViewById(R.id.tvTermsConditions);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupUserTypeSelection() {
        // Universal click listener for all 4 cards
        View.OnClickListener listener = v -> {
            resetUserTypeCards(); // Turn all cards back to white
            ((CardView) v).setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_light));

            int id = v.getId();
            if (id == R.id.cardDonor) updateTypeDisplay("Donor", R.string.user_donor, R.drawable.ic_heart_logo);
            else if (id == R.id.cardRecipient) updateTypeDisplay("Recipient", R.string.user_recipient, R.drawable.ic_heart_plus);
            else if (id == R.id.cardHospital) updateTypeDisplay("Hospital", R.string.user_hospital, R.drawable.ic_hospital);
            else if (id == R.id.cardAdmin) updateTypeDisplay("Admin", R.string.user_admin, R.drawable.ic_admin);

            // Simple "Pop" animation when clicked
            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).start()).start();
        };

        cardDonor.setOnClickListener(listener);
        cardRecipient.setOnClickListener(listener);
        cardHospital.setOnClickListener(listener);
        cardAdmin.setOnClickListener(listener);
    }

    private void resetUserTypeCards() {
        int white = ContextCompat.getColor(this, android.R.color.white);
        cardDonor.setCardBackgroundColor(white);
        cardRecipient.setCardBackgroundColor(white);
        cardHospital.setCardBackgroundColor(white);
        cardAdmin.setCardBackgroundColor(white);
    }

    private void updateTypeDisplay(String type, int textRes, int iconRes) {
        selectedUserType = type;
        selectedTypeCard.setVisibility(View.VISIBLE);
        selectedTypeText.setText(textRes);
        selectedTypeIcon.setImageResource(iconRes);
    }

    private void setupClickListeners() {
        // Toggle password visibility (Eye icon)
        btnTogglePass.setOnClickListener(v -> toggleVisibility(etPassword, btnTogglePass, true));
        btnToggleConfirmPass.setOnClickListener(v -> toggleVisibility(etConfirmPassword, btnToggleConfirmPass, false));

        // Final signup trigger
        btnSignUp.setOnClickListener(v -> validateAndSubmit());

        // Link to switch to login screen
        tvLoginLink.setOnClickListener(v -> navigateToLogin());

        // Terms and conditions click listener
        tvTermsConditions.setOnClickListener(v -> {
            openAppTermsAndConditions();
        });
    }

    private void toggleVisibility(EditText et, ImageButton btn, boolean isPassField) {
        boolean visible = isPassField ? isPassVisible : isConfirmVisible;
        if (visible) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btn.setImageResource(R.drawable.ic_eye);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btn.setImageResource(R.drawable.ic_eye_off);
        }
        if (isPassField) isPassVisible = !isPassVisible; else isConfirmVisible = !isConfirmVisible;
        et.setSelection(et.getText().length());
    }

    private boolean validateForm() {
        // Collect strings from inputs
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        // 1. Check if user selected a role (Donor, etc)
        if (TextUtils.isEmpty(selectedUserType)) {
            Toast.makeText(this, "Please select your user type first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        // 2. Name validation (Minimum 2 words for Full Name)
        if (!name.matches("^[a-zA-Z]+(?:\\s[a-zA-Z]+)+$")) {
            etFullName.setError("Enter your first and last name");
            return false;
        }
        // 3. Simple email format check
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return false;
        }
        // 4. Indian Phone Number check (Starts with 6-9, 10 digits)
        if (!phone.matches("^[6-9]\\d{9}$")) {
            etPhone.setError("Invalid 10-digit number");
            return false;
        }
        // 5. Password Strength check
        Pattern passPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$");
        if (!passPattern.matcher(pass).matches()) {
            etPassword.setError("Use 8+ chars, Upper, Lower, Number & Symbol");
            return false;
        }
        // 6. Match check
        if (!pass.equals(confirm)) {
            etConfirmPassword.setError("Passwords mismatch");
            return false;
        }
        // 7. Checkbox check
        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "Accept terms to continue", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validateAndSubmit() {
        if (validateForm()) {
            btnSignUp.setEnabled(false); // Prevent double clicks
            btnSignUp.setText("Verifying...");
            submitRegistration();
        }
    }

    private void submitRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // STEP 1: Create the User in Firebase Authentication only
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // STEP 2: Send the verification link
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    // STEP 3: Show the waiting dialog to the user
                                    showVerificationWaitingDialog(user);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * VERIFICATION LOGIC EXPLAINED:
     * This dialog stops the user from moving forward until they verify the email.
     * It solves the "Fake Email" problem because the data is NOT yet in Firestore.
     */
    private void showVerificationWaitingDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Your Email 📧");
        builder.setMessage("We sent a link to " + user.getEmail() + ".\n\n" +
                "1. Open your email app.\n" +
                "2. Click the verification link.\n" +
                "3. Return here and click 'I HAVE VERIFIED'.");

        builder.setPositiveButton("I HAVE VERIFIED", (dialog, which) -> {
            // RELOAD: This is critical. It fetches the latest status from Firebase servers.
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // STEP 4: Email is real! Now save to Firestore database.
                    saveUserDataToFirestore(user);
                } else {
                    // Link not clicked yet
                    Toast.makeText(this, "Verification not detected. Try again.", Toast.LENGTH_SHORT).show();
                    showVerificationWaitingDialog(user); // Loop back
                }
            });
        });

        builder.setNegativeButton("Change Email/Typo", (dialog, which) -> {
            // STEP 5: If the email was fake/typo, delete the auth account and let them try again.
            user.delete();
            btnSignUp.setEnabled(true);
            btnSignUp.setText("Sign Up");
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void saveUserDataToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the data to be saved
        Map<String, Object> data = new HashMap<>();
        data.put("User_Type", selectedUserType);
        data.put("Full_Name", etFullName.getText().toString().trim());
        data.put("Email", user.getEmail());
        data.put("Password",etPassword.getText().toString());
        data.put("Phone", etPhone.getText().toString().trim());
        data.put("UID", user.getUid());
        data.put("Timestamp", Timestamp.now());

        // Create a document in the "users" collection named after the Unique ID
        db.collection("users").document(user.getUid())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Signup Successfull!", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToLogin() {
        // 1. Initialize SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        editor.putString("saved_email", email);
        editor.putString("saved_password", password);

        // 3. Save and move
        editor.apply();

        // Create the intent and clear the backstack
        // This prevents the user from clicking the "Back" button to return to the signup form
        Intent intent = new Intent(UniversalSignupActivity.this, Donor_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish(); // Close current activity
    }

    private void openAppTermsAndConditions() {
        Intent intent = new Intent(this, AppTermsConditionsActivity.class);
        startActivityForResult(intent, 1003); // Request code for app terms and conditions
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1003) { // App terms and conditions request code
            if (resultCode == RESULT_OK) {
                // User accepted terms and conditions
                checkboxTerms.setChecked(true);
                Toast.makeText(this, "App Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User declined terms and conditions
                checkboxTerms.setChecked(false);
                Toast.makeText(this, "App Terms and Conditions declined", Toast.LENGTH_SHORT).show();
            }
        }
    }
}