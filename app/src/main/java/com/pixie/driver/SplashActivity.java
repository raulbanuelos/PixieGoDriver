package com.pixie.driver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pixie.driver.view.onAppKilled;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(SplashActivity.this, onAppKilled.class));

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
