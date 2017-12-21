package com.pixielab.pixiegodriver.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.pixielab.pixiegodriver.HistoryActiviy;
import com.pixielab.pixiegodriver.LoginActivity;
import com.pixielab.pixiegodriver.R;
import com.pixielab.pixiegodriver.model.Customer;
import com.pixielab.pixiegodriver.model.Driver;
import com.pixielab.pixiegodriver.model.TimeCalculator;
import com.shitij.goyal.slidebutton.SwipeButton;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DriverMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    //<editor-fold desc="TAGS">
    private static final String TAG_DRIVES = "Drivers";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1 ;
    private static final String TAG = "Token";
    //</editor-fold>

    private ProgressDialog progressDialog;

    private Context context;
    private FloatingActionButton btnNavegarDestino;
    private FloatingActionButton btnPanico;
    private Switch mActiveSwitch;
    private MediaPlayer notificationService;


    //<editor-fold desc="Info Driver">
    private TextView txtDriverName;
    private ImageView photoDriver;

    //</editor-fold>

    //<editor-fold desc="Mapa">
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location mCurrentLocation;
    double latitudDestination;
    double longuitudDestination;
    LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private SupportMapFragment mapFragment;
    private List<Polyline> polylines;
    private List<Location> routeDriverWorking;
    private Date startRide;
    private Date endRide;
    private double tarifa;
    private float rideDistance;

    Polyline lDriverToCustomer; //Representa el camino del driver hacia donde se encuentra el customer.
    Marker positionBeginCustomer;
    private Boolean isDrawRoute = true;
    private Boolean isDrawRouteDestination = false;
    private DateTime dateBeginRide;

    Polyline lCustomerDestination;  //Representa el camino destino que eligió el cliente.
    Marker positionDestinationCustomer; //Represtna el marker del destino del cliente.
    Location LocationBeginRide;
    //</editor-fold>

    //<editor-fold desc="ButtonSheet">
    private LinearLayout bottomSheet;
    private BottomSheetBehavior bsb;
    TextView txtNameCustomer;
    SwipeButton swipeButton;

    private Button btnAuxStartEnd;
    //</editor-fold>

    private Boolean isLogginOut = false;

    private AlertDialog alertTarifa;

    //<editor-fold desc="Status of Activity">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.w(TAG, "token: " + token);
        FirebaseMessaging.getInstance().subscribeToTopic("InfoGralConductores");


        polylines = new ArrayList<>();
        routeDriverWorking = new ArrayList<>();


        context = getApplicationContext();

        setLocationManager((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            mapFragment.getMapAsync(this);
        }

        mActiveSwitch = (Switch) findViewById(R.id.activeSwitch);
        mActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                }else{
                    disconnectDriver();
                }
            }
        });

        notificationService = MediaPlayer.create(this,R.raw.notifir);

        btnAuxStartEnd = (Button) findViewById(R.id.btnAuxStartEnd);
        btnAuxStartEnd.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (customerId != "" && isStarted == null)
                        {
                            startRide = new Date();
                            rideDistance = 0;

                            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference workingDriverRef = FirebaseDatabase.getInstance().getReference();
                            routeDriverWorking.clear();
                            isStarted = true;
                            workingDriverRef.child("DriversWorking").child(driverId).child("Started").setValue( isStarted );
                            dateBeginRide = DateTime.now();
                            LocationBeginRide = mCurrentLocation;

                            btnAuxStartEnd.setText("Presiona para terminar el servicio");

                            //swipeButton.setText( "Desliza para terminar viaje ->" );

                            getDestinationCustomer();

                            //if (isDrawRoute)
                            //    lDriverToCustomer.remove();

                            if (positionBeginCustomer != null)
                                positionBeginCustomer.remove();
                        }else if(isStarted){
                            //Empezamos a calcular el costo del viaje

                            endRide = new Date();
                            TimeCalculator objCalculador = new TimeCalculator();
                            double tiempo = roundPlaces(objCalculador.getDiff(startRide,endRide),2);
                            //routeDriverWorking.add(mLastLocation);
                            //double distancia = roundPlaces( getDistance(),2);

                            tarifa = 0.0;
                            //tarifa = 6.0 + (1.38 * tiempo) + (3.37 * (distancia / 1000));

                            rideDistance = roundPlaces(rideDistance,2);

                            tarifa = 6.0 + (1.38 * tiempo)+ (3.37 * Double.valueOf( rideDistance));
                            tarifa = roundPlaces(tarifa,2);


                            if (tarifa < 20)
                                tarifa = 20;

                            //Toast.makeText(DriverMapActivity.this, "Tarifa: " + tarifa, Toast.LENGTH_LONG).show();

                            //Terminamos de calcular el costo del viaje
                            recordTarifa();
                            recordRide(rideDistance,tiempo);


                            //Empieza dialog personalizado
                            LayoutInflater inflater = getLayoutInflater();
                            View dialoglayout = inflater.inflate(R.layout.layout_tarifa, null);

                            final TextView mTarifa = (TextView) dialoglayout.findViewById(R.id.lblTarifa);
                            final TextView mDuracion = (TextView) dialoglayout.findViewById(R.id.lblDuracion);
                            final TextView mDistancia = (TextView) dialoglayout.findViewById(R.id.lblDistancia);
                            final Button mButtonAceptarTarifa = (Button) dialoglayout.findViewById(R.id.btnAceptarTarifa);
                            mButtonAceptarTarifa.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    erasePolyLines();

                                    customerId = "";
                                    isStarted = null;
                                    latitudDestination = 0;
                                    longuitudDestination = 0;

                                    //if (isDrawRouteDestination)
                                    //    lCustomerDestination.remove();

                                    if (positionDestinationCustomer != null)
                                        positionDestinationCustomer.remove();

                                    String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference workingDriverRef = FirebaseDatabase.getInstance().getReference();
                                    workingDriverRef.child("DriversWorking").child(driverId).removeValue();
                                    workingDriverRef.child( "Users" ).child( "Drivers" ).child( driverId ).child( "customerRideId" ).removeValue();

                                    getAssignedCustomer();
                                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                                    txtNameCustomer.setText("");
                                    mActiveSwitch.setVisibility(View.VISIBLE);
                                    btnNavegarDestino.setVisibility(View.INVISIBLE);
                                    alertTarifa.cancel();

                                }
                            });



                            mTarifa.setText("$" + tarifa);
                            mDuracion.setText(tiempo + " min.");
                            mDistancia.setText(rideDistance + " km.");



                            AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
                            builder.setView(dialoglayout);
                            builder.setCancelable(false);

                            alertTarifa = builder.create();
                            alertTarifa.show();

                            //builder.show();

                            //Termina dialog personalizado

                            /*AlertDialog alertDialog = new AlertDialog.Builder(DriverMapActivity.this).create();
                            alertDialog.setTitle("TARIFA");
                            alertDialog.setMessage("Distancia " + rideDistance + "\nTiempo: " + tiempo  +"\nLa tarifa del viaje es: " + tarifa);
                            //alertDialog.setMessage("La tarifa del viaje es: " + tarifa);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            */

                            /*
                            erasePolyLines();

                            customerId = "";
                            isStarted = null;
                            latitudDestination = 0;
                            longuitudDestination = 0;

                            //if (isDrawRouteDestination)
                            //    lCustomerDestination.remove();

                            if (positionDestinationCustomer != null)
                                positionDestinationCustomer.remove();

                            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference workingDriverRef = FirebaseDatabase.getInstance().getReference();
                            workingDriverRef.child("DriversWorking").child(driverId).removeValue();
                            workingDriverRef.child( "Users" ).child( "Drivers" ).child( driverId ).child( "customerRideId" ).removeValue();

                            getAssignedCustomer();
                            bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                            txtNameCustomer.setText("");
                            mActiveSwitch.setVisibility(View.VISIBLE);
                            btnNavegarDestino.setVisibility(View.INVISIBLE);*/
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }

        });

        //swipeButton = (SwipeButton)findViewById(R.id.slide);
        /*swipeButton.addOnSwipeCallback(new SwipeButton.Swipe(){
            @Override
            public void onButtonPress() {

            }

            @Override
            public void onSwipeCancel() {

            }

            @Override
            public void onSwipeConfirm() {

            }
        });*/

        txtNameCustomer = (TextView) findViewById(R.id.txtCustomerName);
        bottomSheet = (LinearLayout)findViewById(R.id.bottomSheetMapDriver);
        bsb = BottomSheetBehavior.from(bottomSheet);
        bsb.setPeekHeight(0);
        bsb.setState(BottomSheetBehavior.STATE_HIDDEN);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        txtDriverName = (TextView)header.findViewById(R.id.txtDriverName);
        photoDriver = (ImageView)header.findViewById(R.id.photoDriver);
        loadInfoDriver();

        btnPanico = (FloatingActionButton) findViewById(R.id.btnPanico);
        btnPanico.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("panic");
                DatabaseReference panicRef = FirebaseDatabase.getInstance().getReference().child("panic").child("new");

                String requestID = panicRef.push().getKey();

                driverRef.child(requestID).setValue(true);

                if (customerId.equals(null))
                    customerId = "";

                HashMap map = new HashMap();
                map.put("appLaunched",2);
                map.put("customer",customerId);
                map.put("driver",driverId);
                map.put("date",new Date());
                panicRef.child(requestID).updateChildren(map);

                DatabaseReference panicIdRef  = FirebaseDatabase.getInstance().getReference().child("panic").child("new").child(requestID);

                HashMap map1 = new HashMap();
                map1.put("0",mLastLocation.getLatitude());
                map1.put("1",mLastLocation.getLongitude());
                panicIdRef.child("l").updateChildren(map1);

                Toast.makeText(getApplicationContext(), R.string.msj_make_panic, Toast.LENGTH_LONG).show();


            }
        });

        btnNavegarDestino = (FloatingActionButton) findViewById(R.id.btnNavegarDestino);
        btnNavegarDestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double sourceLatitude = mLastLocation.getLatitude();
                double sourceLongitude = mLastLocation.getLongitude();
                double destinationLatitude = latitudDestination;
                double destinationLongitude =  longuitudDestination;
                String uri = "http://maps.google.com/maps?saddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try
                {
                    startActivity(intent);
                }catch(ActivityNotFoundException ex)
                {
                    try
                    {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    }
                    catch(ActivityNotFoundException innerEx)
                    {
                        Toast.makeText(DriverMapActivity.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        getAssignedCustomer();
    }

    public float roundPlaces(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public double roundPlaces(double val, int places) {
            long factor = (long)Math.pow(10,places); 
            val = val * factor;
            long tmp = Math.round(val);
            return (double)tmp / factor; 
    }           

    private double getDistance(){
        float distance = 0;
        double distance1 = 0;
        //Si el arraylist solo trae un elemento la distancia por consecuencia sera 0
        if (routeDriverWorking.size() == 1)
            return distance1;

        for (int i = 1; i < routeDriverWorking.size(); i ++){
            Location location1 = routeDriverWorking.get(i);
            Location location2 = routeDriverWorking.get(i - 1);
            //float distancePoints = (float)location1.distanceTo(location2);
            float[] results = new float[3];
            Location.distanceBetween(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(),location2.getLongitude(), results);
            distance += results[0];
        }
        distance1 =  distance;
        return distance1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msj_please_provide_permision_gps, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        isLogginOut = true;
    }

    private void disconnectDriver(){
        btnPanico.setVisibility(View.INVISIBLE);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }
    //</editor-fold>

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            // Handle the camera action
        } else if (id == R.id.nav_record) {
            goHistory();
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_loguot) {
            LogOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goHistory() {
        Intent intent = new Intent(DriverMapActivity.this, HistoryActiviy.class);
        intent.putExtra("customerOrDriver","Drivers");
        startActivity(intent);
        return;
    }

    private void LogOut(){
        isLogginOut = true;
        disconnectDriver();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverMapActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    //<editor-fold desc="Status of map">
    @Override
    public void onLocationChanged(Location location) {

            if (mLastLocation == null)
                mLastLocation = location;



            if (getApplicationContext() != null){

                if (isAssigned != "Yes" && customerId != "" && isStarted != null && isStarted)
                {
                    rideDistance += mCurrentLocation.distanceTo(location) / 1000;
                }

                mCurrentLocation = location;

                float distance = mCurrentLocation.distanceTo(mLastLocation);

                if (distance > 100){
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                    mLastLocation = location;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (userId != null){
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    if (isAssigned == "Yes"){
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.removeLocation(userId);
                    }else if (customerId != ""){
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    }else{
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
    }

    private void addLocationRoute(Location mlocation){
        if (routeDriverWorking.size()> 0){
            Location lastLocation = routeDriverWorking.get(routeDriverWorking.size() - 1);
            if (lastLocation.getLatitude() != mlocation.getLatitude() && lastLocation.getLongitude() != mlocation.getLongitude()){
                float mdistance =  lastLocation.distanceTo(routeDriverWorking.get(routeDriverWorking.size() - 1));
                //if (mdistance > 10)
                    routeDriverWorking.add(mlocation);
            }
        }
        else
            routeDriverWorking.add(mlocation);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void connectDriver() {
        btnPanico.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {

        return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)
                || getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    private void showAlert() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("GPS Descativo")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " + "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        startActivityForResult(myIntent, 999);
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    //</editor-fold>

    //<editor-fold desc="Customer request">
    private DatabaseReference assignedCustomerRef;
    private ValueEventListener mValueEventListener;

    private ValueEventListener mValueCustomerRequets;
    DatabaseReference assignedCustomerPickuplocationRef;

    private ValueEventListener mValueCustomerRequetsDestination;
    DatabaseReference assignedCustomerDestination;

    private String customerId = "";
    private String isAssigned = "";
    private Boolean isStarted;

    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRideId");
        mValueEventListener= assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    showAlertNewAssignedCustomer();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showAlertNewAssignedCustomer(){
        assignedCustomerRef.removeEventListener( mValueEventListener );
        isAssigned = "Yes";

        notificationService = MediaPlayer.create(this,R.raw.notifir);
        notificationService.start();

        final Vibrator vibrator;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(DriverMapActivity.this);

        dialog.setTitle("Tienes un nuevo servicio")
                .setCancelable(false)
                .setMessage("Un pasaje espera taxi cercas de ti")
                .setPositiveButton("Aceptar pasaje", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notificationService.stop();
                        vibrator.cancel();
                        erasePolyLines();
                        isAssigned = "";

                        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference assignedCustomerRefa = FirebaseDatabase.getInstance().getReference();
                        assignedCustomerRefa.child("Users").child("Drivers").child(driverId).child("customerRideId").child("accepted").setValue(true);
                        assignedCustomerRefa.child("customerRequest").child(customerId).child("driverId").setValue(driverId);

                        mActiveSwitch.setVisibility(View.INVISIBLE);

                        getAssignedCustomerPickupLocation();

                    }
                }).setNegativeButton("Ignorar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                notificationService.stop();
                isAssigned = "";
                String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference assignedCustomerRefv = FirebaseDatabase.getInstance().getReference();
                assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").child("accepted").setValue(false);
                assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").removeValue();
                customerId = "";
                isStarted = null;

                getAssignedCustomer();
            }
        });
        //dialog.show();

        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();

        //Begin
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()){
                    alertDialog.dismiss();
                    isAssigned = "";
                    String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference assignedCustomerRefv = FirebaseDatabase.getInstance().getReference();
                    assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").child("accepted").setValue(false);
                    assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").removeValue();
                    customerId = "";
                    isStarted = null;

                    getAssignedCustomer();
                }
            }
        };

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });
        handler.postDelayed(runnable,12000);
        //End
    }

    private void getDestinationCustomer(){
        assignedCustomerDestination = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("h").child("l");
        mValueCustomerRequetsDestination = assignedCustomerDestination.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat =0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat,locationLng);

                    latitudDestination = driverLatLng.latitude;
                    longuitudDestination = driverLatLng.longitude;
                    positionDestinationCustomer = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("destination location"));

                    erasePolyLines();
                    getRouteToMarker(driverLatLng);

                    if (latitudDestination != 0  && longuitudDestination != 0){
                        btnNavegarDestino.setVisibility(View.VISIBLE);
                    }

                    //Comenzamos a pintar la ruta
                    //DateTime now = new DateTime();

                    //com.google.maps.model.LatLng destino = new com.google.maps.model.LatLng(locationLat,locationLng);
                    //com.google.maps.model.LatLng origen = new com.google.maps.model.LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    //try {

                        //bsb.setState( BottomSheetBehavior.STATE_COLLAPSED );
                        //swipeButton.setText( "Desliza para termina el viaje ->" );

                        //DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                        //        .mode(TravelMode.DRIVING).origin(origen)
                        //        .destination(destino).departureTime(now)
                        //        .await();

                        //List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
                        //lCustomerDestination = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                        //isDrawRouteDestination = true;
                        //assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);

                        //if (latitudDestination != 0  && longuitudDestination != 0){
                        //    btnNavegarDestino.setVisibility(View.VISIBLE);
                        //}

                    //} catch (ApiException e) {
                     //   e.printStackTrace();
                       // isDrawRouteDestination = false;
                        //assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    //} catch (InterruptedException e) {
                      //  e.printStackTrace();
                        //isDrawRouteDestination = false;
                        //assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    //} catch (IOException e) {
                        //e.printStackTrace();
                        //isDrawRouteDestination = false;
                        //assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    //}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedCustomerPickupLocation(){
        DatabaseReference refInformationUser = FirebaseDatabase.getInstance().getReference();
        refInformationUser.child( "Users" ).child( "Customers" ).child( customerId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    Customer customer = dataSnapshot.getValue(Customer.class);
                    String nombre = customer.getNombre();
                    String apellidos = customer.getApellidos();
                    txtNameCustomer.setText(nombre);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );

        assignedCustomerPickuplocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        mValueCustomerRequets = assignedCustomerPickuplocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    positionBeginCustomer = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("pickup location"));

                    //Comenzamos a pintar la ruta
                    //DateTime now = new DateTime();

                    //com.google.maps.model.LatLng destino = new com.google.maps.model.LatLng(locationLat,locationLng);
                    //com.google.maps.model.LatLng origen = new com.google.maps.model.LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    getRouteToMarker(driverLatLng);
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    btnAuxStartEnd.setText("Preciona para iniciar el recorrido");
                    isDrawRoute = true;
                    assignedCustomerPickuplocationRef.removeEventListener(mValueCustomerRequets);

                    /*try {

                        bsb.setState( BottomSheetBehavior.STATE_COLLAPSED );
                        swipeButton.setText( "Desliza para iniciar viaje ->" );

                        DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                                .mode(TravelMode.DRIVING).origin(origen)
                                .destination(destino).departureTime(now)
                                .await();

                        List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
                        lDriverToCustomer = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                        isDrawRoute = true;
                        assignedCustomerPickuplocationRef.removeEventListener(mValueCustomerRequets);

                    } catch (ApiException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerPickuplocationRef.removeEventListener(mValueCustomerRequets);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerPickuplocationRef.removeEventListener(mValueCustomerRequets);
                    } catch (IOException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerPickuplocationRef.removeEventListener(mValueCustomerRequets);
                    }*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private GeoApiContext getGeoContext(){
        GeoApiContext geoApiContext = new GeoApiContext();

        return geoApiContext.setQueryRateLimit(3)
                .setApiKey( getString( R.string.google_api_key ) )
                .setConnectTimeout( 1, TimeUnit.SECONDS )
                .setReadTimeout( 1,TimeUnit.SECONDS )
                .setWriteTimeout( 1,TimeUnit.SECONDS );
    }
    //</editor-fold>

    private void loadInfoDriver(){

            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(TAG_DRIVES).child(driverId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        String nombre = driver.getName();
                        String apellidos = driver.getLastName();
                        String urlPhoto = driver.getProfileImageUrl();
                        Picasso.with(DriverMapActivity.this).load(urlPhoto).into(photoDriver);
                        txtDriverName.setText(nombre + " " + apellidos);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    private void recordTarifa() {
        //customerId
        DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("rate");
        customerRequestRef.setValue(tarifa);

    }

    private void recordRide(double travelDistance, double travelTime){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();

        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        DateTime dateEndRide = DateTime.now();

        HashMap map = new HashMap();
        map.put("driver", driverId);
        map.put("customer",customerId);
        map.put("DateBeginRide",dateBeginRide);
        map.put("LocationBeginLatitude",LocationBeginRide.getLatitude());
        map.put("LocationBeginLongitude",LocationBeginRide.getLongitude());
        map.put("LocationEndLatitud",mCurrentLocation.getLatitude());
        map.put("LocationEndLongitude",mCurrentLocation.getLongitude());
        map.put("DateEndRide",dateEndRide);
        map.put("ratingCustomer",0);
        map.put("travelTime", travelTime);
        map.put("travelDistance", travelDistance);
        map.put("rate",tarifa);

        historyRef.child(requestId).updateChildren(map);

    }

    //Se utilizan solo para ser referencia de los puntos que se van a pintar.
    private LatLng mDestinationLocation;
    private LatLng mLatLngCurretn;
    private void getRouteToMarker(LatLng pickupLatLng) {
        mDestinationLocation = pickupLatLng;
        mLatLngCurretn = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());

        progressDialog = ProgressDialog.show(this, "Por favor espere", "Buscando la ruta...", true);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        progressDialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorPrimaryLight,R.color.colorAccent,R.color.primary_dark_material_light};

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mLatLngCurretn);
        builder.include(mDestinationLocation);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width * 0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);


        progressDialog.dismiss();
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolyLines()
    {
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
    }
}
