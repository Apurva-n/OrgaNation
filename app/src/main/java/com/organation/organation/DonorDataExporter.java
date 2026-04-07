package com.organation.organation;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DonorDataExporter {
    
    private static final String TAG = "DonorDataExporter";
    private FirebaseFirestore db;
    private Context context;
    
    public DonorDataExporter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    public void exportAllDonorsToCSV() {
        // Create CSV file in Downloads folder
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "donor_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
        File csvFile = new File(downloadsDir, fileName);
        
        try {
            FileWriter writer = new FileWriter(csvFile);
            
            // Write CSV header
            writer.append("Name,Age,Weight,Height,BloodGroup,Gender,Phone,Email,City,State\n");
            
            // Fetch all donors from Firestore
            db.collection("donors")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String csvRow = convertDonorToCSV(document);
                                if (csvRow != null) {
                                    writer.append(csvRow);
                                }
                            }
                            writer.flush();
                            writer.close();
                            
                            Toast.makeText(context, "Donor data exported to: " + fileName, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Export completed: " + csvFile.getAbsolutePath());
                        } catch (IOException e) {
                            Log.e(TAG, "Error writing CSV", e);
                            Toast.makeText(context, "Error exporting data", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching donors", e);
                        Toast.makeText(context, "Error fetching donor data", Toast.LENGTH_SHORT).show();
                    });
                    
        } catch (IOException e) {
            Log.e(TAG, "Error creating file", e);
            Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String convertDonorToCSV(QueryDocumentSnapshot document) {
        try {
            // Extract donor data using FieldPath for special characters
            String name = document.getString("01]Full_name");
            String age = document.getString("04]Age");
            String weight = document.getString("08]Weight");
            String height = document.getString("07]Height");
            String bloodGroup = document.getString("06]Blood_group");
            String gender = document.getString("05]Gender");
            String phone = document.getString("09]Phone");
            String email = document.getString("10]Email");
            String city = document.getString("12]City");
            String state = document.getString("11]State");
            
            // Validate required fields for ML model
            if (name == null || age == null || weight == null || height == null || 
                bloodGroup == null || gender == null) {
                return null; // Skip incomplete records
            }
            
            // Clean data and format for CSV
            return String.format(Locale.getDefault(), "\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCSV(name), age, weight, height, bloodGroup, gender,
                    phone != null ? phone : "",
                    email != null ? email : "",
                    city != null ? city : "",
                    state != null ? state : "");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error converting donor to CSV", e);
            return null;
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\""); // Escape quotes in CSV
    }
}
