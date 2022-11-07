package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.util.HashMap;
import java.util.Random;

public class RiderPhoneVerificationActivity extends AppCompatActivity {
    private Button verifyBTN;
    private EditText otpET;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String phone;
    public static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 600;
    int otp = new Random().nextInt(900000) + 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_phone_verification);
        verifyBTN = findViewById(R.id.verifyOtpBTN);
        otpET = findViewById(R.id.otpET);
        firebaseAuth = firebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        sendVerificationCode();

        verifyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Verifying");
                progressDialog.show();
                //validating if the OTP text field is empty or not.
                if (TextUtils.isEmpty(otpET.getText().toString())){
                    //if the OTP text field is empty display a message to user to enter OTP
                    Toast.makeText(RiderPhoneVerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                }else{
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot ds: snapshot.getChildren()){
                                        String otpFromDb =""+ds.child("otp").getValue();
                                        if (otpFromDb.equals(otpET.getText().toString())){

                                            Toast.makeText(RiderPhoneVerificationActivity.this,"You are verified", Toast.LENGTH_SHORT).show();

                                            startActivity(new Intent(RiderPhoneVerificationActivity.this, MainRiderActivity.class));
                                            finish();


                                            HashMap<String, Object> hashMap = new HashMap<>();

                                            hashMap.put("isNumberVerified","true");
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            // db updated
                                                            startActivity(new Intent(RiderPhoneVerificationActivity.this, MainRiderActivity.class));
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            //db failed to update
                                                            progressDialog.dismiss();
                                                            startActivity(new Intent(RiderPhoneVerificationActivity.this, MainRiderActivity.class));
                                                            finish();
                                                            Toast.makeText(RiderPhoneVerificationActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });


                                            progressDialog.dismiss();
                                        }else {
                                            Toast.makeText(RiderPhoneVerificationActivity.this,"Otp is not match", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(RiderPhoneVerificationActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });


                }


            }
        });
    }


    private void sendVerificationCode() {

        progressDialog.show();
        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("otp",otp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Updated
                        progressDialog.dismiss();
                        ActivityCompat.requestPermissions(RiderPhoneVerificationActivity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                MY_PERMISSIONS_REQUEST_SEND_SMS);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed Show error message
                        Toast.makeText(RiderPhoneVerificationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadMyInfo() {
        progressDialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            progressDialog.dismiss();
                            phone = ""+ds.child("phone").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();

                    smsManager.sendTextMessage(phone,null,"Otp for verification is: "+otp,null,null);

                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();


                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }
}

