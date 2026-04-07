package com.organation.organation;

import java.util.Map;

public class HospitalModel {
    // Basic Information
    private String hospitalName;
    private String authorityName;
    private String contactNumber;
    private String officialEmail;
    private String street;
    private String city;
    private String state;
    private String landmark;
    private String govRegNumber;
    private String authorityContact;
    private String authorityEmail;
    private String hospitalType;
    
    // Additional Hospital Details
    private String hospitalId;
    private String estYear;
    private String websiteUrl;
    private String pincode;
    private String latitude;
    private String longitude;
    
    // Facilities
    private Map<String, Boolean> facilities;
    
    // Constructor
    public HospitalModel() {
        facilities = new java.util.HashMap<>();
    }

    // Full constructor for convenience
    public HospitalModel(String hospitalName, String authorityName, String contactNumber,
                        String officialEmail, String hospitalType) {
        this.hospitalName = hospitalName;
        this.authorityName = authorityName;
        this.contactNumber = contactNumber;
        this.officialEmail = officialEmail;
        this.hospitalType = hospitalType;
        this.facilities = new java.util.HashMap<>();
    }

    // Getters and Setters for Basic Information
    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getGovRegNumber() {
        return govRegNumber;
    }

    public void setGovRegNumber(String govRegNumber) {
        this.govRegNumber = govRegNumber;
    }

    public String getAuthorityContact() {
        return authorityContact;
    }

    public void setAuthorityContact(String authorityContact) {
        this.authorityContact = authorityContact;
    }

    public String getAuthorityEmail() {
        return authorityEmail;
    }

    public void setAuthorityEmail(String authorityEmail) {
        this.authorityEmail = authorityEmail;
    }

    public String getHospitalType() {
        return hospitalType;
    }

    public void setHospitalType(String hospitalType) {
        this.hospitalType = hospitalType;
    }

    // Getters and Setters for Additional Details
    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getEstYear() {
        return estYear;
    }

    public void setEstYear(String estYear) {
        this.estYear = estYear;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    // Facilities Getters and Setters
    public Map<String, Boolean> getFacilities() {
        return facilities;
    }

    public void setFacilities(Map<String, Boolean> facilities) {
        this.facilities = facilities;
    }

    // Convenience methods for facilities
    public boolean hasOrganTransplant() {
        return facilities.getOrDefault("organ_transplant", false);
    }

    public boolean hasICU() {
        return facilities.getOrDefault("icu", false);
    }

    public boolean hasEmergency() {
        return facilities.getOrDefault("emergency", false);
    }

    public boolean hasOrganStorage() {
        return facilities.getOrDefault("organ_storage", false);
    }

    public boolean hasLaboratory() {
        return facilities.getOrDefault("laboratory", false);
    }

    public boolean hasAmbulance() {
        return facilities.getOrDefault("ambulance", false);
    }

    // Method to get facilities summary for display
    public String getFacilitiesSummary() {
        StringBuilder summary = new StringBuilder();
        if (hasOrganTransplant()) summary.append("• Organ Transplant\n");
        if (hasICU()) summary.append("• ICU\n");
        if (hasEmergency()) summary.append("• Emergency Services\n");
        if (hasOrganStorage()) summary.append("• Organ Storage\n");
        if (hasLaboratory()) summary.append("• Laboratory\n");
        if (hasAmbulance()) summary.append("• Ambulance Service\n");
        return summary.toString().trim();
    }
}
