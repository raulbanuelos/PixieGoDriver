package com.pixie.driver.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixie.driver.R;
import com.pixie.driver.historyRecyclerView.HistoryObject;
import com.pixie.driver.historyRecyclerView.TravelDetails;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ResumeDayActivity extends AppCompatActivity {

    private String userId;
    private TextView txtRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_day);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistory();
        txtRide = (TextView) findViewById(R.id.txtRate);
    }

    private void getUserHistory(){
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private float rate;
    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    try{
                        TravelDetails details;
                        details = dataSnapshot.getValue(TravelDetails.class);

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                        Date ar = formatter.parse(details.getDateBeginRide());

                        Date arr = formatter.parse(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));

                        if (ar.equals(arr)){
                            rate +=  details.getRate();
                            txtRide.setText(String.valueOf(rate));
                        }


                    }catch (Exception er){
                        String f = er.getMessage();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
