package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.MessagesActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.RidersOrderMainActivity;

public class MainRiderActivity extends AppCompatActivity {

    private TextView nameTV;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    String statusForQueue;
    CardView clickedQueue, clickedChat, clickedProfile, clickedOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rider);



        //CardViews
        //Menu declaring cardviews
        clickedQueue =(CardView) findViewById(R.id.clickedQueue);
        clickedQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusForQueue.equals("true")){
                    startActivity(new Intent(MainRiderActivity.this, RiderQueue.class));
                }
                else{
                    notVerified();
                    Toast.makeText(MainRiderActivity.this,"Verified your account first before taking orders", Toast.LENGTH_SHORT).show();
                }


            }
        });
        clickedChat =(CardView) findViewById(R.id.clickedRiders);
        clickedChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainRiderActivity.this, MessagesActivity.class));
            }
        });
        clickedProfile =(CardView) findViewById(R.id.clickedProfile);
        clickedProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainRiderActivity.this, SettingsActivity.class));
            }
        });
        clickedOrders =(CardView) findViewById(R.id.clickedAdmins);
        clickedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainRiderActivity.this, RidersOrderMainActivity.class));
            }
        });


        nameTV = findViewById(R.id.nameTV);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
       firebaseAuth = FirebaseAuth.getInstance();


       checkUser();


    }// closing onCreate


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainRiderActivity.this, LoginActivity.class));
            finish();
        }else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        statusForQueue ="";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){

                            String name =""+ds.child("name").getValue();
                            String status = ""+ds.child("riderAccepted").getValue();
                            String numberVerified = ""+ds.child("isNumberVerified").getValue();
                            statusForQueue = status;
                            if(status.equals("false") || numberVerified.equals("false")){
                                status = "Unverified";

                            }
                            else{
                                status = "Verified";
                            }

                            nameTV.setText("Welcome, "+ name + " (" +status+ ")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void onBackPressed () {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void notVerified(){

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final VibrationEffect vibrationEffect1;

        // this is the only type of the vibration which requires system version Oreo (API 26)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // this effect creates the vibration of default amplitude for 1000ms(1 sec)
            vibrationEffect1 = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
            // it is safe to cancel other vibrations currently taking place
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect1);
        }
    }

}//Public Class closing