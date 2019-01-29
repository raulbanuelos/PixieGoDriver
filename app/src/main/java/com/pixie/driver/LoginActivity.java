package com.pixie.driver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixie.driver.model.Driver;
import com.pixie.driver.view.DriverMapActivity;

public class LoginActivity extends AppCompatActivity   {

    //<editor-fold desc="TAGS">
    private static final String TAG_DRIVERS = "Drivers";
    private static final String TAG_USERS = "Users";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1 ;
    //</editor-fold>

    private Context context;

    //<editor-fold desc="Elementos">
    private Button btnLogin;
    private TextInputEditText txtUserName;
    private TextInputEditText txtPassword;
    private ProgressDialog progressDialog;
    //</editor-fold>

    //<editor-fold desc="Base de datos">
    private FirebaseAuth firebaseAuth;
    private ValueEventListener mValueEventListener;
    DatabaseReference databaseReference;
    FirebaseAuth.AuthStateListener authStateListener;
    //</editor-fold>

    //<editor-fold desc="Status de activity">
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    //Toast.makeText(LoginActivity.this, "You are signed in Firebase!", Toast.LENGTH_SHORT).show();

                    String IdDriver = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(TAG_USERS).child(TAG_DRIVERS).child(IdDriver);
                    mValueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                Driver driver = dataSnapshot.getValue(Driver.class);

                                if (driver != null){
                                    databaseReference.removeEventListener(mValueEventListener);
                                    if (driver.getActive()){
                                        Toast.makeText(LoginActivity.this,R.string.msg_bienvenido + driver.getName(),Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this,DriverMapActivity.class);

                                        intent.putExtra("IsPrivate",driver.getIsPrivate());

                                        startActivity(intent);
                                        finish();
                                        return;
                                    }else
                                    {
                                        Toast.makeText(LoginActivity.this,R.string.msg_account_no_active,Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    databaseReference.removeEventListener(mValueEventListener);
                                    Toast.makeText(LoginActivity.this,R.string.error_inicio_session,Toast.LENGTH_SHORT).show();
                                }
                                getProgressDialog().hide();

                            }catch (Exception er){
                                getProgressDialog().hide();
                                String a = er.getMessage();
                                Toast.makeText(LoginActivity.this,er.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            getProgressDialog().hide();
                        }
                    });

                }else{
                    Toast.makeText(LoginActivity.this, "Por favor inicia sesión!", Toast.LENGTH_SHORT).show();
                }
            }
        };


        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        inicializarProgres();
        btnLogin = (Button)findViewById(R.id.login);
        txtPassword = (TextInputEditText)findViewById(R.id.password);
        txtUserName = (TextInputEditText)findViewById(R.id.username);

        txtUserName.setText("");
        txtPassword.setText("");

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogInDriver();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getAssignedCustomer();
                } else {
                    Toast.makeText(context,"Debes permitir el acceso a la ubicación para poder usar la app.",Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private Boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return  true;
        }else{
            return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(context,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPermission()){
            requestPermission();
        }
    }

    //</editor-fold>

    //<editor-fold desc="Métodos generales">
    private  void LogInDriver(){
        if (error()){
            String email = txtUserName.getText().toString();
            String password = txtPassword.getText().toString();
            getProgressDialog().show();
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        getProgressDialog().hide();
                        Toast.makeText(LoginActivity.this,R.string.error_inicio_session,Toast.LENGTH_SHORT).show();
                    }
                    else{
                        /*String IdDriver = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(TAG_USERS).child(TAG_DRIVERS).child(IdDriver);
                        mValueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try{
                                    Driver driver = dataSnapshot.getValue(Driver.class);

                                    if (driver != null){
                                        databaseReference.removeEventListener(mValueEventListener);
                                        if (driver.getActive()){
                                            Toast.makeText(LoginActivity.this,R.string.msg_bienvenido + driver.getName(),Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this,DriverMapActivity.class);

                                            intent.putExtra("IsPrivate",driver.getIsPrivate());

                                            startActivity(intent);
                                            finish();
                                            return;
                                        }else
                                        {
                                            Toast.makeText(LoginActivity.this,R.string.msg_account_no_active,Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        databaseReference.removeEventListener(mValueEventListener);
                                        Toast.makeText(LoginActivity.this,R.string.error_inicio_session,Toast.LENGTH_SHORT).show();
                                    }
                                    getProgressDialog().hide();

                                }catch (Exception er){
                                    getProgressDialog().hide();
                                    String a = er.getMessage();
                                    Toast.makeText(LoginActivity.this,er.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                getProgressDialog().hide();
                            }
                        });*/
                    }
                }
            });
        }
    }

    private Boolean error(){
        if (txtUserName.getText().toString().trim().length() > 0){
            if (txtPassword.getText().toString().trim().length() > 0){
                return true;
            }
            else{
                Toast.makeText(LoginActivity.this,R.string.msg_ingresa_contrasena,Toast.LENGTH_LONG).show();
                return  false;
            }
        }
        else{
            Toast.makeText(LoginActivity.this,R.string.msg_ingresa_usuario,Toast.LENGTH_LONG).show();
            return  false;
        }
    }

    private void inicializarProgres(){
        setProgressDialog(new ProgressDialog(this));
        getProgressDialog().setMessage("Por Favor Espere...");
        getProgressDialog().setCancelable(false);
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }
    //</editor-fold>

}
