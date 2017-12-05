package com.pixielab.pixiegodriver.historyRecyclerView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixielab.pixiegodriver.R;

import org.w3c.dom.Text;

import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String rideId, currentUserId, customerId, driverId,userDriverOrCustomer;

    private TextView rideLocation;
    private TextView rideDistance;
    private TextView rideDate;
    private TextView userName;
    private TextView userPhone;
    private ImageView userImage;

    private DatabaseReference historyRideInfoDb;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        rideId = getIntent().getExtras().getString("rideId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        rideLocation = (TextView) findViewById(R.id.rideLocation);
        rideDistance = (TextView)findViewById(R.id.rideDistance);
        rideDate = (TextView) findViewById(R.id.rideDate);
        userName = (TextView) findViewById(R.id.username);
        userPhone = (TextView) findViewById(R.id.userPhone);
        userImage = (ImageView) findViewById(R.id.userImage);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();

    }

    private void getRideInformation() {

        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot child: dataSnapshot.getChildren()){
                        if (child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
                            if (!customerId.equals(currentUserId)){
                                userDriverOrCustomer = "Drivers";
                                getUserInformation("Customers", customerId);
                            }
                        }

                        if (child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
                            if (!driverId.equals(currentUserId)){
                                userDriverOrCustomer = "Customers";
                                getUserInformation("Drivers", driverId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserInformation(String otherUserDriversOrCustomer, String otherUserId) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriversOrCustomer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String,Object>)dataSnapshot.getValue();
                    if (map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }

                    if (map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }

                    if (map.get("profileImageUrl") != null){
                        
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        
    }
}
