package com.organation.organation;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MLDonorMatchingService {
    
    private static final String TAG = "MLDonorMatching";
    private FirebaseFirestore db;
    
    public MLDonorMatchingService(Context context) {
        db = FirebaseFirestore.getInstance();
    }
    
    public interface DonorMatchingCallback {
        void onMatchingCompleted(List<DonorMatchResult> topDonors);
        void onMatchingFailed(String error);
    }
    
    public void findTopCompatibleDonors(RecipientModel recipient, DonorMatchingCallback callback) {
        // Fetch all donors from database
        db.collection("donors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonorMatchResult> allMatches = new ArrayList<>();
                    List<DonorModel> donors = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            DonorModel donor = convertDocumentToDonor(document);
                            if (donor != null && isOrganCompatible(recipient, donor)) {
                                donors.add(donor);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing donor", e);
                        }
                    }
                    
                    for (DonorModel donor : donors) {
                        double compatibilityScore = calculateCompatibilityScore(recipient, donor);
                        if (compatibilityScore > 0.1) { // Lower threshold to include more donors
                            allMatches.add(new DonorMatchResult(donor, compatibilityScore));
                        } else {
                            Log.d(TAG, "Donor filtered out - Score too low: " + compatibilityScore);
                        }
                    }
                    
                    // Sort by compatibility score (highest first)
                    Collections.sort(allMatches, new Comparator<DonorMatchResult>() {
                        @Override
                        public int compare(DonorMatchResult o1, DonorMatchResult o2) {
                            return Double.compare(o2.compatibilityScore, o1.compatibilityScore);
                        }
                    });
                    
                    // Return top 10
                    int maxResults = Math.min(10, allMatches.size());
                    List<DonorMatchResult> topDonors = allMatches.subList(0, maxResults);
                    
                    callback.onMatchingCompleted(topDonors);
                })
                .addOnFailureListener(e -> {
                    callback.onMatchingFailed("Error fetching donors: " + e.getMessage());
                });
    }
    
    private double calculateCompatibilityScore(RecipientModel recipient, DonorModel donor) {
        double score = 0.0;
        int factors = 0;
        
        try {
            // Blood Group Compatibility (40% weight)
            double bloodScore = calculateBloodGroupCompatibility(recipient.getBloodGroup(), donor.getBloodGroup());
            score += bloodScore * 0.4;
            factors++;
            
            // Age Compatibility (25% weight)
            double ageScore = calculateAgeCompatibility(recipient.getAge(), donor.getAge());
            score += ageScore * 0.25;
            factors++;
            
            // Gender Compatibility (10% weight)
            double genderScore = calculateGenderCompatibility(recipient.getGender(), donor.getGender());
            score += genderScore * 0.1;
            factors++;
            
            // BMI/Weight Compatibility (20% weight)
            double weightScore = calculateWeightCompatibility(recipient.getWeight(), donor.getWeight());
            score += weightScore * 0.2;
            factors++;
            
            // Height Compatibility (5% weight)
            double heightScore = calculateHeightCompatibility(recipient.getHeight(), donor.getHeight());
            score += heightScore * 0.05;
            factors++;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating compatibility", e);
            return 0.0;
        }
        
        return factors > 0 ? score : 0.0;
    }
    
    private double calculateBloodGroupCompatibility(String recipientBlood, String donorBlood) {
        Log.d(TAG, "Blood group compatibility - Recipient: " + recipientBlood + ", Donor: " + donorBlood);
        
        if (recipientBlood == null || donorBlood == null) return 0.0;
        
        // O- is universal donor (can donate to ALL types)
        if (donorBlood.equals("O-")) {
            Log.d(TAG, "Blood compatibility: O- universal donor - COMPATIBLE");
            return 1.0;
        }
        
        // O+ is universal donor for positive types
        if (donorBlood.equals("O+")) {
            if (recipientBlood.equals("A+") || recipientBlood.equals("B+") || 
                recipientBlood.equals("AB+") || recipientBlood.equals("O+")) {
                Log.d(TAG, "Blood compatibility: O+ to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: O+ to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // A+ can donate to A+ and AB+
        if (donorBlood.equals("A+")) {
            if (recipientBlood.equals("A+") || recipientBlood.equals("AB+")) {
                Log.d(TAG, "Blood compatibility: A+ to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: A+ to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // A- can donate to A+, A-, AB+, AB-
        if (donorBlood.equals("A-")) {
            if (recipientBlood.equals("A+") || recipientBlood.equals("A-") || 
                recipientBlood.equals("AB+") || recipientBlood.equals("AB-")) {
                Log.d(TAG, "Blood compatibility: A- to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: A- to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // B+ can donate to B+ and AB+
        if (donorBlood.equals("B+")) {
            if (recipientBlood.equals("B+") || recipientBlood.equals("AB+")) {
                Log.d(TAG, "Blood compatibility: B+ to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: B+ to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // B- can donate to B+, B-, AB+, AB-
        if (donorBlood.equals("B-")) {
            if (recipientBlood.equals("B+") || recipientBlood.equals("B-") || 
                recipientBlood.equals("AB+") || recipientBlood.equals("AB-")) {
                Log.d(TAG, "Blood compatibility: B- to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: B- to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // AB+ can donate to AB+ only
        if (donorBlood.equals("AB+")) {
            if (recipientBlood.equals("AB+")) {
                Log.d(TAG, "Blood compatibility: AB+ to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: AB+ to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        // AB- can donate to AB+ and AB-
        if (donorBlood.equals("AB-")) {
            if (recipientBlood.equals("AB+") || recipientBlood.equals("AB-")) {
                Log.d(TAG, "Blood compatibility: AB- to " + recipientBlood + " - COMPATIBLE");
                return 1.0;
            }
            Log.d(TAG, "Blood compatibility: AB- to " + recipientBlood + " - INCOMPATIBLE");
            return 0.0;
        }
        
        Log.d(TAG, "Blood compatibility: Unknown blood group - INCOMPATIBLE");
        return 0.0; // Incompatible
    }
    
    private double calculateAgeCompatibility(String recipientAgeStr, String donorAgeStr) {
        try {
            if (recipientAgeStr == null || donorAgeStr == null) {
                Log.d(TAG, "Age compatibility: Missing data - recipient: " + 
                          (recipientAgeStr != null ? recipientAgeStr : "NULL") + 
                          ", donor: " + (donorAgeStr != null ? donorAgeStr : "NULL"));
                return 0.5; // Neutral score for missing data
            }
            
            int recipientAge = Integer.parseInt(recipientAgeStr.trim());
            int donorAge = Integer.parseInt(donorAgeStr.trim());
            
            // Ideal age difference is within 10 years
            int ageDiff = Math.abs(recipientAge - donorAge);
            if (ageDiff <= 5) return 1.0;
            if (ageDiff <= 10) return 0.8;
            if (ageDiff <= 15) return 0.6;
            if (ageDiff <= 20) return 0.4;
            if (ageDiff <= 25) return 0.2;
            
            return 0.0;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing age values - recipient: " + recipientAgeStr + 
                      ", donor: " + donorAgeStr, e);
            return 0.5; // Neutral score for parsing errors
        }
    }
    
    private double calculateWeightCompatibility(String recipientWeightStr, String donorWeightStr) {
        try {
            // Add null checks
            if (recipientWeightStr == null || donorWeightStr == null) {
                Log.d(TAG, "Weight compatibility: Missing data - recipient: " + 
                          (recipientWeightStr != null ? recipientWeightStr : "NULL") + 
                          ", donor: " + (donorWeightStr != null ? donorWeightStr : "NULL"));
                return 0.5; // Neutral score for missing data
            }
            
            double recipientWeight = Double.parseDouble(recipientWeightStr.trim());
            double donorWeight = Double.parseDouble(donorWeightStr.trim());
            
            // Ideal weight difference is within 10kg
            double weightDiff = Math.abs(recipientWeight - donorWeight);
            if (weightDiff <= 5) return 1.0;
            if (weightDiff <= 10) return 0.8;
            if (weightDiff <= 15) return 0.6;
            if (weightDiff <= 20) return 0.4;
            
            return 0.2;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing weight values - recipient: " + recipientWeightStr + 
                      ", donor: " + donorWeightStr, e);
            return 0.5; // Neutral score for parsing errors
        }
    }
    
    private double calculateHeightCompatibility(String recipientHeightStr, String donorHeightStr) {
        try {
            // Add null checks
            if (recipientHeightStr == null || donorHeightStr == null) {
                Log.d(TAG, "Height compatibility: Missing data - recipient: " + 
                          (recipientHeightStr != null ? recipientHeightStr : "NULL") + 
                          ", donor: " + (donorHeightStr != null ? donorHeightStr : "NULL"));
                return 0.5; // Neutral score for missing data
            }
            
            double recipientHeight = Double.parseDouble(recipientHeightStr.trim());
            double donorHeight = Double.parseDouble(donorHeightStr.trim());
            
            // Height should be within 15% difference
            double heightDiff = Math.abs(recipientHeight - donorHeight) / recipientHeight;
            if (heightDiff <= 0.05) return 1.0;
            if (heightDiff <= 0.1) return 0.8;
            if (heightDiff <= 0.15) return 0.6;
            if (heightDiff <= 0.2) return 0.4;
            
            return 0.2;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing height values - recipient: " + recipientHeightStr + 
                      ", donor: " + donorHeightStr, e);
            return 0.5; // Neutral score for parsing errors
        }
    }
    
    private double calculateGenderCompatibility(String recipientGender, String donorGender) {
        if (recipientGender == null || donorGender == null) return 0.0;
        return recipientGender.equalsIgnoreCase(donorGender) ? 1.0 : 0.8;
    }
    
    private boolean isOrganCompatible(RecipientModel recipient, DonorModel donor) {
        if (recipient.getOrgansNeeded() == null || donor.getOrgansToNDonate() == null) {
            Log.e(TAG, "Organ compatibility: Missing data - recipient: " + 
                  (recipient.getOrgansNeeded() != null ? recipient.getOrgansNeeded() : "NULL") + 
                  ", donor: " + (donor.getOrgansToNDonate() != null ? donor.getOrgansToNDonate() : "NULL"));
            return false;
        }
        
        String recipientOrgan = recipient.getOrgansNeeded().trim();
        String donorOrgans = donor.getOrgansToNDonate().trim();
        
        // Handle common variations and case issues
        recipientOrgan = normalizeOrganName(recipientOrgan);
        
        Log.d(TAG, "Organ compatibility check - Recipient needs: " + recipientOrgan + 
                  ", Donor offers: " + donorOrgans);
        
        // Check if donor has no organs available
        if (donorOrgans.equals("N/A") || donorOrgans.isEmpty()) {
            Log.d(TAG, "Organ compatibility: Donor has no available organs");
            return false;
        }
        
        // Handle comma-separated organs like "liver,pancreas,kidney"
        String[] donorOrganList = donorOrgans.split(",");
        
        for (String donorOrgan : donorOrganList) {
            donorOrgan = normalizeOrganName(donorOrgan.trim());
            if (donorOrgan.equals(recipientOrgan)) {
                Log.d(TAG, "Organ compatibility: MATCH FOUND - " + donorOrgan);
                
                // CRITICAL: Check if this organ is still available
                if (isOrganStillAvailable(donor, recipientOrgan)) {
                    Log.d(TAG, "Organ availability: " + donorOrgan + " is still available for donor " + donor.getFullName());
                    return true; // Found matching available organ
                } else {
                    Log.d(TAG, "Organ availability: " + donorOrgan + " is already used for donor " + donor.getFullName());
                    return false; // Organ already used
                }
            }
        }
        
        Log.d(TAG, "Organ compatibility: NO MATCH FOUND");
        return false; // No matching organ found
    }
    
    /**
     * Check if a specific organ is still available for donation from a donor
     * This prevents selecting organs that have already been used in previous transplants
     * NOTE: This is a simplified check to avoid main thread issues
     */
    private boolean isOrganStillAvailable(DonorModel donor, String organNeeded) {
        try {
            Log.d("ML_MATCHING", "=== CHECKING ORGAN AVAILABILITY ===");
            Log.d("ML_MATCHING", "Donor: " + donor.getFullName());
            Log.d("ML_MATCHING", "Organ Needed: " + organNeeded);
            
            // Normalize the organ name for comparison
            organNeeded = normalizeOrganName(organNeeded);
            
            // Get donor's current organ list - this should now read from updated field
            String donorOrgans = donor.getOrgansToNDonate();
            Log.d("ML_MATCHING", "Donor's current organs: " + donorOrgans);
            
            if (donorOrgans == null || donorOrgans.trim().isEmpty() || donorOrgans.equals("N/A")) {
                Log.d("ML_MATCHING", "Donor has no organs available - returning false");
                return false;
            }
            
            // Check if the organ is in the donor's current list
            String[] donorOrganList = donorOrgans.split(",");
            for (String donorOrgan : donorOrganList) {
                donorOrgan = normalizeOrganName(donorOrgan.trim());
                Log.d("ML_MATCHING", "Checking donor organ: " + donorOrgan + " vs needed: " + organNeeded);
                if (donorOrgan.equals(organNeeded)) {
                    Log.d("ML_MATCHING", "✅ ORGAN MATCH: " + organNeeded + " is available for donor " + donor.getFullName());
                    return true; // Organ found in current list
                }
            }
            
            Log.d("ML_MATCHING", "❌ ORGAN NOT FOUND: " + organNeeded + " is NOT available for donor " + donor.getFullName() + " (not in current list: " + donorOrgans + ")");
            return false; // Organ not found in current list
            
        } catch (Exception e) {
            Log.e("ML_MATCHING", "❌ ERROR checking organ availability for donor: " + donor.getFullName() + ", organ: " + organNeeded, e);
            Log.e("ML_MATCHING", "❌ Error details: " + e.getMessage());
            // If we can't check, assume organ is not available to be safe
            return false;
        }
    }
    
    // Helper method to safely get string from document
    private String getSafeStringFromDocument(DocumentSnapshot doc, String fieldName) {
        try {
            return doc.getString(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String normalizeOrganName(String organ) {
        if (organ == null) return "";
        
        switch (organ.toLowerCase()) {
            case "kidney":
                return "kidney";
            case "liver":
                return "liver";
            case "heart":
                return "heart";
            case "pancreas":
                return "pancreas";
            case "skin":
                return "skin";
            case "eyes":
            case "eye":
                return "eyes";
            case "intestine":
            case "intestines":
                return "intestine";
            default:
                return organ.toLowerCase();
        }
    }
    
    private DonorModel convertDocumentToDonor(QueryDocumentSnapshot document) {
        try {
            DonorModel donor = new DonorModel();
            // Use getData() to access all fields and avoid special character issues
            Map<String, Object> data = document.getData();
            if (data == null) {
                Log.e(TAG, "Document data is null");
                return null;
            }
            
            // Log all available fields for debugging
            Log.d(TAG, "Available fields in donor document: " + data.keySet().toString());
            
            // Get fields using the map instead of direct access
            donor.setFullName(getStringFromData(data, "01]Full_name"));
            donor.setAge(getStringFromData(data, "04]Age"));
            donor.setWeight(getStringFromData(data, "08]Weight"));
            donor.setHeight(getStringFromData(data, "07]Height"));
            donor.setBloodGroup(getStringFromData(data, "06]Blood_group"));
            donor.setGender(getStringFromData(data, "05]Gender"));
            donor.setPhone(getStringFromData(data, "09]Phone"));
            donor.setEmail(getStringFromData(data, "10]Email"));
            donor.setCity(getStringFromData(data, "12]City"));
            donor.setState(getStringFromData(data, "11]State"));
            donor.setStreet(getStringFromData(data, "13]Street"));
            donor.setLandmark(getStringFromData(data, "14]Landmark"));
            donor.setOrgansToNDonate(getStringFromData(data, "16]Organs_to_donate"));
            donor.setAadhaarNo(getStringFromData(data, "02]Aadhaar_no"));
            
            // Log the extracted data for debugging
            Log.d(TAG, "Extracted donor - Name: " + donor.getFullName() + 
                      ", Blood: " + donor.getBloodGroup() + 
                      ", Organs: " + donor.getOrgansToNDonate());
            
            return donor;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to donor", e);
            return null;
        }
    }
    
    private String getStringFromData(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return value != null ? value.toString() : null;
    }
    
    public static class DonorMatchResult {
        public DonorModel donor;
        public double compatibilityScore;
        
        public DonorMatchResult(DonorModel donor, double compatibilityScore) {
            this.donor = donor;
            this.compatibilityScore = compatibilityScore;
        }
    }
}
