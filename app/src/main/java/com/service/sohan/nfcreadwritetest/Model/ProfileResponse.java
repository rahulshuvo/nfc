package com.service.sohan.nfcreadwritetest.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("Data")
    @Expose
    private List<Data> data = null;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ProfileResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public class Data {

        @SerializedName("Id")
        @Expose
        private Integer id;
        @SerializedName("Email")
        @Expose
        private String email;
        @SerializedName("Phone")
        @Expose
        private String phone;
        @SerializedName("FirstName")
        @Expose
        private String firstName;
        @SerializedName("LastName")
        @Expose
        private String lastName;
        @SerializedName("Title")
        @Expose
        private String title;
        @SerializedName("CompanyName")
        @Expose
        private String companyName;
        @SerializedName("WorkPhone")
        @Expose
        private String workPhone;
        @SerializedName("WorkPhoneExt")
        @Expose
        private String workPhoneExt;
        @SerializedName("Address")
        @Expose
        private String address;
        @SerializedName("Address1")
        @Expose
        private String address1;
        @SerializedName("State")
        @Expose
        private String state;
        @SerializedName("City")
        @Expose
        private String city;
        @SerializedName("Other")
        @Expose
        private String other;
        @SerializedName("Website")
        @Expose
        private String website;
        @SerializedName("Category")
        @Expose
        private String category;
        @SerializedName("TypeOfContact")
        @Expose
        private String typeOfContact;
        @SerializedName("RefPhone")
        @Expose
        private String refPhone;
        @SerializedName("Fax")
        @Expose
        private String Fax;
        @SerializedName("Region")
        @Expose
        private String Region;
        @SerializedName("Zip")
        @Expose
        private String Zip;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getWorkPhone() {
            return workPhone;
        }

        public void setWorkPhone(String workPhone) {
            this.workPhone = workPhone;
        }

        public String getWorkPhoneExt() {
            return workPhoneExt;
        }

        public void setWorkPhoneExt(String workPhoneExt) {
            this.workPhoneExt = workPhoneExt;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress1() {
            return address1;
        }

        public void setAddress1(String address1) {
            this.address1 = address1;
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

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTypeOfContact() {
            return typeOfContact;
        }

        public void setTypeOfContact(String typeOfContact) {
            this.typeOfContact = typeOfContact;
        }

        public String getRefPhone() {
            return refPhone;
        }

        public void setRefPhone(String refPhone) {
            this.refPhone = refPhone;
        }

        public String getFax() {
            return Fax;
        }

        public void setFax(String fax) {
            Fax = fax;
        }

        public String getRegion() {
            return Region;
        }

        public void setRegion(String region) {
            Region = region;
        }

        public String getZip() {
            return Zip;
        }

        public void setZip(String zip) {
            Zip = zip;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "id=" + id +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", title='" + title + '\'' +
                    ", companyName='" + companyName + '\'' +
                    ", workPhone='" + workPhone + '\'' +
                    ", workPhoneExt='" + workPhoneExt + '\'' +
                    ", address='" + address + '\'' +
                    ", address1='" + address1 + '\'' +
                    ", state='" + state + '\'' +
                    ", city='" + city + '\'' +
                    ", other='" + other + '\'' +
                    ", website='" + website + '\'' +
                    ", category='" + category + '\'' +
                    ", typeOfContact='" + typeOfContact + '\'' +
                    ", refPhone='" + refPhone + '\'' +
                    ", Fax='" + Fax + '\'' +
                    ", Region='" + Region + '\'' +
                    ", Zip='" + Zip + '\'' +
                    '}';
        }
    }
}
