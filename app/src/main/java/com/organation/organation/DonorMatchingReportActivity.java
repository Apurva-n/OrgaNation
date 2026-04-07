package com.organation.organation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class DonorMatchingReportActivity extends AppCompatActivity {
    
    private RecyclerView rvDonorMatches;
    private ProgressBar progressBar;
    private TextView tvRecipientInfo, tvNoMatches;
    private LinearLayout llContent;
    private Button btnExportReport;
    
    private RecipientModel recipient;
    private DonorMatchAdapter matchAdapter;
    private MLDonorMatchingService matchingService;
    private List<MLDonorMatchingService.DonorMatchResult> donorMatches;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_matching_report);
        
        // Get recipient data from intent
        recipient = (RecipientModel) getIntent().getSerializableExtra("recipient");
        if (recipient == null) {
            Toast.makeText(this, "Error: Recipient data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupRecyclerView();
        startDonorMatching();
    }
    
    private void initializeViews() {
        rvDonorMatches = findViewById(R.id.rvDonorMatches);
        progressBar = findViewById(R.id.progressBar);
        tvRecipientInfo = findViewById(R.id.tvRecipientInfo);
        tvNoMatches = findViewById(R.id.tvNoMatches);
        llContent = findViewById(R.id.llContent);
        btnExportReport = findViewById(R.id.btnExportReport);
        
        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Top 10 Compatible Donors");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Display recipient information
        tvRecipientInfo.setText("Finding compatible donors for: " + recipient.getFullName() + 
                               " (Blood Group: " + recipient.getBloodGroup() + 
                               ", Organ Needed: " + recipient.getOrgansNeeded() + ")");
        
        // Export button click listener
        btnExportReport.setOnClickListener(v -> exportMatchingReport());
        
        // Initialize matching service
        matchingService = new MLDonorMatchingService(this);
    }
    
    private void setupRecyclerView() {
        matchAdapter = new DonorMatchAdapter(this, donorMatches, new DonorMatchAdapter.OnDonorActionListener() {
            @Override
            public void onCallDonor(String phoneNumber) {
                makePhoneCall(phoneNumber);
            }
            
            @Override
            public void onEmailDonor(String emailAddress) {
                sendEmail(emailAddress);
            }
            
            @Override
            public void onViewDonorDetails(DonorModel donor) {
                showDonorDetailsDialog(donor);
            }
        });
        
        rvDonorMatches.setLayoutManager(new LinearLayoutManager(this));
        rvDonorMatches.setAdapter(matchAdapter);
    }
    
    private void startDonorMatching() {
        progressBar.setVisibility(View.VISIBLE);
        llContent.setVisibility(View.GONE);
        
        matchingService.findTopCompatibleDonors(recipient, new MLDonorMatchingService.DonorMatchingCallback() {
            @Override
            public void onMatchingCompleted(List<MLDonorMatchingService.DonorMatchResult> topDonors) {
                progressBar.setVisibility(View.GONE);
                
                // Add debug logging
                Log.d("DonorMatchingReport", "Matching completed with " + topDonors.size() + " donors");
                
                if (topDonors.isEmpty()) {
                    Log.d("DonorMatchingReport", "No donors found - showing empty state");
                    tvNoMatches.setVisibility(View.VISIBLE);
                    tvNoMatches.setText("No compatible donors found for this recipient.\n\n" +
                            "Possible reasons:\n" +
                            "• No donors with compatible blood group\n" +
                            "• No donors with required organ\n" +
                            "• All donors filtered out by compatibility score");
                    btnExportReport.setEnabled(false);
                } else {
                    Log.d("DonorMatchingReport", "Donors found - updating adapter");
                    llContent.setVisibility(View.VISIBLE);
                    matchAdapter.updateDonorMatches(topDonors);
                    
                    String summary = "Found " + topDonors.size() + " compatible donors";
                    tvNoMatches.setVisibility(View.GONE);
                    // You could update a TextView with this summary if you have one
                    
                    donorMatches = topDonors;
                    
                    Toast.makeText(DonorMatchingReportActivity.this, 
                                 "Found " + topDonors.size() + " compatible donors", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onMatchingFailed(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DonorMatchingReportActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    private void makePhoneCall(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendEmail(String emailAddress) {
        if (emailAddress != null && !emailAddress.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + emailAddress));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Organ Donation Inquiry - " + recipient.getFullName());
            intent.putExtra(Intent.EXTRA_TEXT, "Dear " + emailAddress + ",\n\n" +
                    "We found you as a potential compatible donor for " + recipient.getFullName() + 
                    " who needs " + recipient.getOrgansNeeded() + ".\n\n" +
                    "Please contact us for further details.\n\n" +
                    "Thank you for your consideration.\n\n" +
                    "OrgaNation Team");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Email address not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showDonorDetailsDialog(DonorModel donor) {
        DecimalFormat df = new DecimalFormat("#.##");
        
        String details = "📋 Donor Details\n\n" +
                "👤 Name: " + donor.getFullName() + "\n" +
                "🆔 Aadhaar: " + (donor.getAadhaarNo() != null ? donor.getAadhaarNo() : "N/A") + "\n" +
                "🎂 Age: " + donor.getAge() + " years\n" +
                "⚥ Gender: " + donor.getGender() + "\n" +
                "🩸 Blood Group: " + donor.getBloodGroup() + "\n" +
                "📏 Height: " + donor.getHeight() + " cm\n" +
                "⚖️ Weight: " + donor.getWeight() + " kg\n" +
                "📞 Phone: " + (donor.getPhone() != null ? donor.getPhone() : "N/A") + "\n" +
                "📧 Email: " + (donor.getEmail() != null ? donor.getEmail() : "N/A") + "\n" +
                "📍 Address: " + donor.getFullAddress() + "\n" +
                "🏥 Organs to Donate: " + donor.getOrgansToNDonate();
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Donor Information")
                .setMessage(details)
                .setPositiveButton("Call", (dialog, which) -> makePhoneCall(donor.getPhone()))
                .setNegativeButton("Email", (dialog, which) -> sendEmail(donor.getEmail()))
                .setNeutralButton("Close", null)
                .show();
    }
    
    private void exportMatchingReport() {
        if (donorMatches == null || donorMatches.isEmpty()) {
            Toast.makeText(this, "No donor data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create detailed report text
        StringBuilder report = new StringBuilder();
        report.append("ORGAN DONATION MATCHING REPORT\n");
        report.append("================================\n\n");
        
        report.append("RECIPIENT INFORMATION:\n");
        report.append("Name: ").append(recipient.getFullName()).append("\n");
        report.append("Blood Group: ").append(recipient.getBloodGroup()).append("\n");
        report.append("Organ Needed: ").append(recipient.getOrgansNeeded()).append("\n");
        report.append("Age: ").append(recipient.getAge()).append("\n");
        report.append("Gender: ").append(recipient.getGender()).append("\n");
        report.append("Address: ").append(recipient.getFullAddress()).append("\n\n");
        
        // Add Hospital Information
        Map<String, String> hospitalDetails = recipient.getHospitalDetails();
        if (hospitalDetails != null && !hospitalDetails.isEmpty()) {
            report.append("HOSPITAL INFORMATION:\n");
            report.append("=====================\n");
            report.append("Hospital Name: ").append(hospitalDetails.getOrDefault("hospitalName", "N/A")).append("\n");
            report.append("Contact Number: ").append(hospitalDetails.getOrDefault("contactNumber", "N/A")).append("\n");
            report.append("Official Email: ").append(hospitalDetails.getOrDefault("officialEmail", "N/A")).append("\n");
            report.append("Website: ").append(hospitalDetails.getOrDefault("websiteUrl", "N/A")).append("\n");
            report.append("Address: ").append(hospitalDetails.getOrDefault("street", ""))
                      .append(", ").append(hospitalDetails.getOrDefault("city", ""))
                      .append(", ").append(hospitalDetails.getOrDefault("state", "")).append("\n");
            report.append("Treating Doctor: Dr. ").append(hospitalDetails.getOrDefault("treatingDoctor", "N/A")).append("\n");
            report.append("Hospital Type: ").append(hospitalDetails.getOrDefault("hospitalType", "N/A")).append("\n");
            report.append("Gov. Reg. Number: ").append(hospitalDetails.getOrDefault("govRegNumber", "N/A")).append("\n\n");
        }
        
        report.append("TOP COMPATIBLE DONORS:\n");
        report.append("====================\n\n");
        
        DecimalFormat df = new DecimalFormat("#.##");
        for (int i = 0; i < donorMatches.size(); i++) {
            MLDonorMatchingService.DonorMatchResult match = donorMatches.get(i);
            DonorModel donor = match.donor;
            
            report.append("Rank ").append(i + 1).append(" - Compatibility: ").append(df.format(match.compatibilityScore * 100)).append("%\n");
            report.append("Name: ").append(donor.getFullName()).append("\n");
            report.append("Age: ").append(donor.getAge()).append(" | Gender: ").append(donor.getGender()).append("\n");
            report.append("Blood Group: ").append(donor.getBloodGroup()).append("\n");
            report.append("Height: ").append(donor.getHeight()).append(" cm | Weight: ").append(donor.getWeight()).append(" kg\n");
            report.append("Phone: ").append(donor.getPhone() != null ? donor.getPhone() : "N/A").append("\n");
            report.append("Email: ").append(donor.getEmail() != null ? donor.getEmail() : "N/A").append("\n");
            report.append("Address: ").append(donor.getFullAddress()).append("\n");
            report.append("Organs to Donate: ").append(donor.getOrgansToNDonate()).append("\n");
            report.append("----------------------------------------\n\n");
        }
        
        report.append("Report Generated: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date())).append("\n");
        report.append("Generated by: OrgaNation System\n");
        
        // Share the report
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Donor Matching Report - " + recipient.getFullName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Donor Matching Report"));
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
