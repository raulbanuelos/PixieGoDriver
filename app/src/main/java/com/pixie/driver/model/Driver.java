package com.pixie.driver.model;

/**
 * Created by raulb on 23/09/2017.
 */

public class Driver {
    private String id;
    private String name;
    private String lastName;
    private String address;
    private String rfc;
    private String curp;
    private String placasTaxi;
    private Boolean isActive;
    private String profileImageUrl;

    public Driver() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getPlacasTaxi() {
        return placasTaxi;
    }

    public void setPlacasTaxi(String placasTaxi) {
        this.placasTaxi = placasTaxi;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Driver(String id, String name, String lastName, String address, String rfc, String curp, String placasTaxi, Boolean isActive,String profileImageUrl) {

        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.address = address;
        this.rfc = rfc;
        this.curp = curp;
        this.placasTaxi = placasTaxi;
        this.isActive = isActive;
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
