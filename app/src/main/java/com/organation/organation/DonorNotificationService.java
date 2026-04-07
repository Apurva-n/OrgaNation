package com.organation.organation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DonorNotificationService {
    
    private static final String TAG = "DonorNotification";
    private Context context;
    private FirebaseFirestore db;
    
    public DonorNotificationService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    public void sendDonorNotification(RequestModel request, DonorModel donor, 
                                            String transplantDate, String transplantTime, 
                                            String hospitalName, NotificationCallback callback) {
        
        // Generate professional email content
        String emailSubject = generateEmailSubject(request, donor, transplantDate);
        String emailBody = generateEmailBody(request, donor, transplantDate, transplantTime, hospitalName);
        
        // Send email via Android Intent
        sendEmailIntent(donor.getEmail(), emailSubject, emailBody, callback);
        
        // Save notification record to Firestore
        saveNotificationRecord(request, donor, transplantDate, transplantTime, hospitalName);
    }
    
    public void sendDonorNotification(RequestModel request, DonorModel donor, 
                                            String transplantDate, String transplantTime, 
                                            String hospitalName, Map<String, String> hospitalDetails, NotificationCallback callback) {
        
        // Generate professional email content with actual hospital details
        String emailSubject = generateEmailSubject(request, donor, transplantDate);
        String emailBody = generateEmailBody(request, donor, transplantDate, transplantTime, hospitalName, hospitalDetails);
        
        // Send email via Android Intent
        sendEmailIntent(donor.getEmail(), emailSubject, emailBody, callback);
        
        // Save notification record to Firestore
        saveNotificationRecord(request, donor, transplantDate, transplantTime, hospitalName);
    }
    
    private String generateEmailSubject(RequestModel request, DonorModel donor, String transplantDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        
        return "🏥 ORGAN TRANSPLANT CONFIRMATION - " + donor.getFullName().toUpperCase() + 
               " - " + request.getOrganType().toUpperCase() + " - " + transplantDate;
    }
    
    private String generateEmailBody(RequestModel request, DonorModel donor, 
                                   String transplantDate, String transplantTime, String hospitalName) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        
        StringBuilder emailBody = new StringBuilder();
        
        // Professional Header
        emailBody.append("🏥 ORGANATION TRANSPLANT CONFIRMATION\n");
        emailBody.append("======================================\n\n");
        
        // Important Notice
        emailBody.append("🎉 CONGRATULATIONS! YOU HAVE BEEN SELECTED\n\n");
        emailBody.append("Dear ").append(donor.getFullName()).append(",\n\n");
        emailBody.append("We are pleased to inform you that you have been selected as the organ donor for a life-saving transplant procedure. Your selfless decision will make a significant difference in someone's life.\n\n");
        
        // Transplant Details
        emailBody.append("📋 TRANSPLANT DETAILS\n");
        emailBody.append("====================\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("👤 Recipient: ").append(getSafeString(request.getRecipientName(), "Recipient Name")).append("\n");
        emailBody.append("🫁 Organ Type: ").append(request.getOrganType()).append("\n");
        emailBody.append("🩸 Blood Group: ").append(request.getBloodType()).append("\n");
        emailBody.append("📅 Transplant Date: ").append(transplantDate).append("\n");
        emailBody.append("⏰ Transplant Time: ").append(transplantTime).append("\n");
        emailBody.append("📍 Hospital Location: ").append(getSafeString(request.getHospitalLocation(), request.getHospitalCity())).append("\n\n");
        
        // Preparation Instructions
        emailBody.append("📝 IMPORTANT PREPARATION INSTRUCTIONS\n");
        emailBody.append("=====================================\n");
        emailBody.append("1. 🍽️ FASTING: Please fast for 8 hours before the procedure\n");
        emailBody.append("2. 💊 MEDICATION: Bring all current medications to the hospital\n");
        emailBody.append("3. 📄 DOCUMENTS: Carry your ID card and medical records\n");
        emailBody.append("4. 🚗 TRANSPORT: Arrange for someone to drive you home post-procedure\n");
        emailBody.append("5. 📱 CONTACT: Save hospital emergency number for assistance\n\n");
        
        // What to Expect
        emailBody.append("🏥 WHAT TO EXPECT\n");
        emailBody.append("=================\n");
        emailBody.append("• Pre-operative tests and consultation\n");
        emailBody.append("• Anesthesia and surgical preparation\n");
        emailBody.append("• Procedure duration: 3-6 hours\n");
        emailBody.append("• Recovery period: 2-3 days in hospital\n");
        emailBody.append("• Complete recovery: 4-6 weeks\n\n");
        
        // Contact Information
        emailBody.append("📞 CONTACT INFORMATION\n");
        emailBody.append("=====================\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("👨‍⚕️ Treating Doctor: Dr. ").append(getSafeString(request.getTreatingDoctor(), "Hospital Doctor")).append("\n");
        emailBody.append("📱 Emergency: Please contact hospital reception for emergency assistance\n");
        emailBody.append("📧 Email: transplant@").append(hospitalName.toLowerCase().replace(" ", "")).append(".com\n\n");
        
        // Important Reminders
        emailBody.append("⚠️ IMPORTANT REMINDERS\n");
        emailBody.append("======================\n");
        emailBody.append("• Arrive 2 hours before scheduled time\n");
        emailBody.append("• Avoid alcohol and heavy meals 24 hours prior\n");
        emailBody.append("• Bring comfortable clothes for discharge\n");
        emailBody.append("• Arrange for post-procedure care at home\n");
        emailBody.append("• Keep emergency contact person informed\n\n");
        
        // Gratitude and Closing
        emailBody.append("🙏 OUR GRATITUDE\n");
        emailBody.append("================\n");
        emailBody.append("Your generosity and courage in choosing to donate an organ is truly remarkable. You are giving someone a second chance at life, and your contribution will be remembered forever.\n\n");
        
        emailBody.append("The transplant team and the recipient's family extend their heartfelt gratitude for your life-saving decision.\n\n");
        
        // Footer
        emailBody.append("📧 This email was sent by OrgaNation System\n");
        emailBody.append("📅 Sent on: ").append(currentDateTime).append("\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("🆔 Request ID: ").append(request.getRequestId()).append("\n");
        emailBody.append("🔗 Website: www.orgaNation.com\n\n");
        
        emailBody.append("For any queries or concerns, please contact the transplant coordinator immediately.\n\n");
        
        emailBody.append("With heartfelt gratitude,\n");
        emailBody.append("The OrgaNation Transplant Team\n");
        emailBody.append("🏥 ").append(hospitalName);
        
        return emailBody.toString();
    }
    
    private String generateEmailBody(RequestModel request, DonorModel donor, 
                                   String transplantDate, String transplantTime, String hospitalName, Map<String, String> hospitalDetails) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        
        StringBuilder emailBody = new StringBuilder();
        
        // Professional Header
        emailBody.append("🏥 ORGANATION TRANSPLANT CONFIRMATION\n");
        emailBody.append("======================================\n\n");
        
        // Important Notice
        emailBody.append("🎉 CONGRATULATIONS! YOU HAVE BEEN SELECTED\n\n");
        emailBody.append("Dear ").append(donor.getFullName()).append(",\n\n");
        emailBody.append("We are pleased to inform you that you have been selected as the organ donor for a life-saving transplant procedure. Your selfless decision will make a significant difference in someone's life.\n\n");
        
        // Transplant Details
        emailBody.append("📋 TRANSPLANT DETAILS\n");
        emailBody.append("====================\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("👤 Recipient: ").append(getSafeString(request.getRecipientName(), "Recipient Name")).append("\n");
        emailBody.append("🫁 Organ Type: ").append(request.getOrganType()).append("\n");
        emailBody.append("🩸 Blood Group: ").append(request.getBloodType()).append("\n");
        emailBody.append("📅 Transplant Date: ").append(transplantDate).append("\n");
        emailBody.append("⏰ Transplant Time: ").append(transplantTime).append("\n");
        
        // Use actual hospital location if available
        String hospitalLocation = hospitalDetails.getOrDefault("hospitalLocation", "");
        if (hospitalLocation.isEmpty()) {
            hospitalLocation = hospitalDetails.getOrDefault("street", "") + ", " + 
                             hospitalDetails.getOrDefault("city", "") + ", " + 
                             hospitalDetails.getOrDefault("state", "");
        }
        emailBody.append("📍 Hospital Location: ").append(hospitalLocation).append("\n\n");
        
        // Preparation Instructions
        emailBody.append("📝 IMPORTANT PREPARATION INSTRUCTIONS\n");
        emailBody.append("=====================================\n");
        emailBody.append("1. 🍽️ FASTING: Please fast for 8 hours before the procedure\n");
        emailBody.append("2. 💊 MEDICATION: Bring all current medications to the hospital\n");
        emailBody.append("3. 📄 DOCUMENTS: Carry your ID card and medical records\n");
        emailBody.append("4. 🚗 TRANSPORT: Arrange for someone to drive you home post-procedure\n");
        emailBody.append("5. 📱 CONTACT: Save hospital emergency number for assistance\n\n");
        
        // What to Expect
        emailBody.append("🏥 WHAT TO EXPECT\n");
        emailBody.append("=================\n");
        emailBody.append("• Pre-operative tests and consultation\n");
        emailBody.append("• Anesthesia and surgical preparation\n");
        emailBody.append("• Procedure duration: 3-6 hours\n");
        emailBody.append("• Recovery period: 2-3 days in hospital\n");
        emailBody.append("• Complete recovery: 4-6 weeks\n\n");
        
        // Contact Information - Use actual hospital details
        emailBody.append("📞 CONTACT INFORMATION\n");
        emailBody.append("=====================\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("👨‍⚕️ Treating Doctor: Dr. ").append(getSafeString(request.getTreatingDoctor(), "Hospital Doctor")).append("\n");
        
        // Use actual hospital contact number
        String contactNumber = hospitalDetails.getOrDefault("contactNumber", "");
        if (!contactNumber.isEmpty()) {
            emailBody.append("📱 Emergency Contact: ").append(contactNumber).append("\n");
        } else {
            emailBody.append("📱 Emergency: Please contact hospital reception for emergency assistance\n");
        }
        
        // Use actual hospital email
        String hospitalEmail = hospitalDetails.getOrDefault("officialEmail", "");
        if (!hospitalEmail.isEmpty()) {
            emailBody.append("📧 Email: ").append(hospitalEmail).append("\n");
        } else {
            emailBody.append("📧 Email: transplant@").append(hospitalName.toLowerCase().replace(" ", "")).append(".com\n");
        }
        
        // Use actual hospital website
        String website = hospitalDetails.getOrDefault("websiteUrl", "");
        if (!website.isEmpty()) {
            emailBody.append("🌐 Website: ").append(website).append("\n");
        }
        emailBody.append("\n");
        
        // Important Reminders
        emailBody.append("⚠️ IMPORTANT REMINDERS\n");
        emailBody.append("======================\n");
        emailBody.append("• Arrive 2 hours before scheduled time\n");
        emailBody.append("• Avoid alcohol and heavy meals 24 hours prior\n");
        emailBody.append("• Bring comfortable clothes for discharge\n");
        emailBody.append("• Arrange for post-procedure care at home\n");
        emailBody.append("• Keep emergency contact person informed\n\n");
        
        // Gratitude and Closing
        emailBody.append("🙏 OUR GRATITUDE\n");
        emailBody.append("================\n");
        emailBody.append("Your generosity and courage in choosing to donate an organ is truly remarkable. You are giving someone a second chance at life, and your contribution will be remembered forever.\n\n");
        
        emailBody.append("The transplant team and the recipient's family extend their heartfelt gratitude for your life-saving decision.\n\n");
        
        // Footer
        emailBody.append("📧 This email was sent by OrgaNation System\n");
        emailBody.append("📅 Sent on: ").append(currentDateTime).append("\n");
        emailBody.append("🏥 Hospital: ").append(hospitalName).append("\n");
        emailBody.append("🆔 Request ID: ").append(request.getRequestId()).append("\n");
        emailBody.append("🔗 Website: www.orgaNation.com\n\n");
        
        emailBody.append("For any queries or concerns, please contact the transplant coordinator immediately.\n\n");
        
        emailBody.append("With heartfelt gratitude,\n");
        emailBody.append("The OrgaNation Transplant Team\n");
        emailBody.append("🏥 ").append(hospitalName);
        
        return emailBody.toString();
    }
    
    private String getSafeString(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    
    private void sendEmailIntent(String recipientEmail, String subject, String body, NotificationCallback callback) {
        try {
            Log.d(TAG, "Attempting to send email to: " + recipientEmail);
            
            // Use ACTION_SEND with message/rfc822 type (more reliable)
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            
            // Debug: Check what apps can handle this intent
            Log.d(TAG, "Email intent created: " + emailIntent.toString());
            Log.d(TAG, "Intent type: " + emailIntent.getType());
            
            // Check if email app is available
            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "Email app found, opening chooser");
                
                // Show confirmation dialog before opening email
                showEmailConfirmationDialog(recipientEmail, subject, body, emailIntent, callback);
                
            } else {
                Log.d(TAG, "No email app found for ACTION_SEND, trying ACTION_SENDTO");
                // Try alternative approach with ACTION_SENDTO
                Intent alternativeIntent = new Intent(Intent.ACTION_SENDTO);
                alternativeIntent.setData(Uri.parse("mailto:" + recipientEmail));
                alternativeIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                alternativeIntent.putExtra(Intent.EXTRA_TEXT, body);
                
                if (alternativeIntent.resolveActivity(context.getPackageManager()) != null) {
                    Log.d(TAG, "ACTION_SENDTO worked, opening chooser");
                    showEmailConfirmationDialog(recipientEmail, subject, body, alternativeIntent, callback);
                } else {
                    Log.d(TAG, "No email app found at all, showing options dialog");
                    // No email app available - show user options
                    showEmailOptionsDialog(recipientEmail, subject, body, callback);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending email", e);
            Toast.makeText(context, "Error sending email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onNotificationSent(false);
            }
        }
    }
    
    private void showEmailConfirmationDialog(String recipientEmail, String subject, String body, Intent emailIntent, NotificationCallback callback) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Send Email to Donor")
                .setMessage("Email will be sent to:\n\n" +
                        "📧 To: " + recipientEmail + "\n" +
                        "📋 Subject: " + subject + "\n\n" +
                        "Please click SEND in your email app to complete the notification.")
                .setPositiveButton("Open Email App", (dialog, which) -> {
                    // Start email activity
                    context.startActivity(Intent.createChooser(emailIntent, "Send Transplant Confirmation"));
                    
                    // Show user instruction
                    Toast.makeText(context, "Please click SEND in your email app to notify the donor", Toast.LENGTH_LONG).show();
                    
                    // Mark as "opened" but not necessarily "sent"
                    if (callback != null) {
                        callback.onNotificationOpened(); // New callback for opened status
                    }
                    
                    // Save notification record anyway (user can send later)
                    saveNotificationRecordWithStatus(recipientEmail, subject, body, "EMAIL_OPENED");
                    
                    // Set up a timer to check if email was sent (approximate)
                    scheduleEmailDeliveryCheck(recipientEmail, callback);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (callback != null) {
                        callback.onNotificationCancelled();
                    }
                })
                .setNeutralButton("Copy Email Details", (dialog, which) -> {
                    copyEmailDetailsToClipboard(recipientEmail, subject, body);
                    if (callback != null) {
                        callback.onNotificationCopied();
                    }
                })
                .show();
    }
    
    private void scheduleEmailDeliveryCheck(String recipientEmail, NotificationCallback callback) {
        // Schedule a check after 30 seconds to see if user might have sent the email
        // This is approximate - we can't actually track if email was sent
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            // Show reminder to user
            Toast.makeText(context, "Reminder: Please ensure you clicked SEND in the email app", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Email delivery reminder shown for: " + recipientEmail);
        }, 30000); // 30 seconds
    }
    
    // Enhanced callback interface
    public interface NotificationCallback {
        void onNotificationSent(boolean success);
        default void onNotificationOpened() {}
        default void onNotificationCancelled() {}
        default void onNotificationCopied() {}
    }
    
    private void saveNotificationRecordWithStatus(String recipientEmail, String subject, String body, String status) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientEmail", recipientEmail);
            notificationData.put("subject", subject);
            notificationData.put("body", body);
            notificationData.put("status", status); // EMAIL_OPENED, EMAIL_SENT, EMAIL_FAILED
            notificationData.put("timestamp", System.currentTimeMillis());
            
            // Save to Firestore
            db.collection("notifications").add(notificationData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Notification record saved with status: " + status);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving notification record", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in saveNotificationRecordWithStatus", e);
        }
    }
    
    private void showEmailOptionsDialog(String recipientEmail, String subject, String body, NotificationCallback callback) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Email App Not Found")
                .setMessage("No email app is installed on this device. Please choose an option:")
                .setPositiveButton("Copy Email Details", (dialog, which) -> {
                    copyEmailDetailsToClipboard(recipientEmail, subject, body);
                    if (callback != null) {
                        callback.onNotificationSent(false); // Mark as not sent, but details provided
                    }
                })
                .setNegativeButton("Skip Email", (dialog, which) -> {
                    Toast.makeText(context, "Email notification skipped. You can manually email the donor.", Toast.LENGTH_LONG).show();
                    if (callback != null) {
                        callback.onNotificationSent(false);
                    }
                })
                .setNeutralButton("Install Gmail", (dialog, which) -> {
                    // Redirect to Play Store to install Gmail
                    try {
                        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
                        playStoreIntent.setData(Uri.parse("market://details?id=com.google.android.gm"));
                        context.startActivity(playStoreIntent);
                    } catch (Exception e) {
                        // Fallback to browser
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm"));
                        context.startActivity(browserIntent);
                    }
                })
                .setCancelable(false)
                .show();
    }
    
    private void copyEmailDetailsToClipboard(String recipientEmail, String subject, String body) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Email Details", 
                "To: " + recipientEmail + "\n" +
                "Subject: " + subject + "\n\n" +
                "Body:\n" + body);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(context, "Email details copied to clipboard! Paste in any email app.", Toast.LENGTH_LONG).show();
    }
    
    private void saveNotificationRecord(RequestModel request, DonorModel donor, 
                                     String transplantDate, String transplantTime, String hospitalName) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientName", request.getRecipientName());
            notificationData.put("recipientAadhaar", request.getRecipientAadhaar());
            notificationData.put("donorName", donor.getFullName());
            notificationData.put("donorAadhaar", donor.getAadhaarNo());
            notificationData.put("donorEmail", donor.getEmail());
            notificationData.put("donorPhone", donor.getPhone());
            notificationData.put("organType", request.getOrganType());
            notificationData.put("bloodGroup", request.getBloodType());
            notificationData.put("transplantDate", transplantDate);
            notificationData.put("transplantTime", transplantTime);
            notificationData.put("hospitalName", hospitalName);
            notificationData.put("treatingDoctor", request.getTreatingDoctor());
            notificationData.put("hospitalLocation", request.getHospitalLocation());
            notificationData.put("requestId", request.getRequestId());
            notificationData.put("notificationSent", true);
            notificationData.put("notificationSentAt", new Date());
            notificationData.put("status", "scheduled");
            
            // Save to transplant_notifications collection
            db.collection("transplant_notifications")
                    .document(request.getRequestId() + "_" + donor.getAadhaarNo())
                    .set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Notification record saved successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving notification record", e);
                    });
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating notification record", e);
        }
    }
    
    public void sendPreTransplantReminder(String notificationId, String recipientEmail, 
                                        String donorName, String transplantDate, String transplantTime) {
        String subject = "🏥 TRANSPLANT REMINDER - " + donorName.toUpperCase() + " - TOMORROW";
        
        String body = "📋 TRANSPLANT REMINDER\n" +
                     "====================\n\n" +
                     "Dear " + donorName + ",\n\n" +
                     "This is a friendly reminder about your scheduled organ transplant tomorrow:\n\n" +
                     "📅 Date: " + transplantDate + "\n" +
                     "⏰ Time: " + transplantTime + "\n\n" +
                     "📝 REMINDERS:\n" +
                     "• Fast for 8 hours before the procedure\n" +
                     "• Arrive 2 hours before scheduled time\n" +
                     "• Bring ID and medical documents\n" +
                     "• Arrange for post-procedure transport\n\n" +
                     "We look forward to seeing you at the hospital.\n\n" +
                     "With best wishes,\n" +
                     "The Transplant Team";
        
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            
            context.startActivity(Intent.createChooser(emailIntent, "Send Transplant Reminder"));
        } catch (Exception e) {
            Log.e(TAG, "Error sending reminder", e);
        }
    }
}
