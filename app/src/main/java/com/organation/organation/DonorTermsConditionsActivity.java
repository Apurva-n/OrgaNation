package com.organation.organation;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DonorTermsConditionsActivity extends AppCompatActivity {

    private WebView webViewTerms;
    private TextView btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_terms_conditions);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Donor Terms and Conditions");
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
        String termsContent = getTermsAndConditionsContent();
        String formattedContent = "<html><head><style>" +
                "body { font-family: sans-serif; padding: 16px; line-height: 1.6; color: #333; }" +
                "h1 { color: #E91E63; border-bottom: 2px solid #E91E63; padding-bottom: 8px; }" +
                "h2 { color: #E91E63; margin-top: 24px; }" +
                "h3 { color: #666; margin-top: 20px; }" +
                "ul { margin: 12px 0; padding-left: 20px; }" +
                "li { margin: 8px 0; }" +
                ".highlight { background-color: #FFF9C4; padding: 12px; border-radius: 8px; margin: 16px 0; }" +
                ".warning { background-color: #FFEBEE; padding: 12px; border-radius: 8px; margin: 16px 0; }" +
                "</style></head><body>" + termsContent + "</body></html>";

        webViewTerms.loadDataWithBaseURL(null, formattedContent, "text/html", "UTF-8", null);
    }

    private String getTermsAndConditionsContent() {
        return "<h1>Organ Donation Terms and Conditions</h1>" +
                
                "<h2>1. Voluntary Donation Declaration</h2>" +
                "<div class='highlight'>" +
                "<strong>I hereby declare that I am voluntarily agreeing to donate my organs without any coercion, pressure, or financial incentive.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>I am of sound mind and capable of making this decision independently</li>" +
                "<li>I understand that organ donation is a purely voluntary act</li>" +
                "<li>I am not under any influence of alcohol, drugs, or external pressure</li>" +
                "<li>I have not been offered any financial compensation for this donation</li>" +
                "</ul>" +

                "<h2>2. Medical Consent</h2>" +
                "<p>I hereby give my informed consent for:</p>" +
                "<ul>" +
                "<li>Medical evaluation to determine my eligibility for organ donation</li>" +
                "<li>Surgical procedures required for organ retrieval</li>" +
                "<li>Post-operative care and follow-up treatments</li>" +
                "<li>Storage and preservation of donated organs</li>" +
                "</ul>" +

                "<h2>3. Understanding of Risks</h2>" +
                "<div class='warning'>" +
                "<strong>I understand that organ donation carries certain risks including but not limited to:</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Surgical complications and anesthesia risks</li>" +
                "<li>Post-operative pain and recovery period</li>" +
                "<li>Potential long-term health effects</li>" +
                "<li>Risks associated with organ removal procedures</li>" +
                "</ul>" +

                "<h2>4. Right to Withdraw</h2>" +
                "<p>I understand that I have the right to withdraw my consent:</p>" +
                "<ul>" +
                "<li>At any time before the organ retrieval procedure begins</li>" +
"<li>Without any penalty or negative consequences</li>" +
                "<li>By providing written notice to the medical facility</li>" +
                "<li>Verbally to the medical team before the procedure</li>" +
                "</ul>" +

                "<h2>5. Confidentiality</h2>" +
                "<p>I understand that:</p>" +
                "<ul>" +
                "<li>My medical information will be kept confidential</li>" +
                "<li>Only authorized medical personnel will access my records</li>" +
                "<li>My identity will be protected in accordance with privacy laws</li>" +
                "<li>Recipient information will remain confidential</li>" +
                "</ul>" +

                "<h2>6. No Financial Compensation</h2>" +
                "<div class='highlight'>" +
                "<strong>I confirm that I will not receive any financial compensation for my organ donation.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Organ donation is purely altruistic</li>" +
                "<li>No payment will be made for the donated organ</li>" +
                "<li>Medical costs related to donation will be covered separately</li>" +
                "<li>I understand that selling organs is illegal</li>" +
                "</ul>" +

                "<h2>7. Legal Compliance</h2>" +
                "<p>I confirm that:</p>" +
                "<ul>" +
                "<li>I am at least 18 years of age</li>" +
                "<li>I am legally competent to make this decision</li>" +
                "<li>All information provided is true and accurate</li>" +
                "<li>I understand the legal implications of organ donation</li>" +
                "</ul>" +

                "<h2>8. Post-Donation Care</h2>" +
                "<p>I agree to:</p>" +
                "<ul>" +
                "<li>Follow all post-operative medical instructions</li>" +
                "<li>Attend all scheduled follow-up appointments</li>" +
                "<li>Report any complications to medical staff immediately</li>" +
                "<li>Maintain a healthy lifestyle during recovery</li>" +
                "</ul>" +

                "<h2>9. Contact Information</h2>" +
                "<p>For questions or concerns about organ donation:</p>" +
                "<ul>" +
                "<li>Medical Facility: [Hospital Name]</li>" +
                "<li>Organ Donation Coordinator: [Contact Person]</li>" +
                "<li>Emergency Contact: [Phone Number]</li>" +
                "<li>Email: [Email Address]</li>" +
                "</ul>" +

                "<h2>10. Acknowledgment</h2>" +
                "<div class='highlight'>" +
                "<strong>By accepting these terms, I confirm that I have read, understood, and agree to all conditions outlined above.</strong>" +
                "</div>" +
                "<p>I understand that this is a legally binding agreement and that I may seek independent legal counsel if desired.</p>" +
                "<p><strong>Effective Date:</strong> [Date of Acceptance]</p>" +
                "<p><strong>Signature:</strong> Digital Acceptance via Application</p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
