package com.pixie.driver;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by raulb on 25/11/2017.
 */

public class PixieFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG =  "PixieInstanceIdService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.w(TAG, "TokenRefresh: " + token);


    }
}

