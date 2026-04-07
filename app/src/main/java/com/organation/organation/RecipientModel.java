package com.organation.organation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RecipientModel implements Serializable {
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
    private String organsNeeded;
    private String urgency;
    private Map<String, String> hospitalDetails;

    public RecipientModel() {
        this.hospitalDetails = new HashMap<>();
    }

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

    public String getOrgansNeeded() {
        return organsNeeded;
    }

    public void setOrgansNeeded(String organsNeeded) {
        this.organsNeeded = organsNeeded;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Map<String, String> getHospitalDetails() {
        return hospitalDetails;
    }

    public void setHospitalDetails(Map<String, String> hospitalDetails) {
        this.hospitalDetails = hospitalDetails;
    }
    
    public String getFullAddress() {
        return (street != null ? street + ", " : "") +
               (city != null ? city + ", " : "") +
               (state != null ? state : "");
    }
}
