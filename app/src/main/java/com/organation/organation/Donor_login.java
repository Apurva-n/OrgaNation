package com.organation.organation;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Donor_login extends AppCompatActivity {

    private EditText Email, Password;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvSignUp, tvForgotPassword;
    private ImageView ivTogglePassword;
    private ProgressBar progressBar;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_login2);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        progressBar = new ProgressBar(this);

        // Auto-fill from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Email.setText(sharedPreferences.getString("saved_email", ""));
        Password.setText(sharedPreferences.getString("saved_password", ""));

        btnLogin.setOnClickListener(v -> {
            String userEmail = Email.getText().toString().trim();
            String pwd = Password.getText().toString().trim();

            if (userEmail.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(userEmail, pwd);
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Donor_login.this, UniversalSignupActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        // Password visibility toggle
        ivTogglePassword.setOnClickListener(v -> {
            togglePasswordVisibility();
        });
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        // Success! Now fetch profile status
                        fetchProfileStatus(user.getUid());
                    } else {
                        handleUnverifiedUser();
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProfileStatus(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("User_Type");
                        Boolean isComplete = documentSnapshot.getBoolean("isProfileComplete");

                        if (isComplete != null && isComplete) {
                            // PASS THE TYPE HERE
                            goToDashboard(type, documentSnapshot);
                        } else {
                            navigateBasedOnType(type);
                        }
                    } else {
                        resetLoginButton("User profile missing in Database");
                    }
                })
                .addOnFailureListener(e -> resetLoginButton("Database Error: " + e.getMessage()));
    }

    private void showPendingVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account Verification Pending")
                .setMessage("Your hospital registration is pending verification by our admin team.\n\n" +
                        "You will receive an email once your account is approved.\n\n" +
                        "Please wait for admin approval before accessing the dashboard.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to login
                    Intent intent = new Intent(this, Donor_login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void navigateBasedOnType(String type) {
        if (type == null) return;

        Intent intent1;
        switch (type) {
            case "Donor": intent1 = new Intent(this, DonorRegistration.class); break;
            case "Hospital": intent1 = new Intent(this, hospital_registeration.class); break;
            case "Recipient": intent1 = new Intent(this, RecipientRegistration.class); break;
            case "Admin": intent1 = new Intent(this, AdminRegistrationActivity.class); break;
            default: intent1 = new Intent(this, Donor_main_page.class); break;
        }
        startActivity(intent1);
        finish();
    }

    private void goToDashboard(String type, com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        // Safety check for null type
        if (type == null) {
            intent = new Intent(this, Donor_login.class); // Default fallback
        }
        else if (type.equals("Donor")) {
            intent = new Intent(this, Donor_main_page.class);
        }
        else if (type.equals("Recipient")) {
            intent = new Intent(this, Recepeint_main_page.class);
        } else if (type.equals("Hospital")) {
            // Check hospital verification status before allowing dashboard access
            String hospitalId = documentSnapshot.getString("hospitalId");
            if (hospitalId != null) {
                db.collection("hospitals").document(hospitalId).get()
                        .addOnSuccessListener(hospitalDoc -> {
                            if (hospitalDoc.exists()) {
                                String verificationStatus = hospitalDoc.getString("verificationStatus");
                                if ("approved".equals(verificationStatus)) {
                                    intent = new Intent(this, hospital_main_page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Show pending verification message
                                    showPendingVerificationDialog();
                                }
                            } else {
                                Toast.makeText(this, "Hospital data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error checking verification status", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Hospital ID not found", Toast.LENGTH_SHORT).show();
            }
            return; // Don't continue to startActivity below
        }
        else if (type.equals("Admin")) {
            intent = new Intent(this, admin_main_page.class); // If you have one
        }else {
            intent = new Intent(this, Donor_login.class);
        }

        startActivity(intent);
        finish();
    }

    private void handleUnverifiedUser() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
        Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show();
        mAuth.signOut();
    }

    private void resetLoginButton(String message) {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showForgotPasswordDialog() {
        // Create dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.progressBar);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email address to receive password reset instructions.");
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        // Auto-fill current email if available
        String currentEmail = Email.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            etEmail.setText(currentEmail);
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Send Reset Email", (dialogInterface, which) -> {
            // Will be overridden below
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> {
            dialog.dismiss();
        });

        dialog.show();

        // Override positive button click to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress
            dialogProgressBar.setVisibility(View.VISIBLE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        dialogProgressBar.setVisibility(View.GONE);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        
                        Toast.makeText(this, "Password reset email sent! Please check your inbox.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        dialogProgressBar.setVisibility(View.GONE);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && errorMessage.contains("no user record")) {
                            Toast.makeText(this, "No account found with this email address", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Failed to send reset email: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void togglePasswordVisibility() {
        // Check current input type
        int currentInputType = Password.getInputType();
        
        if (currentInputType == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Password is currently hidden, show it
            Password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off); // Change to eye-off icon
        } else {
            // Password is currently visible, hide it
            Password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye); // Change to eye icon
        }
        
        // Move cursor to the end of the text
        Password.setSelection(Password.getText().length());
    }
}