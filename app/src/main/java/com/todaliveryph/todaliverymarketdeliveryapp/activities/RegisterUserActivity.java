package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class RegisterUserActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {

    private ImageButton backBTN;
    private ImageView profileIV;
    private EditText fullnameET, phoneET, addressET, emailET, passwordET, cpasswordET;
    private Button registerBTN;
    private TextView registerSellerTV;
    private Spinner routeSpinner,brgySpinner;
    private TextView registerRiderTV;

    //permission constants
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;



    //permission arrays
    private String[]cameraPermissions;
    private String[]storagePermissions;

    //image pick uri
    private Uri image_uri;

    private Bitmap bitmap;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        backBTN = findViewById(R.id.backBTN);
        profileIV =findViewById(R.id.profileIV);
        fullnameET =findViewById(R.id.fullnameET);
        phoneET =findViewById(R.id.phoneET);
        addressET =findViewById(R.id.addressET);
        emailET =findViewById(R.id.emailET);
        passwordET =findViewById(R.id.passwordET);
        cpasswordET =findViewById(R.id.cpasswordET);
        registerBTN = findViewById(R.id.registerBTN);
        registerSellerTV = findViewById(R.id.registerSellerTV);
        registerRiderTV = findViewById(R.id.registerRiderTV);

        //inside permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = firebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        routeSpinner = findViewById(R.id.routeSpin);
        brgySpinner = findViewById(R.id.brgySpin);

        String[] routeItems = new String[]{"STA CRUZ", "SANTIAGO LAUCPAO", "SAN PABLO 1st", "SAN PABLO 2nd",
                "DELA PAZ", "SAN PEDRO SAUG SANPEDRO PALCARANGAN", "SAN JOSE GUMI","BALANTACAN"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, routeItems);
        routeSpinner.setAdapter(adapter);
        routeSpinner.setOnItemSelectedListener(this);
        //when back img click
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //just like back button
            }
        });



        //Select Profile Picture
        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //Registering to database
        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        //Register as Seller
        registerSellerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class));
            }
        });

        //Register as Rider
        registerRiderTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this, RegisterRiderActivity.class));
            }
        });


    }   //Closing bracket of onCreate Bundle

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        if (routeSpinner.getSelectedItem().equals("STA CRUZ")) {
            Toast.makeText(getApplicationContext(), "STA CRUZ",
                    Toast.LENGTH_SHORT).show();
            String[] item1 = new String[]{"Remedios", "Sta Cruz", "San Roque Arbol", "Sta Maria", "Sta Teresa 2nd",
                    "San Agustin", "Sta Catalina", "Calangain"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item1);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("SANTIAGO LAUCPAO")){
            String[] item2 = new String[]{"Santiago","Laucpao"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item2);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("SAN PABLO 1st")){
            String[] item3 = new String[]{"San Pablo 1st"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item3);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("SAN PABLO 2nd")){
            String[] item4 = new String[]{"San Pablo 2nd"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item4);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("DELA PAZ")){
            String[] item5 = new String[]{"Dela Paz"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item5);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("SAN PEDRO SAUG SANPEDRO PALCARANGAN")){
            String[] item6 = new String[]{"San Pedro Saug", "San Pedro Palcarangan"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item6);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("SAN JOSE GUMI")){
            String[] item7 = new String[]{"San Jose Gumi"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item7);
            brgySpinner.setAdapter(adapter);
        }
        else if (routeSpinner.getSelectedItem().equals("BALANTACAN")){
            String[] item8 = new String[]{"Balantacan"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item8);
            brgySpinner.setAdapter(adapter);
        }


    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    String fullName, phoneNumber, address, email, password, confirmPassword;

    private void inputData() {

        fullName = fullnameET.getText().toString().trim();
        phoneNumber= phoneET.getText().toString().trim();
        address= addressET.getText().toString().trim();
        email= emailET.getText().toString().trim();
        password= passwordET.getText().toString().trim();
        confirmPassword= cpasswordET.getText().toString().trim();

        //conditions

        if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter Fullname ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Enter Phone Number ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Address Format ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length()<6){
            Toast.makeText(this, "Password too short. must be 6 characters or more ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Password doesn't match ...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account added
                        saveFirebaseData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //account creation dismissed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Uploading your information to out database...");

        String timestamp = ""+System.currentTimeMillis();

        if (image_uri==null){
            //save without profile picture

            //setup for saving data to firebase
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("address",""+address);
            hashMap.put("brgy",brgySpinner.getSelectedItem());
            hashMap.put("zone",routeSpinner.getSelectedItem());
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("profileImage","");

            //Final setup: saving to database

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //db failed to update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                            finish();

                        }
                    });

        }else{
            //save with profile picture

            //path and name of the image

            String filepathAndName = "profile_images/"+""+firebaseAuth.getUid();
            //image upload
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filepathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //get url of uploaded image file
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                //setup for saving data to firebase
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("brgy",brgySpinner.getSelectedItem());
                                hashMap.put("zone",routeSpinner.getSelectedItem());
                                hashMap.put("address",""+address);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","User");
                                hashMap.put("online","true");
                                hashMap.put("profileImage",""+downloadImageUri); ///convert image to url

                                //Final setup: saving to database

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // db updated

                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //db failed to update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                                finish();

                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void showImagePicDialog() {
        // options to display in dialog
        String[] options = {"Camera","Gallery"};
        //dialog popout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select image from")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle the clicks
                        if (which == 0){
                            // camera selected
                            if (checkCameraPermission()){
                                //permission allowed
                                pickFromCamera();
                            }else{
                                //not allowed
                                requestCameraPermission();
                            }
                        }else{
                            //gallery selected
                            if (checkStoragePermission()){
                                //permission allowed
                                pickfromGallery();
                            }else{
                                //not allowed
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickfromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        //image picked
                        Intent data = result.getData();
                        image_uri = data.getData();

                        profileIV.setImageURI(image_uri);
                    }else{
                        //cancelled
                        Toast.makeText(RegisterUserActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Bundle bundle = result.getData().getExtras();
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                        bitmap = (Bitmap) bundle.get("data");

                        image_uri = getImageUri(RegisterUserActivity.this ,bitmap);

                        profileIV.setImageURI(image_uri);
                    }
                }
            }
    );

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Camera Permission Denied ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowed
                        pickfromGallery();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Gallery Permission Denied ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                profileIV.setImageURI(image_uri);
            }else if (requestCode ==IMAGE_PICK_CAMERA_CODE){
                profileIV.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}   //Closing bracket of public class RegisterUserActivity