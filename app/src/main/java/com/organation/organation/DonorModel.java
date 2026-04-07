package com.organation.organation;

import java.util.HashMap;
import java.util.Map;

public class DonorModel {
    private String aadhaarNo;
    private String fullName;
    private String age;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String height;
    private String weight;
    private String phone;
    private String email;
    private String state;
    private String city;
    private String street;
    private String landmark;
    private String organsToNDonate;
    private String medicalConditions;
    private String previousSurgeries;
    private Map<String, String> emergencyContact;

    public DonorModel() {
        this.emergencyContact = new HashMap<>();
    }

    public DonorModel(String fullName, String phone, String email, String bloodGroup, String organsToNDonate) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.organsToNDonate = organsToNDonate;
        this.emergencyContact = new HashMap<>();
    }

    // Getters and Setters
    public String getAadhaarNo() {
        return aadhaarNo;
    }

    public void setAadhaarNo(String aadhaarNo) {
        this.aadhaarNo = aadhaarNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getOrgansToNDonate() {
        return organsToNDonate;
    }

    public void setOrgansToNDonate(String organsToNDonate) {
        this.organsToNDonate = organsToNDonate;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getPreviousSurgeries() {
        return previousSurgeries;
    }

    public void setPreviousSurgeries(String previousSurgeries) {
        this.previousSurgeries = previousSurgeries;
    }

    public Map<String, String> getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(Map<String, String> emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getFullAddress() {
        return (street != null ? street + ", " : "") +
               (city != null ? city + ", " : "") +
               (state != null ? state : "");
    }
    
    public String getOrgansToDonate() {
        return organsToNDonate;
    }
}
