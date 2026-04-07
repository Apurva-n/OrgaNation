package com.organation.organation;

public class RequestModel {
    // Basic Request Information
    private String requestId;
    private String recipientUid;
    private String recipientName;
    private String recipientAadhaar;
    
    // Organ Information
    private String organType;
    private String bloodType;
    private String urgency;
    
    // Hospital Information
    private String hospitalName;
    private String hospitalCity;
    private String hospitalLocation;
    private String treatingDoctor;
    
    // Medical Information
    private String medicalDetails;
    private String additionalNotes;
    
    // Request Details
    private String requestDate;
    private String status; // pending, approved, processed, completed, declined
    
    // Hospital Actions
    private String hospitalNotes;
    private String approvedDate;
    private String processedDate;
    private String completedDate;
    private String declinedDate;
    private String declinedReason;

    // Constructors
    public RequestModel() {}

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRecipientUid() {
        return recipientUid;
    }

    public void setRecipientUid(String recipientUid) {
        this.recipientUid = recipientUid;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientAadhaar() {
        return recipientAadhaar;
    }

    public void setRecipientAadhaar(String recipientAadhaar) {
        this.recipientAadhaar = recipientAadhaar;
    }

    public String getOrganType() {
        return organType;
    }

    public void setOrganType(String organType) {
        this.organType = organType;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalCity() {
        return hospitalCity;
    }

    public void setHospitalCity(String hospitalCity) {
        this.hospitalCity = hospitalCity;
    }

    public String getHospitalLocation() {
        return hospitalLocation;
    }

    public void setHospitalLocation(String hospitalLocation) {
        this.hospitalLocation = hospitalLocation;
    }

    public String getTreatingDoctor() {
        return treatingDoctor;
    }

    public void setTreatingDoctor(String treatingDoctor) {
        this.treatingDoctor = treatingDoctor;
    }

    public String getMedicalDetails() {
        return medicalDetails;
    }

    public void setMedicalDetails(String medicalDetails) {
        this.medicalDetails = medicalDetails;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHospitalNotes() {
        return hospitalNotes;
    }

    public void setHospitalNotes(String hospitalNotes) {
        this.hospitalNotes = hospitalNotes;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(String processedDate) {
        this.processedDate = processedDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getDeclinedDate() {
        return declinedDate;
    }

    public void setDeclinedDate(String declinedDate) {
        this.declinedDate = declinedDate;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    // Convenience methods
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isProcessed() {
        return "processed".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isDeclined() {
        return "declined".equals(status);
    }

    public String getStatusDisplay() {
        if (status == null) {
            return "Unknown Status";
        }
        
        switch (status) {
            case "pending":
                return "⏳ Pending";
            case "approved":
                return "✅ Approved";
            case "processed":
                return "🔄 In Process";
            case "completed":
                return "🎉 Completed";
            case "declined":
                return "❌ Declined";
            default:
                return status;
        }
    }

    public int getStatusColor() {
        if (status == null) {
            return android.R.color.black;
        }
        
        switch (status) {
            case "pending":
                return android.R.color.holo_orange_dark;
            case "approved":
                return android.R.color.holo_green_dark;
            case "processed":
                return R.color.purple;
            case "completed":
                return R.color.accent;
            case "declined":
                return R.color.heart_red;
            default:
                return android.R.color.black;
        }
    }
}
