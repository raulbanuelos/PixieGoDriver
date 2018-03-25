package com.pixie.driver.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pixie.driver.R;
import com.pixie.driver.model.Driver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "CreateAccountActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private Button btnJoinUs;

    private ImageView mProfileImage;
    private Uri resultUri;

    private TextInputEditText edtNombre;
    private TextInputEditText edtApellido;
    private TextInputEditText edtDireccion;
    private TextInputEditText edtRFC;
    private TextInputEditText edtCURP;
    private TextInputEditText edtPlacas;
    private TextInputEditText edtEmail;
    private TextInputEditText edtpassword;
    private TextInputEditText edtConfirmPassword;

    private SwitchCompat IsDriver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        showToolbar(getResources().getString(R.string.toolbar_tittle_createaccount), true);

        btnJoinUs = (Button)findViewById(R.id.joinUs);

        edtNombre = (TextInputEditText) findViewById( R.id.name );
        edtApellido = (TextInputEditText) findViewById( R.id.apellidos );
        edtDireccion = (TextInputEditText) findViewById( R.id.direccion );
        edtRFC = (TextInputEditText) findViewById( R.id.RFC );
        edtCURP = (TextInputEditText) findViewById( R.id.CURP );
        edtPlacas = (TextInputEditText) findViewById( R.id.Placas );
        edtEmail = (TextInputEditText) findViewById(R.id.email);
        edtpassword = (TextInputEditText)findViewById(R.id.password);
        edtConfirmPassword = (TextInputEditText) findViewById( R.id.confirmpassword );

        mProfileImage = (ImageView)findViewById( R.id.profileImage );

        mProfileImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( Intent.ACTION_PICK );
                intent.setType( "image/*" );
                startActivityForResult( intent,1 );
            }
        } );

        IsDriver = (SwitchCompat) findViewById(R.id.isDriver);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.w(TAG, "Usuario Logueado" + firebaseUser.getEmail());
                }else{
                    Log.w(TAG,"Usuario no logueado");
                }
            }
        };

        btnJoinUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (fieldsValid())
                    createAccountDriver();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI( resultUri );

        }
    }
    DatabaseReference databaseReference;
    Driver driver;
    private void createAccountDriver() {
        String email = edtEmail.getText().toString();
        String password = edtpassword.getText().toString();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    if (IsDriver.isChecked()){
                        //DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                        //current_user_db.setValue(true);

                        //String id, String name, String lastName, String address, String rfc, String curp, String placasTaxi, Boolean isActive

                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        driver = new Driver(user_id,edtNombre.getText().toString(),edtApellido.getText().toString(), edtDireccion.getText().toString(),edtRFC.getText().toString(),edtCURP.getText().toString(),edtPlacas.getText().toString(),false,"");
                        databaseReference.child("Users").child("Drivers").child( driver.getId() ).setValue( driver );

                        if (resultUri != null){
                            StorageReference filePath = FirebaseStorage.getInstance().getReference().child( "profile_images" ).child( user_id );
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap( getApplication().getContentResolver(),resultUri );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress( Bitmap.CompressFormat.JPEG,20,baos );
                            byte[] data = baos.toByteArray();

                            UploadTask uploadTask = filePath.putBytes( data );

                            uploadTask.addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                    Map newImage = new HashMap();
                                    newImage.put( "profileImageUrl",downloadUrl.toString() );
                                    databaseReference.child("Users").child("Drivers").child( driver.getId() ).updateChildren( newImage );


                                    finish();
                                    return;

                                }
                            } );
                        }


                    }else{
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                        current_user_db.setValue(true);
                    }
                    Toast.makeText(CreateAccountActivity.this,"Cuenta creada existosamente",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(CreateAccountActivity.this,"Ocurrió un error al crear la cuenta.",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private Boolean fieldsValid(){
        if (edtNombre.getText().toString().trim().length() > 0){
            if (edtApellido.getText().toString().trim().length()>0) {
                if (edtDireccion.getText().toString().trim().length() > 0) {
                    if (edtRFC.getText().toString().trim().length() > 0) {
                        if (edtCURP.getText().toString().trim().length() > 0) {
                            if (edtPlacas.getText().toString().trim().length() > 0) {
                                if (edtEmail.getText().toString().trim().length() > 0) {
                                    if (edtpassword.getText().toString().trim().length() > 0) {
                                        if (edtConfirmPassword.getText().toString().trim().length() > 0) {
                                            if (edtpassword.getText().toString().length() < 8){

                                                Toast.makeText(CreateAccountActivity.this,"La contraseña debe ser al menos 8 digitos",Toast.LENGTH_LONG).show();
                                                return  false;
                                            }

                                            if (!edtpassword.getText().toString().equals( edtConfirmPassword.getText().toString())){
                                                Toast.makeText(CreateAccountActivity.this,"Las contraseñas no coinciden",Toast.LENGTH_LONG).show();
                                                return  false;
                                            }

                                            if (!validarEmail()){
                                                Toast.makeText(CreateAccountActivity.this,"Ingrese un email valido",Toast.LENGTH_LONG).show();
                                                return  false;
                                            }
                                        }
                                        else{
                                            Toast.makeText(CreateAccountActivity.this,"Favor de confirmar la contraseña",Toast.LENGTH_LONG).show();
                                            return  false;
                                        }
                                    }
                                    else{
                                        Toast.makeText(CreateAccountActivity.this,"Favor de ingresar la contraseña",Toast.LENGTH_LONG).show();
                                        return  false;
                                    }
                                }
                                else{
                                    Toast.makeText(CreateAccountActivity.this,"Favor de ingresar el email",Toast.LENGTH_LONG).show();
                                    return  false;
                                }
                            }
                            else{
                                Toast.makeText(CreateAccountActivity.this,"Favor de ingresar las placas del taxi",Toast.LENGTH_LONG).show();
                                return  false;
                            }
                        }
                        else{
                            Toast.makeText(CreateAccountActivity.this,"Favor de ingresar la CURP",Toast.LENGTH_LONG).show();
                            return  false;
                        }
                    }
                    else{
                        Toast.makeText(CreateAccountActivity.this,"Favor de ingresar el RFC",Toast.LENGTH_LONG).show();
                        return  false;
                    }
                }
                else{
                    Toast.makeText(CreateAccountActivity.this,"Favor de ingresar la dirección",Toast.LENGTH_LONG).show();
                    return  false;
                }
            }
            else{
                Toast.makeText(CreateAccountActivity.this,"Favor de ingresar los apellidos",Toast.LENGTH_LONG).show();
                return  false;
            }
        }
        else{
            Toast.makeText(CreateAccountActivity.this,"Favor de ingresar el nombre",Toast.LENGTH_LONG).show();
            return  false;
        }

        if (resultUri == null){
            Toast.makeText(CreateAccountActivity.this,"Por favor seleccione una imagen",Toast.LENGTH_LONG).show();
            return  false;
        }

        return  true;
    }

    private boolean validarEmail(){
        boolean regresar = false;
        String validacionEmail ="^[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$" ;
        Pattern pattern = Pattern.compile(validacionEmail, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(edtEmail.getText().toString());
        if (matcher.matches()) {
            regresar = true;
        }else{
            return false;
        }
        return regresar;
    }

    public  void showToolbar(String tittle, boolean upButton)
    {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

}
