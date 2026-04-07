package com.organation.organation;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HospitalTermsConditionsActivity extends AppCompatActivity {

    private WebView webViewTerms;
    private TextView btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_terms_conditions);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hospital Terms and Conditions");
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
            // Return to registration with terms accepted
            setResult(RESULT_OK);
            finish();
        });

        btnDecline.setOnClickListener(v -> {
            // Return to registration with terms declined
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
        String termsContent = getHospitalTermsAndConditionsContent();
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

    private String getHospitalTermsAndConditionsContent() {
        return "<h1>Hospital Registration Terms and Conditions</h1>" +
                
                "<h2>1. Hospital Accreditation and Compliance</h2>" +
                "<div class='highlight'>" +
                "<strong>By registering with OrgaNation, you confirm that your hospital meets all required healthcare standards and legal requirements.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Hospital must be legally registered and accredited</li>" +
                "<li>All medical licenses must be current and valid</li>" +
                "<li>Compliance with national healthcare regulations</li>" +
                "<li>Adherence to medical ethics and standards</li>" +
                "<li>Regular quality audits and certifications</li>" +
                "</ul>" +

                "<h2>2. Medical Facility Requirements</h2>" +
                "<p>Your hospital must maintain the following standards:</p>" +
                "<ul>" +
                "<li>Fully equipped medical facilities</li>" +
                "<li>Certified medical professionals and staff</li>" +
                "<li>Emergency response capabilities</li>" +
                "<li>Organ transplant facilities (if applicable)</li>" +
                "<li>Laboratory and diagnostic services</li>" +
                "<li>ICU and critical care units</li>" +
                "<li>Blood bank and storage facilities</li>" +
                "</ul>" +

                "<h2>3. Organ Transplant Participation</h2>" +
                "<div class='warning'>" +
                "<strong>Participation in organ transplant programs requires strict compliance with medical and legal standards.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Must have certified transplant surgeons and medical team</li>" +
                "<li>Organ storage and preservation facilities</li>" +
                "<li>Tissue typing and compatibility testing capabilities</li>" +
                "<li>Post-operative care and monitoring facilities</li>" +
                "<li>Compliance with organ transplant regulations</li>" +
                "<li>Regular reporting to transplant authorities</li>" +
                "</ul>" +

                "<h2>4. Data Management and Privacy</h2>" +
                "<p>We are committed to protecting patient and hospital data:</p>" +
                "<ul>" +
                "<li>All patient data must be kept confidential and secure</li>" +
                "<li>Compliance with healthcare data protection laws</li>" +
                "<li>Secure storage of medical records</li>" +
                "<li>Limited access to sensitive patient information</li>" +
                "<li>Regular data security audits</li>" +
                "<li>Breach notification protocols</li>" +
                "</ul>" +

                "<h2>5. Quality Assurance and Standards</h2>" +
                "<div class='info'>" +
                "<strong>Your hospital must maintain high quality standards in all medical services.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Regular quality control measures</li>" +
                "<li>Patient safety protocols</li>" +
                "<li>Infection control standards</li>" +
                "<li>Medical waste management</li>" +
                "<li>Emergency response procedures</li>" +
                "<li>Continuous staff training and development</li>" +
                "</ul>" +

                "<h2>6. Emergency Services Availability</h2>" +
                "<p>Hospitals must provide 24/7 emergency services:</p>" +
                "<ul>" +
                "<li>24-hour emergency department</li>" +
                "<li>Ambulance services and transportation</li>" +
                "<li>Emergency medical equipment and supplies</li>" +
                "<li>Qualified emergency medical staff</li>" +
                "<li>Triage and emergency response protocols</li>" +
                "</ul>" +

                "<h2>7. Organ Donation Coordination</h2>" +
                "<p>For hospitals participating in organ donation:</p>" +
                "<ul>" +
                "<li>Dedicated organ donation coordinator</li>" +
                "<li>Organ retrieval and preservation facilities</li>" +
                "<li>Coordination with organ transplant organizations</li>" +
                "<li>Family counseling and support services</li>" +
                "<li>Legal documentation for organ donation</li>" +
                "<li>Post-donation care and follow-up</li>" +
                "</ul>" +

                "<h2>8. Professional Staff Requirements</h2>" +
                "<p>Your hospital must maintain qualified medical staff:</p>" +
                "<ul>" +
                "<li>Board-certified physicians and surgeons</li>" +
                "<li>Qualified nursing staff</li>" +
                "<li>Licensed medical technicians</li>" +
                "<li>Administrative and support staff</li>" +
                "<li>Regular professional development</li>" +
                "<li>Staff credential verification</li>" +
                "</ul>" +

                "<h2>9. Facility Maintenance and Safety</h2>" +
                "<div class='warning'>" +
                "<strong>Hospital facilities must meet safety and maintenance standards.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Regular facility inspections and maintenance</li>" +
                "<li>Fire safety and emergency evacuation plans</li>" +
                "<li>Medical equipment calibration and maintenance</li>" +
                "<li>Sanitation and hygiene standards</li>" +
                "<li>Patient safety protocols</li>" +
                "<li>Environmental safety compliance</li>" +
                "</ul>" +

                "<h2>10. Legal and Regulatory Compliance</h2>" +
                "<p>Your hospital must comply with all applicable laws:</p>" +
                "<ul>" +
                "<li>Healthcare facility licensing requirements</li>" +
                "<li>Medical practice regulations</li>" +
                "<li>Organ transplant laws and guidelines</li>" +
                "<li>Patient rights and privacy laws</li>" +
                "<li>Medical malpractice insurance requirements</li>" +
                "<li>Regular regulatory inspections</li>" +
                "</ul>" +

                "<h2>11. Financial and Insurance Requirements</h2>" +
                "<ul>" +
                "<li>Valid medical malpractice insurance</li>" +
                "<li>Financial transparency in billing</li>" +
                "<li>Insurance provider partnerships</li>" +
                "<li>Emergency care coverage policies</li>" +
                "<li>Organ transplant cost transparency</li>" +
                "<li>Financial assistance programs</li>" +
                "</ul>" +

                "<h2>12. Partnership Obligations</h2>" +
                "<div class='info'>" +
                "<strong>As an OrgaNation partner hospital, you agree to:</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Provide accurate and timely information</li>" +
                "<li>Maintain high standards of patient care</li>" +
                "<li>Participate in organ donation programs</li>" +
                "<li>Collaborate with network hospitals</li>" +
                "<li>Share best practices and protocols</li>" +
                "<li>Contribute to healthcare research and development</li>" +
                "</ul>" +

                "<h2>13. Termination and Suspension</h2>" +
                "<p>OrgaNation reserves the right to terminate or suspend hospital partnerships for:</p>" +
                "<ul>" +
                "<li>Violation of healthcare standards</li>" +
                "<li>Medical malpractice or negligence</li>" +
                "<li>Non-compliance with regulations</li>" +
                "<li>Breach of patient confidentiality</li>" +
                "<li>Fraudulent activities</li>" +
                "<li>Failure to maintain required standards</li>" +
                "</ul>" +

                "<h2>14. Liability and Indemnification</h2>" +
                "<div class='warning'>" +
                "<strong>Hospital assumes responsibility for all medical services provided.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Medical malpractice liability coverage</li>" +
                "<li>Patient safety and care responsibility</li>" +
                "<li>Staff professional liability</li>" +
                "<li>Facility safety and maintenance</li>" +
                "<li>Compliance with legal requirements</li>" +
                "<li>Indemnification for network partners</li>" +
                "</ul>" +

                "<h2>15. Contact and Support</h2>" +
                "<p>For questions about hospital partnership terms:</p>" +
                "<ul>" +
                "<li>Hospital Relations: [Contact Phone]</li>" +
                "<li>Medical Standards: [Contact Email]</li>" +
                "<li>Organ Transplant Coordination: [Contact Phone]</li>" +
                "<li>Emergency Support: [24/7 Hotline]</li>" +
                "<li>Regulatory Compliance: [Contact Email]</li>" +
                "</ul>" +

                "<h2>16. Acknowledgment and Agreement</h2>" +
                "<div class='highlight'>" +
                "<strong>By accepting these terms, you acknowledge that your hospital meets all requirements and agrees to comply with all conditions.</strong>" +
                "</div>" +
                "<p>You understand that this is a legally binding agreement and that false information may result in immediate termination and legal action.</p>" +
                "<p><strong>Effective Date:</strong> [Date of Acceptance]</p>" +
                "<p><strong>Hospital Representative:</strong> [Authorized Signatory]</p>" +
                "<p><strong>Hospital License Number:</strong> [Registration Number]</p>" +
                "<p><strong>Last Updated:</strong> [Date of Last Update]</p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
