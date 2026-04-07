package com.organation.organation;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AppTermsConditionsActivity extends AppCompatActivity {

    private WebView webViewTerms;
    private TextView btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_terms_conditions);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Terms and Conditions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        webViewTerms = findViewById(R.id.webViewTerms);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        // Set up WebView
        setupWebView();

        // Set up click listeners
        btnAccept.setOnClickListener(v -> {
            // Return to login with terms accepted
            setResult(RESULT_OK);
            finish();
        });

        btnDecline.setOnClickListener(v -> {
            // Return to login with terms declined
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupWebView() {
        // Enable JavaScript for better formatting
        webViewTerms.getSettings().setJavaScriptEnabled(true);
        
        // Set WebViewClient to handle navigation
        webViewTerms.setWebViewClient(new WebViewClient());

        // Load terms and conditions content
        String termsContent = getAppTermsAndConditionsContent();
        String formattedContent = "<html><head><style>" +
                "body { font-family: sans-serif; padding: 16px; line-height: 1.6; color: #333; }" +
                "h1 { color: #E91E63; border-bottom: 2px solid #E91E63; padding-bottom: 8px; }" +
                "h2 { color: #E91E63; margin-top: 24px; }" +
                "h3 { color: #666; margin-top: 20px; }" +
                "ul { margin: 12px 0; padding-left: 20px; }" +
                "li { margin: 8px 0; }" +
                ".highlight { background-color: #FFF9C4; padding: 12px; border-radius: 8px; margin: 16px 0; }" +
                ".warning { background-color: #FFEBEE; padding: 12px; border-radius: 8px; margin: 16px 0; }" +
                ".info { background-color: #E3F2FD; padding: 12px; border-radius: 8px; margin: 16px 0; }" +
                "</style></head><body>" + termsContent + "</body></html>";

        webViewTerms.loadDataWithBaseURL(null, formattedContent, "text/html", "UTF-8", null);
    }

    private String getAppTermsAndConditionsContent() {
        return "<h1>OrgaNation App Terms and Conditions</h1>" +
                
                "<h2>1. Acceptance of Terms</h2>" +
                "<div class='highlight'>" +
                "<strong>By downloading, installing, or using the OrgaNation app, you agree to be bound by these Terms and Conditions.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>If you do not agree to these terms, do not use the app</li>" + "<li>These terms constitute a legally binding agreement</li>" +
                "<li>You must be at least 18 years old to use this app</li>" +
                "<li>You must have the legal capacity to enter into this agreement</li>" +
                "</ul>" +

                "<h2>2. App Description and Purpose</h2>" +
                "<p>OrgaNation is a healthcare platform designed to:</p>" +
                "<ul>" +
                "<li>Connect organ donors with recipients in need</li>" +
                "<li>Facilitate organ donation processes</li>" +
                "<li>Provide healthcare matching services</li>" +
                "<li>Maintain secure medical records</li>" +
                "<li>Enable communication between healthcare providers</li>" +
                "</ul>" +

                "<h2>3. User Account and Registration</h2>" +
                "<div class='info'>" +
                "<strong>To use the app, you must create an account and provide accurate information.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>You must provide truthful, accurate, and complete information</li>" +
                "<li>You are responsible for maintaining the confidentiality of your account</li>" +
                "<li>You must notify us immediately of any unauthorized use of your account</li>" +
                "<li>You are responsible for all activities under your account</li>" +
                "<li>We reserve the right to terminate accounts for violations</li>" +
                "</ul>" +

                "<h2>4. Privacy and Data Protection</h2>" +
                "<p>Your privacy is important to us. We collect and process your data as follows:</p>" +
                "<ul>" +
                "<li>Personal information: Name, contact details, medical information</li>" +
                "<li>Usage data: App interactions, preferences, and session information</li>" +
                "<li>Device information: Device type, operating system, unique identifiers</li>" +
                "<li>We use Firebase Firestore for secure data storage</li>" +
                "<li>Data is encrypted in transit and at rest</li>" +
                "<li>We comply with applicable privacy laws and regulations</li>" +
                "</ul>" +

                "<h2>5. Medical Information and Health Data</h2>" +
                "<div class='warning'>" +
                "<strong>Medical information is sensitive and requires special protection.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Medical information is used only for healthcare matching</li>" +
                "<li>We do not sell or share medical data with third parties</li>" +
                "<li>Medical records are accessible only to authorized healthcare providers</li>" +
                "<li>You can request deletion of your medical data at any time</li>" +
                "<li>We maintain audit trails for all medical data access</li>" +
                "</ul>" +

                "<h2>6. Prohibited Activities</h2>" +
                "<p>You agree not to use the app for:</p>" +
                "<ul>" +
                "<li>Any illegal or unauthorized purpose</li>" +
                "<li>Fraudulent organ donation activities</li>" +
                "<li>Impersonating another person or entity</li>" +
                "<li>Interfering with or disrupting the app's functionality</li>" +
                "<li>Attempting to gain unauthorized access to our systems</li>" +
                "<li>Violating any applicable laws or regulations</li>" +
                "</ul>" +

                "<h2>7. Intellectual Property Rights</h2>" +
                "<p>The app and its content are owned by OrgaNation and are protected by:</p>" +
                "<ul>" +
                "<li>Copyright laws</li>" +
                "<li>Trademark laws</li>" +
                "<li>Trade secret laws</li>" +
                "<li>Other intellectual property rights</li>" +
                "<li>You may not copy, modify, or distribute our content</li>" +
                "</ul>" +

                "<h2>8. Healthcare Provider Network</h2>" +
                "<div class='info'>" +
                "<strong>We work with certified healthcare providers and medical facilities.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>All healthcare providers are verified and certified</li>" +
                "<li>Medical facilities must meet our quality standards</li>" +
                "<li>We maintain a directory of authorized providers</li>" +
                "<li>Providers must follow medical ethics and guidelines</li>" +
                "<li>We monitor provider compliance and performance</li>" +
                "</ul>" +

                "<h2>9. Limitation of Liability</h2>" +
                "<div class='warning'>" +
                "<strong>Our liability is limited as described in this section.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>The app is provided on an \"as is\" and \"as available\" basis</li>" +
                "<li>We are not liable for medical advice or treatment decisions</li>" +
                "<li>We are not liable for data loss or corruption</li>" +
                "<li>We are not liable for third-party services or content</li>" +
                "<li>Our total liability shall not exceed the amount paid for the service</li>" +
                "</ul>" +

                "<h2>10. Service Availability and Support</h2>" +
                "<p>We strive to provide reliable service, but:</p>" +
                "<ul>" +
                "<li>Service availability is not guaranteed</li>" +
                "<li>We may experience downtime for maintenance</li>" +
                "<li>We are not liable for service interruptions</li>" +
                "<li>Support is provided through our customer service channels</li>" +
                "<li>Response times may vary based on volume</li>" +
                "</ul>" +

                "<h2>11. Termination of Service</h2>" +
                "<p>We may terminate or suspend your account for:</p>" +
                "<ul>" +
                "<li>Violation of these terms and conditions</li>" +
                "<li>Illegal or fraudulent activities</li>" +
                "<li>Non-payment of applicable fees</li>" +
                "<li>Providing false or misleading information</li>" +
                "<li>Violating healthcare regulations or ethics</li>" +
                "</ul>" +

                "<h2>12. Changes to Terms and Conditions</h2>" +
                "<div class='highlight'>" +
                "<strong>We may update these terms from time to time.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Changes will be effective immediately upon posting</li>" +
                "<li>You will be notified of significant changes</li>" +
                "<li>Continued use of the app constitutes acceptance</li>" +
                "<li>You should review these terms periodically</li>" +
                "</ul>" +

                "<h2>13. Contact Information</h2>" +
                "<p>For questions about these terms and conditions:</p>" +
                "<ul>" +
                "<li>Email: support@organation.com</li>" +
                "<li>Phone: [Support Phone Number]</li>" +
                "<li>Address: [Company Address]</li>" +
                "<li>Website: www.organation.com</li>" +
                "</ul>" +

                "<h2>14. Governing Law and Jurisdiction</h2>" +
                "<p>These terms are governed by:</p>" +
                "<ul>" +
                "<li>Applicable national and state laws</li>" +
                "<li>Healthcare regulations and compliance requirements</li>" +
                "<li>Data protection and privacy laws</li>" +
                "<li>Disputes shall be resolved through arbitration</li>" +
                "</ul>" +

                "<h2>15. Acknowledgment</h2>" +
                "<div class='highlight'>" +
                "<strong>By accepting these terms, you acknowledge that you have read, understood, and agree to be bound by all conditions.</strong>" +
                "</div>" +
                "<p>You understand that this is a legally binding agreement and that you may seek independent legal counsel if desired.</p>" +
                "<p><strong>Effective Date:</strong> [Date of Acceptance]</p>" +
                "<p><strong>Last Updated:</strong> [Date of Last Update]</p>" +
                "<p><strong>Version:</strong> 1.0</p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
