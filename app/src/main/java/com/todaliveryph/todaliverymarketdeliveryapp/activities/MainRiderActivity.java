package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

public class MainRiderActivity extends AppCompatActivity {

    private TextView nameTV;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    CardView clickedBuy, clickedChat, clickedProfile, clickedOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rider);



        //CardViews
        //Menu declaring cardviews
        clickedBuy =(CardView) findViewById(R.id.clickedUsers);
        clickedBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        clickedChat =(CardView) findViewById(R.id.clickedRiders);
        clickedChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){

                            String name =""+ds.child("name").getValue();
                            String status = ""+ds.child("riderAccepted").getValue();
                            if(status.equals("false")){
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

}//Public Class closing