package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterRiderActivity extends AppCompatActivity /*implements LocationListener*/ {

    private ImageButton backBTN;
    private ImageView profileIV;
    private EditText fullnameET,shopnameET, phoneET,deliveryfeeET, addressET, emailET, passwordET, cpasswordET,otpET;
    private ScrollView scrollView;
    private LinearLayout verificationLayout;
    private Button registerBTN, verifyBTN;

    //permission constants
   // public static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;


    //permission arrays
    private String[]cameraPermissions;
    private String[]storagePermissions;


    //private String[]locationpermissions;
    private LocationManager locationManager;
    private double latitude, longitude;

    //image pick uri
    private Uri image_uri;
    private Bitmap bitmap;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_rider);



        backBTN = findViewById(R.id.backBTN);
        verifyBTN = findViewById(R.id.verifyOtpBTN);
        otpET = findViewById(R.id.otpET);
        profileIV =findViewById(R.id.profileIV);
        fullnameET =findViewById(R.id.fullnameET);
        phoneET =findViewById(R.id.phoneET);
        addressET =findViewById(R.id.addressET);
        emailET =findViewById(R.id.emailET);
        passwordET =findViewById(R.id.passwordET);
        cpasswordET =findViewById(R.id.cpasswordET);
        registerBTN = findViewById(R.id.registerBTN);
        scrollView = findViewById(R.id.scrollView);
        verificationLayout = findViewById(R.id.otpVerificationView);


        //inside permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //locationpermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        firebaseAuth = firebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        /*
        if(checkLocationPermission()){

            detectLocation();
        }else{
            requestLocationPermission();
        }
         */

        //when back img click
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //just like back button
            }
        });

        verifyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validating if the OTP text field is empty or not.
                if (TextUtils.isEmpty(otpET.getText().toString())){
                    //if the OTP text field is empty display a message to user to enter OTP
                    Toast.makeText(RegisterRiderActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                }else{
                    //if OTP field is not empty calling method to verify the OTP.
                    verifyCode(otpET.getText().toString());

                }

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


    }   //Closing bracket of onCreate Bundle

    String fullName, shopName, phoneNumber, deliveryFee, address, email, password, confirmPassword;

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
                        Toast.makeText(RegisterRiderActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationCode(String number) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        //below method is used when OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            //when we recieve the OTP it contains a unique id wich we are storing in our string which we have already created.
            scrollView.setVisibility(View.GONE);
            verificationLayout.setVisibility(View.VISIBLE);
            verificationId = s;
        }

        //this method is called when user recieve OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //below line is used for getting OTP code which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();
            //checking if the code is null or not.
            if (code != null) {
                //if the code is not null then we are setting that code to our OTP edittext field.
                otpET.setText(code);
                //after setting this code to OTP edittext field we are calling our verifycode method.
                verifyCode(code);

            }

        }

        //thid method is called when firebase doesnot sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            //displaying error message with firebase exception.
            Toast.makeText(RegisterRiderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("error", e.getMessage());
        }
    };



    private void verifyCode(String code) {
        //below line is used for getting getting credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        //after getting credential we are calling sign in method.

       signInWithCredential(credential);

    }


    private void signInWithCredential(PhoneAuthCredential credential) {
        //inside this method we are checking if the code entered is correct or not.
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid",""+firebaseAuth.getUid());
                            hashMap.put("isNumberVerified","true");
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // db updated
                                            Intent i =new Intent(RegisterRiderActivity.this,LoginActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //db failed to update
                                            progressDialog.dismiss();
                                            startActivity(new Intent(RegisterRiderActivity.this, MainRiderActivity.class));
                                            finish();
                                            Toast.makeText(RegisterRiderActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });




                        } else {
                            //if the code is not correct then we are displaying an error message to the user.
                            Toast.makeText(RegisterRiderActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Uploading your information to our database...");

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
            // hashMap.put("latitude",""+latitude);
            // hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","Rider");
            hashMap.put("online","true");
            hashMap.put("riderAccepted","false");
            hashMap.put("isNumberVerified","false");
            hashMap.put("profileImage","");

            //Final setup: saving to database

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // db updated

                            Toast.makeText(RegisterRiderActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            sendVerificationCode(phoneET.getText().toString());
                            progressDialog.dismiss();
//                            startActivity(new Intent(RegisterRiderActivity.this, MainRiderActivity.class));
//                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //db failed to update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterRiderActivity.this, MainRiderActivity.class));
                            finish();
                            Toast.makeText(RegisterRiderActivity.this, "Failed", Toast.LENGTH_SHORT).show();


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
                                hashMap.put("address",""+address);
                                // hashMap.put("latitude",""+latitude);
                                // hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","Rider");
                                hashMap.put("online","true");
                                hashMap.put("riderAccepted","false");
                                hashMap.put("isNumberVerified","false");
                                hashMap.put("profileImage",""+downloadImageUri); ///convert image to url

                                //Final setup: saving to database

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // db updated

                                                Toast.makeText(RegisterRiderActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                sendVerificationCode(phoneET.getText().toString());
                                                progressDialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //db failed to update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterRiderActivity.this, MainSellerActivity.class));
                                                finish();

                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterRiderActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterRiderActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
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

                       image_uri = getImageUri(RegisterRiderActivity.this ,bitmap);

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

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

/*
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationpermissions, LOCATION_REQUEST_CODE);
    }

*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            /*
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Location Permission Denied ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
*/
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

    /*
    private void detectLocation() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);

    }
*/

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

/*
    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }


    private void findAddress() {

        Geocoder  geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude,1);

           String addressLocale = addresses.get(0).getAddressLine(0);

            addressET.setText(addressLocale);

        }catch (Exception e){

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
        //gps location disabled
        Toast.makeText(this, "Please turn on your Location", Toast.LENGTH_SHORT).show();
    }

    */

}   //Closing bracket of public class RegisterUserActivity
