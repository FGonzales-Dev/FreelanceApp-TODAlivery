package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    RelativeLayout RlViewProfile, RlsignOut,RlVerification;
    TextView notificationStatusTv,phoneVerifyDetails;
    ImageButton backBTN;
    SwitchCompat fcmSwitch;
    ProgressDialog progressDialog;


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
        RlVerification= findViewById(R.id.RlVerification);
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
                            if (accountType.equals("Rider")){
                                RlVerification.setVisibility(View.VISIBLE);
                                if (isNumberVerified.equals("true")) {
                                    phoneVerifyDetails.setText("VERIFIED");
                                    RlVerification.setClickable(false);

                                } else {
                                    phoneVerifyDetails.setText("NOY YET VERIFIED");
                                    RlVerification.setClickable(true);
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });}


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