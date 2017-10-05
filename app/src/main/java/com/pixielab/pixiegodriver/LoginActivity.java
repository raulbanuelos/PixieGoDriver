package com.pixielab.pixiegodriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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
import com.pixielab.pixiegodriver.model.Driver;
import com.pixielab.pixiegodriver.view.DriverMapActivity;

public class LoginActivity extends AppCompatActivity   {

    //<editor-fold desc="TAGS">
    private static final String TAG_DRIVERS = "Drivers";
    private static final String TAG_USERS = "Users";
    //</editor-fold>

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
    //</editor-fold>

    //<editor-fold desc="Status de activity">
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inicializarProgres();
        btnLogin = (Button)findViewById(R.id.login);
        txtPassword = (TextInputEditText)findViewById(R.id.password);
        txtUserName = (TextInputEditText)findViewById(R.id.username);

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogInDriver();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
