package com.organation.organation;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RecipientTermsConditionsActivity extends AppCompatActivity {

    private WebView webViewTerms;
    private TextView btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_terms_conditions);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recipient Terms and Conditions");
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
        String termsContent = getRecipientTermsAndConditionsContent();
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

    private String getRecipientTermsAndConditionsContent() {
        return "<h1>Organ Recipient Terms and Conditions</h1>" +
                
                "<h2>1. Medical Eligibility and Verification</h2>" +
                "<div class='highlight'>" +
                "<strong>By registering as an organ recipient, you confirm that you have been medically evaluated and certified as requiring organ transplantation.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>You must provide accurate and complete medical information</li>" +
                "<li>All medical evaluations must be current and valid</li>" +
                "<li>You must disclose all relevant medical conditions</li>" +
                "<li>Regular medical follow-ups are required</li>" +
                "<li>Medical eligibility must be verified by certified physicians</li>" +
                "</ul>" +

                "<h2>2. Medical Information and Privacy</h2>" +
                "<p>Your medical information is sensitive and requires special protection:</p>" +
                "<ul>" +
                "<li>All medical data will be kept confidential and secure</li>" +
                "<li>Medical information will only be shared with authorized healthcare providers</li>" +
                "<li>You consent to medical information sharing for transplant purposes</li>" +
                "<li>Compliance with healthcare privacy laws and regulations</li>" +
                "<li>Regular updates of medical status are required</li>" +
                "<li>Medical records will be maintained securely</li>" +
                "</ul>" +

                "<h2>3. Transplant Process Participation</h2>" +
                "<div class='warning'>" +
                "<strong>Participation in the organ transplant process requires your active cooperation and compliance.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>You must be available for transplant procedures when organs become available</li>" +
                "<li>Pre-transplant medical evaluations are mandatory</li>" +
                "<li>Post-transplant follow-up care is required</li>" +
                "<li>Compliance with medication regimens is essential</li>" +
                "<li>Lifestyle modifications may be necessary</li>" +
                "<li>Regular medical monitoring is required</li>" +
                "</ul>" +

                "<h2>4. Financial Responsibilities</h2>" +
                "<p>Organ transplantation involves significant financial considerations:</p>" +
                "<ul>" +
                "<li>You are responsible for all medical costs not covered by insurance</li>" +
                "<li>Pre-transplant evaluation costs must be covered</li>" +
                "<li>Post-transplant medication costs are ongoing</li>" +
                "<li>Follow-up care costs must be budgeted</li>" +
                "<li>Emergency care costs may arise</li>" +
                "<li>Financial counseling is recommended</li>" +
                "</ul>" +

                "<h2>5. Organ Matching and Allocation</h2>" +
                "<div class='info'>" +
                "<strong>The organ matching process follows established medical and ethical guidelines.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Organ matching is based on medical compatibility</li>" +
                "<li>Allocation follows established priority systems</li>" +
                "<li>Urgency levels are medically determined</li>" +
                "<li>Geographic location may affect availability</li>" +
                "<li>Blood type and tissue matching are critical factors</li>" +
                "<li>Waiting list position may change based on medical status</li>" +
                "</ul>" +

                "<h2>6. Pre-Transplant Requirements</h2>" +
                "<p>Before transplantation, you must complete several requirements:</p>" +
                "<ul>" +
                "<li>Comprehensive medical evaluation</li>" +
                "<li>Psychological assessment and counseling</li>" +
                "<li>Financial planning and insurance verification</li>" +
                "<li>Social support system evaluation</li>" +
                "<li>Lifestyle modification programs</li>" +
                "<li>Educational sessions about transplant process</li>" +
                "</ul>" +

                "<h2>7. Post-Transplant Care</h2>" +
                "<p>After transplantation, lifelong care is required:</p>" +
                "<ul>" +
                "<li>Strict medication adherence is mandatory</li>" +
                "<li>Regular medical follow-ups are required</li>" +
                "<li>Lifestyle modifications must be maintained</li>" +
                "<li>Dietary restrictions must be followed</li>" +
                "<li>Physical activity limitations may apply</li>" +
                "<li>Regular monitoring for organ rejection</li>" +
                "</ul>" +

                "<h2>8. Communication and Updates</h2>" +
                "<p>Maintaining communication is essential for successful transplantation:</p>" +
                "<ul>" +
                "<li>You must keep contact information current</li>" +
                "<li>Regular health status updates are required</li>" +
                "<li>Immediate notification of health changes is necessary</li>" +
                "<li>Travel restrictions may apply during waiting period</li>" +
                "<li>24/7 contact availability may be required</li>" +
                "<li>Emergency contact information must be provided</li>" +
                "</ul>" +

                "<h2>9. Legal and Ethical Considerations</h2>" +
                "<div class='warning'>" +
                "<strong>Organ transplantation involves important legal and ethical considerations.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>You must comply with all applicable laws and regulations</li>" +
                "<li>Organ donation and transplant laws must be followed</li>" +
                "<li>Ethical guidelines for organ allocation must be respected</li>" +
                "<li>Informed consent for all procedures is required</li>" +
                "<li>Medical decision-making capacity must be maintained</li>" +
                "<li>Advance directives should be considered</li>" +
                "</ul>" +

                "<h2>10. Support System Requirements</h2>" +
                "<p>A strong support system is essential for transplant success:</p>" +
                "<ul>" +
                "<li>Family or caregiver support is required</li>" +
                "<li>Transportation arrangements for medical appointments</li>" +
                "<li>Emotional support during recovery period</li>" +
                "<li>Assistance with daily activities during recovery</li>" +
                "<li>Emergency support arrangements</li>" +
                "<li>Financial support planning</li>" +
                "</ul>" +

                "<h2>11. Lifestyle Modifications</h2>" +
                "<p>Significant lifestyle changes may be required:</p>" +
                "<ul>" +
                "<li>Dietary restrictions and modifications</li>" +
                "<li>Alcohol and substance use restrictions</li>" +
                "<li>Physical activity limitations and requirements</li>" +
                "<li>Travel restrictions during certain periods</li>" +
                "<li>Occupational modifications may be necessary</li>" +
                "<li>Social activity adjustments</li>" +
                "</ul>" +

                "<h2>12. Emergency and Urgent Care</h2>" +
                "<p>Emergency preparedness is critical for transplant recipients:</p>" +
                "<ul>" +
                "<li>24/7 emergency contact availability is required</li>" +
                "<li>Emergency medical information must be carried</li>" +
                "<li>Local emergency medical services must be identified</li>" +
                "<li>Emergency medications must be available</li>" +
                "<li>Emergency transportation arrangements</li>" +
                "<li>Hospital emergency department information</li>" +
                "</ul>" +

                "<h2>13. Data Sharing and Research</h2>" +
                "<div class='info'>" +
                "<strong>Your participation may contribute to medical research and improved transplant outcomes.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Anonymized medical data may be used for research</li>" +
                "<li>Participation in clinical studies may be requested</li>" +
                "<li>Transplant registry participation is required</li>" +
                "<li>Outcome tracking for quality improvement</li>" +
                "<li>Educational contributions to transplant community</li>" +
                "<li>Research participation is voluntary but encouraged</li>" +
                "</ul>" +

                "<h2>14. Termination and Withdrawal</h2>" +
                "<p>You may withdraw from the transplant program under certain conditions:</p>" +
                "<ul>" +
                "<li>Written notice to transplant coordinator</li>" +
                "<li>Medical evaluation for withdrawal safety</li>" +
                "<li>Alternative care planning</li>" +
                "<li>Follow-up care arrangements</li>" +
                "<li>Medical record transfer procedures</li>" +
                "<li>Support system transition planning</li>" +
                "</ul>" +

                "<h2>15. Contact and Support</h2>" +
                "<p>For questions about recipient terms and conditions:</p>" +
                "<ul>" +
                "<li>Transplant Coordinator: [Contact Phone]</li>" +
                "<li>Medical Support: [Contact Email]</li>" +
                "<li>Financial Counseling: [Contact Phone]</li>" +
                "<li>Social Work Support: [Contact Phone]</li>" +
                "<li>Emergency Support: [24/7 Hotline]</li>" +
                "<li>Patient Advocacy: [Contact Email]</li>" +
                "</ul>" +

                "<h2>16. Acknowledgment and Agreement</h2>" +
                "<div class='highlight'>" +
                "<strong>By accepting these terms, you acknowledge that you understand and agree to all conditions for organ recipients.</strong>" +
                "</div>" +
                "<p>You understand that organ transplantation is a complex medical procedure requiring significant commitment and lifestyle changes. You agree to comply with all medical requirements and maintain open communication with your healthcare team.</p>" +
                "<p><strong>Effective Date:</strong> [Date of Acceptance]</p>" +
                "<p><strong>Recipient ID:</strong> [Registration Number]</p>" +
                "<p><strong>Transplant Center:</strong> [Hospital Name]</p>" +
                "<p><strong>Last Updated:</strong> [Date of Last Update]</p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
