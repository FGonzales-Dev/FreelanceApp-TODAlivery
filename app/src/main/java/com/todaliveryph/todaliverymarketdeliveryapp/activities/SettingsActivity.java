package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    RelativeLayout RlViewProfile, RlsignOut,RlVerification,RlIdVerification;
    TextView notificationStatusTv,phoneVerifyDetails,IdVerifyDetails;
    ImageButton backBTN;
    ImageView id_image;
    SwitchCompat fcmSwitch;
    ProgressDialog progressDialog;



    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;
    //image pick uri
    private Uri image_uri;
    private Bitmap bitmap;



    //permission arrays
    private String[]cameraPermissions;
    private String[]storagePermissions;


    FirebaseAuth firebaseAuth;
    private boolean isChecked=false;
    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public static final String enabledMessage = "Notifications are enabled";
    public static final String disabledMessage = "Notifications are disabled";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        phoneVerifyDetails= findViewById(R.id.phoneVerifyDetails);
        IdVerifyDetails = findViewById(R.id.IdVerifyDetails);
        RlVerification= findViewById(R.id.RlVerification);

        RlIdVerification = findViewById(R.id.RlIdVerification);
        RlViewProfile = findViewById(R.id.RlViewProfile);
        RlsignOut = findViewById(R.id.RlsignOut);
        backBTN = findViewById(R.id.backBTN);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        checkUser();
        checkRider();

        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        isChecked = sp.getBoolean("FCM_ENABLED",false);
        fcmSwitch.setChecked(isChecked);


        if (isChecked){
            notificationStatusTv.setText(enabledMessage);
        }else {
            notificationStatusTv.setText(disabledMessage);
        }

        RlViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccountType();
                checkUserType();
            }
        });


        RlIdVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImagePicDialog();
            }
        });
        RlVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SettingsActivity.this, RiderPhoneVerificationActivity.class));
                finish();

            }
        });

        RlsignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        makeMeOffline();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccountType();
                checkMainWho();
            }
        });

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    subscribeToTopic();
                }else{
                    unSubscribeToTopic();
                }
            }
        });

    }//closing on create

    private void makeMeOffline() {


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update the value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success update

                        firebaseAuth.signOut();
                        progressDialog.setMessage("Logging out...");
                        progressDialog.show();
                        progressDialog.dismiss();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //update failed
                    }
                });

    }// closing make offline
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

    private ActivityResultLauncher galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        //image picked
                        Intent data = result.getData();
                        image_uri = data.getData();
                        uploadId();

                    }else{
                        //cancelled
                        Toast.makeText(SettingsActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
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

                        image_uri = getImageUri(SettingsActivity.this ,bitmap);



                        uploadId();
                    }
                }
            }
    );

    private void uploadId(){
        progressDialog.setMessage("Verifying");
        progressDialog.show();
        String filepathAndName = "id_image/"+""+firebaseAuth.getUid();
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
                            hashMap.put("IdImage",""+downloadImageUri); ///convert image to url

                            //Final setup: saving to database

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Toast.makeText(SettingsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //db failed to update
                                            progressDialog.dismiss();
                                            startActivity(new Intent(SettingsActivity.this, MainSellerActivity.class));
                                            finish();

                                        }
                                    });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkMainWho() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String accountType =""+ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                startActivity(new Intent(SettingsActivity.this, MainSellerActivity.class));
                                finish();
                            }
                            else if (accountType.equals("Admin")){
                                startActivity(new Intent(SettingsActivity.this, MainAdminActivity.class));
                                finish();
                            }
                            else if (accountType.equals("Rider")){
                                startActivity(new Intent(SettingsActivity.this, MainRiderActivity.class));
                                finish();
                            }
                            else{
                                startActivity(new Intent(SettingsActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkAccountType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    private void checkRider() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String accountType =""+ds.child("accountType").getValue();
                            String isNumberVerified =""+ds.child("isNumberVerified").getValue();
                            String isIdVerified =""+ds.child("riderAccepted").getValue();
                            if (accountType.equals("Rider")){
                                RlVerification.setVisibility(View.VISIBLE);
                                RlIdVerification.setVisibility(View.VISIBLE);
                                if (isNumberVerified.equals("true")) {
                                    phoneVerifyDetails.setText("VERIFIED");
                                    RlVerification.setClickable(false);
                                } else {
                                    phoneVerifyDetails.setText("NOT YET VERIFIED");
                                    RlVerification.setClickable(true);
                                }


                                if (isIdVerified.equals("true")) {
                                    IdVerifyDetails.setText("VERIFIED");
                                    RlIdVerification.setClickable(false);
                                } else {
                                    RlIdVerification.setClickable(true);
                                    IdVerifyDetails.setText("NOT YET VERIFIED");
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


        private void checkUserType() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String accountType =""+ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                startActivity(new Intent(SettingsActivity.this, SellerProfileActivity.class));
                                finish();
                            }
                            else if (accountType.equals("Admin")){
                                startActivity(new Intent(SettingsActivity.this, AdminProfileActivity.class));
                                finish();
                            }else{
                                startActivity(new Intent(SettingsActivity.this, UserProfileActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //save setting inside shared preference
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", true);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", false);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checkAccountType();
        checkMainWho();
    }


}//closing public class