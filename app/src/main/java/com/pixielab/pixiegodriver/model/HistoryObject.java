package com.pixielab.pixiegodriver.model;

import android.text.method.HideReturnsTransformationMethod;

/**
 * Created by raulb on 06/11/2017.
 */

public class HistoryObject {
    private String rideId;

    public HistoryObject(String rideId){
        this.rideId = rideId;
    }

    public  String getRideId(){
        return rideId;
    }


}
