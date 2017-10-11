package com.pixielab.pixiegodriver.view;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.pixielab.pixiegodriver.LoginActivity;
import com.pixielab.pixiegodriver.R;
import com.pixielab.pixiegodriver.model.Customer;
import com.pixielab.pixiegodriver.model.Driver;
import com.shitij.goyal.slidebutton.SwipeButton;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DriverMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    //<editor-fold desc="TAGS">
    private static final String TAG_DRIVES = "Drivers";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1 ;
    //</editor-fold>

    private Context context;
    private FloatingActionButton btnNavegarDestino;
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

    Polyline lDriverToCustomer; //Representa el camino del driver hacia donde se encuentra el customer.
    Marker positionBeginCustomer;
    private Boolean isDrawRoute = true;

    Polyline lCustomerDestination;  //Representa el camino destino que eligió el cliente.
    Marker positionDestinationCustomer; //Represtna el marker del destino del cliente.
    //</editor-fold>

    //<editor-fold desc="ButtonSheet">
    private LinearLayout bottomSheet;
    private BottomSheetBehavior bsb;
    TextView txtNameCustomer;
    SwipeButton swipeButton;
    //</editor-fold>

    private Boolean isLogginOut = false;

    //<editor-fold desc="Status of Activity">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();

        notificationService = MediaPlayer.create(this,R.raw.notifi);


        swipeButton = (SwipeButton)findViewById(R.id.slide);
        swipeButton.addOnSwipeCallback(new SwipeButton.Swipe(){
            @Override
            public void onButtonPress() {

            }

            @Override
            public void onSwipeCancel() {

            }

            @Override
            public void onSwipeConfirm() {
                if (customerId != "" && isStarted == null)
                {
                    String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference workingDriverRef = FirebaseDatabase.getInstance().getReference();
                    isStarted = true;
                    workingDriverRef.child("DriversWorking").child(driverId).child("Started").setValue( isStarted );

                    swipeButton.setText( "Desliza para terminar viaje ->" );

                    getDestinationCustomer();

                    if (isDrawRoute)
                        lDriverToCustomer.remove();

                    if (positionBeginCustomer != null)
                        positionBeginCustomer.remove();
                }else if(isStarted){
                    customerId = "";
                    isStarted = null;
                    latitudDestination = 0;
                    longuitudDestination = 0;

                    lCustomerDestination.remove();
                    positionDestinationCustomer.remove();
                    
                    String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference workingDriverRef = FirebaseDatabase.getInstance().getReference();
                    workingDriverRef.child("DriversWorking").child(driverId).removeValue();
                    workingDriverRef.child( "Users" ).child( "Drivers" ).child( driverId ).child( "customerRideId" ).removeValue();

                    getAssignedCustomer();
                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                    txtNameCustomer.setText("");
                    btnNavegarDestino.setVisibility(View.INVISIBLE);



                }
            }
        });

        txtNameCustomer = (TextView) findViewById(R.id.txtCustomerName);
        bottomSheet = (LinearLayout)findViewById(R.id.bottomSheetMapDriver);
        bsb = BottomSheetBehavior.from(bottomSheet);
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


        setLocationManager((LocationManager)getSystemService(Context.LOCATION_SERVICE));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPermission()){
            requestPermission();
        }
    }

    private Boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return  true;
        }else{
        return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(context,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAssignedCustomer();
                } else {
                    //Mostrar al usuario que no se puedes continuar sin los permisos de Location
                }
                return;
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLogginOut){
            disconnectDriver();
        }
    }

    private void disconnectDriver(){
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

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_loguot) {
            LogOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
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

                        isAssigned = "";

                        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference assignedCustomerRefa = FirebaseDatabase.getInstance().getReference();
                        assignedCustomerRefa.child("Users").child("Drivers").child(driverId).child("customerRideId").child("accepted").setValue(true);
                        assignedCustomerRefa.child("customerRequest").child(customerId).child("driverId").setValue(driverId);


                        getAssignedCustomerPickupLocation();

                    }
                }).setNegativeButton("Ignorar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isAssigned = "";
                String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference assignedCustomerRefv = FirebaseDatabase.getInstance().getReference();
                assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").child("accepted").setValue(false);
                assignedCustomerRefv.child("Users").child("Drivers").child(driverId).child("customerRideId").removeValue();
                customerId = "";
                isStarted = false;

                getAssignedCustomer();
            }
        });
        //dialog.show();

        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();
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

                    //Comenzamos a pintar la ruta
                    DateTime now = new DateTime();

                    com.google.maps.model.LatLng destino = new com.google.maps.model.LatLng(locationLat,locationLng);
                    com.google.maps.model.LatLng origen = new com.google.maps.model.LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    try {

                        //bsb.setState( BottomSheetBehavior.STATE_COLLAPSED );
                        //swipeButton.setText( "Desliza para termina el viaje ->" );

                        DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                                .mode(TravelMode.DRIVING).origin(origen)
                                .destination(destino).departureTime(now)
                                .await();

                        List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
                        lCustomerDestination = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                        isDrawRoute = true;
                        assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);

                        if (latitudDestination != 0  && longuitudDestination != 0){
                            btnNavegarDestino.setVisibility(View.VISIBLE);
                        }

                    } catch (ApiException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    } catch (IOException e) {
                        e.printStackTrace();
                        isDrawRoute = false;
                        assignedCustomerDestination.removeEventListener(mValueCustomerRequetsDestination);
                    }
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
                    DateTime now = new DateTime();

                    com.google.maps.model.LatLng destino = new com.google.maps.model.LatLng(locationLat,locationLng);
                    com.google.maps.model.LatLng origen = new com.google.maps.model.LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    try {

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
                    }
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

}
