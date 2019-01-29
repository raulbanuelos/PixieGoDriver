package com.pixie.driver.historyRecyclerView;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by raulb on 07/05/2018.
 */

public class TravelDetails {
    private float LocationBeginLatitude;
    private float LocationBeginLongitude;
    private float LocationEndLatitud;
    private float LocationEndLongitude;
    private String customer;
    private String driver;
    private float rate;
    private float ratingCustomer;
    private float travelDistance;
    private float travelTime;
    private String DateBeginRide;
    private String DateEndRide;

    public TravelDetails(){

    }

    public TravelDetails(float locationBeginLatitude, float locationBeginLongitude, float locationEndLatitud, float locationEndLongitude, String customer, String driver, float rate, float ratingCustomer, float travelDistance, float travelTime, String dateBeginRide, String dateEndRide) {
        LocationBeginLatitude = locationBeginLatitude;
        LocationBeginLongitude = locationBeginLongitude;
        LocationEndLatitud = locationEndLatitud;
        LocationEndLongitude = locationEndLongitude;
        this.customer = customer;
        this.driver = driver;
        this.rate = rate;
        this.ratingCustomer = ratingCustomer;
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
        DateBeginRide = dateBeginRide;
        DateEndRide = dateEndRide;
    }

    public String getDateBeginRide() {
        return DateBeginRide;
    }

    public void setDateBeginRide(String dateBeginRide) {
        DateBeginRide = dateBeginRide;
    }

    public String getDateEndRide() {
        return DateEndRide;
    }

    public void setDateEndRide(String dateEndRide) {
        DateEndRide = dateEndRide;
    }

    public void setLocationBeginLatitude(float locationBeginLatitude) {
        LocationBeginLatitude = locationBeginLatitude;
    }

    public void setLocationBeginLongitude(float locationBeginLongitude) {
        LocationBeginLongitude = locationBeginLongitude;
    }

    public void setLocationEndLatitud(float locationEndLatitud) {
        LocationEndLatitud = locationEndLatitud;
    }

    public void setLocationEndLongitude(float locationEndLongitude) {
        LocationEndLongitude = locationEndLongitude;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setRatingCustomer(float ratingCustomer) {
        this.ratingCustomer = ratingCustomer;
    }

    public void setTravelDistance(float travelDistance) {
        this.travelDistance = travelDistance;
    }

    public void setTravelTime(float travelTime) {
        this.travelTime = travelTime;
    }



    public float getLocationBeginLatitude() {
        return LocationBeginLatitude;
    }

    public float getLocationBeginLongitude() {
        return LocationBeginLongitude;
    }

    public float getLocationEndLatitud() {
        return LocationEndLatitud;
    }

    public float getLocationEndLongitude() {
        return LocationEndLongitude;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDriver() {
        return driver;
    }

    public float getRate() {
        return rate;
    }

    public float getRatingCustomer() {
        return ratingCustomer;
    }

    public float getTravelDistance() {
        return travelDistance;
    }

    public float getTravelTime() {
        return travelTime;
    }



}
