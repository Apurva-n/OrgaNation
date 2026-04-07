package com.organation.organation;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminTermsConditionsActivity extends AppCompatActivity {

    private WebView webViewTerms;
    private TextView btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_terms_conditions);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Terms and Conditions");
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
        String termsContent = getAdminTermsAndConditionsContent();
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

    private String getAdminTermsAndConditionsContent() {
        return "<h1>Administrator Terms and Conditions</h1>" +
                
                "<h2>1. Administrative Authority and Responsibility</h2>" +
                "<div class='highlight'>" +
                "<strong>By registering as an administrator, you accept significant responsibility for managing the OrgaNation healthcare platform and all user data.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>You must have proper authorization to administer this healthcare platform</li>" +
                "<li>All administrative actions must be performed with due care and diligence</li>" +
                "<li>You are responsible for maintaining system integrity and security</li>" +
                "<li>Administrative decisions must prioritize patient safety and data protection</li>" +
                "<li>Regular training and certification updates are required</li>" +
                "</ul>" +

                "<h2>2. Data Privacy and Security Compliance</h2>" +
                "<p>As an administrator, you have access to sensitive healthcare data and must maintain strict confidentiality:</p>" +
                "<ul>" +
                "<li>All patient and medical data must be kept strictly confidential</li>" +
                "<li>Access to sensitive data is limited to administrative duties only</li>" +
                "<li>Compliance with healthcare data protection laws (HIPAA, GDPR, etc.)</li>" +
                "<li>Regular security audits and access log reviews are required</li>" +
                "<li>Data breaches must be reported immediately according to protocol</li>" +
                "<li>Secure password practices and authentication methods must be maintained</li>" +
                "</ul>" +

                "<h2>3. System Management and Maintenance</h2>" +
                "<div class='warning'>" +
                "<strong>You are responsible for the proper functioning and maintenance of the entire healthcare platform.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Regular system backups and disaster recovery procedures</li>" +
                "<li>Software updates and security patches must be applied promptly</li>" +
                "<li>User account management and access control</li>" +
                "<li>System performance monitoring and optimization</li>" +
                "<li>Database maintenance and integrity checks</li>" +
                "<li>Integration with healthcare systems and third-party services</li>" +
                "</ul>" +

                "<h2>4. User Management and Support</h2>" +
                "<p>Administrators must provide proper support and management for all platform users:</p>" +
                "<ul>" +
                "<li>User registration and account verification</li>" +
                "<li>Password reset and account recovery assistance</li>" +
                "<li>User access level management and permissions</li>" +
                "<li>Technical support and troubleshooting</li>" +
                "<li>User training and documentation maintenance</li>" +
                "<li>Complaint handling and dispute resolution</li>" +
                "</ul>" +

                "<h2>5. Healthcare Compliance and Regulations</h2>" +
                "<div class='info'>" +
                "<strong>The healthcare platform must comply with all applicable medical and data protection regulations.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Healthcare data storage and transmission standards</li>" +
                "<li>Medical device integration compliance</li>" +
                "<li>Clinical data management regulations</li>" +
                "<li>Patient privacy and confidentiality laws</li>" +
                "<li>Healthcare provider accreditation requirements</li>" +
                "<li>Organ transplant regulations and guidelines</li>" +
                "</ul>" +

                "<h2>6. Quality Assurance and Auditing</h2>" +
                "<p>Regular quality assurance measures must be implemented and maintained:</p>" +
                "<ul>" +
                "<li>System performance and reliability monitoring</li>" +
                "<li>User satisfaction surveys and feedback collection</li>" +
                "<li>Security audits and vulnerability assessments</li>" +
                "<li>Data integrity and accuracy verification</li>" +
                "<li>Compliance audits and regulatory reporting</li>" +
                "<li>Continuous improvement initiatives</li>" +
                "</ul>" +

                "<h2>7. Emergency Response and Incident Management</h2>" +
                "<p>Administrators must be prepared for emergency situations and system incidents:</p>" +
                "<ul>" +
                "<li>24/7 availability for critical system issues</li>" +
                "<li>Emergency response protocols and procedures</li>" +
                "<li>System downtime communication and user notifications</li>" +
                "<li>Data recovery and business continuity planning</li>" +
                "<li>Critical incident reporting and analysis</li>" +
                "<li>Coordination with healthcare providers during emergencies</li>" +
                "</ul>" +

                "<h2>8. Reporting and Analytics</h2>" +
                "<p>Administrators must provide regular reports and analytics to stakeholders:</p>" +
                "<ul>" +
                "<li>System usage and performance metrics</li>" +
                "<li>User registration and activity statistics</li>" +
                "<li>Healthcare outcome tracking and reporting</li>" +
                "<li>Security incident reports and trend analysis</li>" +
                "<li>Compliance status and audit results</li>" +
                "<li>Financial and operational reporting</li>" +
                "</ul>" +

                "<h2>9. Integration and Interoperability</h2>" +
                "<div class='warning'>" +
                "<strong>The platform must integrate seamlessly with other healthcare systems and services.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Electronic Health Record (EHR) system integration</li>" +
                "<li>Laboratory and diagnostic system connectivity</li>" +
                "<li>Pharmacy and medication management integration</li>" +
                "<li>Billing and insurance system interfaces</li>" +
                "<li>Government healthcare database connections</li>" +
                "<li>Third-party API management and security</li>" +
                "</ul>" +

                "<h2>10. Training and Professional Development</h2>" +
                "<p>Administrators must maintain current knowledge of healthcare technology and regulations:</p>" +
                "<ul>" +
                "<li>Regular training on healthcare technology updates</li>" +
                "<li>Data privacy and security certification maintenance</li>" +
                "<li>Healthcare industry regulation updates</li>" +
                "<li>System administration best practices</li>" +
                "<li>Emergency response and disaster recovery training</li>" +
                "<li>Professional development and networking</li>" +
                "</ul>" +

                "<h2>11. Ethical Considerations and Professional Conduct</h2>" +
                "<ul>" +
                "<li>All administrative decisions must prioritize patient welfare</li>" +
                "<li>Conflicts of interest must be disclosed and managed</li>" +
                "<li>Professional ethics must be maintained in all interactions</li>" +
                "<li>Cultural sensitivity and diversity must be respected</li>" +
                "<li>Transparency in decision-making processes</li>" +
                "<li>Accountability for administrative actions and decisions</li>" +
                "</ul>" +

                "<h2>12. Third-Party Vendor Management</h2>" +
                "<p>Administrators must oversee all third-party service providers and vendors:</p>" +
                "<ul>" +
                "<li>Vendor selection and due diligence processes</li>" +
                "<li>Service level agreement monitoring and enforcement</li>" +
                "<li>Security assessment of third-party systems</li>" +
                "<li>Contract management and compliance verification</li>" +
                "<li>Performance monitoring and quality assurance</li>" +
                "<li>Vendor relationship management and communication</li>" +
                "</ul>" +

                "<h2>13. Change Management and System Updates</h2>" +
                "<div class='info'>" +
                "<strong>System changes must be managed carefully to minimize disruption to healthcare services.</strong>" +
                "</div>" +
                "<ul>" +
                "<li>Change control procedures and documentation</li>" +
                "<li>User communication and training for system updates</li>" +
                "<li>Testing and validation of system changes</li>" +
                "<li>Rollback procedures for failed updates</li>" +
                "<li>Impact assessment and risk mitigation</li>" +
                "<li>Stakeholder approval and sign-off processes</li>" +
                "</ul>" +

                "<h2>14. Legal and Regulatory Compliance</h2>" +
                "<p>Administrators must ensure compliance with all applicable laws and regulations:</p>" +
                "<ul>" +
                "<li>Healthcare data protection and privacy laws</li>" +
                "<li>Medical device and software regulations</li>" +
                "<li>Healthcare provider licensing requirements</li>" +
                "<li>Organ donation and transplant regulations</li>" +
                "<li>International data transfer and privacy laws</li>" +
                "<li>Industry-specific compliance frameworks</li>" +
                "</ul>" +

                "<h2>15. Termination and Succession Planning</h2>" +
                "<p>Procedures for administrator transition and knowledge transfer:</p>" +
                "<ul>" +
                "<li>Orderly transition of administrative responsibilities</li>" +
                "<li>Knowledge transfer and documentation handover</li>" +
                "<li>System access revocation and account closure</li>" +
                "<li>Confidentiality agreements post-termination</li>" +
                "<li>Succession planning and backup administrator training</li>" +
                "<li>Exit interviews and process improvement feedback</li>" +
                "</ul>" +

                "<h2>16. Contact and Support Resources</h2>" +
                "<p>For administrative support and guidance:</p>" +
                "<ul>" +
                "<li>Technical Support: [24/7 Hotline]</li>" +
                "<li>Security Team: [Contact Email]</li>" +
                "<li>Compliance Officer: [Contact Phone]</li>" +
                "<li>Legal Counsel: [Contact Email]</li>" +
                "<li>System Administrator: [Contact Phone]</li>" +
                "<li>Emergency Response: [24/7 Hotline]</li>" +
                "</ul>" +

                "<h2>17. Acknowledgment and Agreement</h2>" +
                "<div class='highlight'>" +
                "<strong>By accepting these terms, you acknowledge the significant responsibility of administering a healthcare platform.</strong>" +
                "</div>" +
                "<p>You understand that your role directly impacts patient care, data security, and healthcare delivery. You commit to maintaining the highest standards of professionalism, security, and ethical conduct in all administrative activities.</p>" +
                "<p><strong>Effective Date:</strong> [Date of Acceptance]</p>" +
                "<p><strong>Administrator ID:</strong> [Registration Number]</p>" +
                "<p><strong>Access Level:</strong> [Permission Level]</p>" +
                "<p><strong>Last Updated:</strong> [Date of Last Update]</p>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
