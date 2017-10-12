package com.pixielab.pixiegodriver.model;

import com.firebase.geofire.GeoFire;

import org.joda.time.DateTime;

/**
 * Created by raulb on 11/10/2017.
 */

public class Travel {
    public Customer customer;
    public Driver driver;
    public DateTime dateTimeStart;
    public DateTime dateTimeEnd;
    public GeoFire mLocationStart;
    public GeoFire mLocationEnd;


}
