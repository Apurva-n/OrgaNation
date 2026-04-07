package com.organation.organation;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Map;

public class HospitalVerificationModel {
    private String id;
    private String hospitalName;
    private String authorityName;
    private String registrationNumber;
    private String email;
    private String phone;
    private String state;
    private String city;
    private String street;
    private String landmark;
    private String pincode;
    private String hospitalType;
    private String facilities;
    private String status;
    private Date registrationDate;
    private Date verificationDate;
    private String verifiedBy;
    private String rejectionReason;

    public HospitalVerificationModel() {
        // Default constructor for Firebase
    }

    public HospitalVerificationModel(Map<String, Object> data) {
        this.hospitalName = (String) data.get("hospitalName");
        this.authorityName = (String) data.get("authorityName");
        this.registrationNumber = (String) data.get("registrationNumber");
        this.email = (String) data.get("email");
        this.phone = (String) data.get("phone");
        this.state = (String) data.get("state");
        this.city = (String) data.get("city");
        this.street = (String) data.get("street");
        this.landmark = (String) data.get("landmark");
        this.pincode = (String) data.get("pincode");
        this.hospitalType = (String) data.get("hospitalType");
        this.facilities = (String) data.get("facilities");
        this.status = data.get("status") != null ? (String) data.get("status") : "pending";

        // ✅ Fixed: Handle Firestore Timestamp instead of Long
        Object regDate = data.get("registrationDate");
        if (regDate instanceof Timestamp) {
            this.registrationDate = ((Timestamp) regDate).toDate();
        } else if (regDate instanceof Long) {
            this.registrationDate = new Date((Long) regDate);
        }

        Object verDate = data.get("verificationDate");
        if (verDate instanceof Timestamp) {
            this.verificationDate = ((Timestamp) verDate).toDate();
        } else if (verDate instanceof Long) {
            this.verificationDate = new Date((Long) verDate);
        }

        this.verifiedBy = (String) data.get("verifiedBy");
        this.rejectionReason = (String) data.get("rejectionReason");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getAuthorityName() { return authorityName; }
    public void setAuthorityName(String authorityName) { this.authorityName = authorityName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public Date getVerificationDate() { return verificationDate; }
    public void setVerificationDate(Date verificationDate) { this.verificationDate = verificationDate; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}