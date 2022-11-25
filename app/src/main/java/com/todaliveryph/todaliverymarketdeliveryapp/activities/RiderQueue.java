package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RiderQueue extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;
    String user, todaRoute;
    Button onlineBtn, deliverBtn;
    TextView routeTV, statusTV, timeTV;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_queue);

        timeTV = findViewById(R.id.timeTV);
        routeTV = findViewById(R.id.routeTV);
        statusTV = findViewById(R.id.statusTV);

        onlineBtn = findViewById(R.id.queueBtn);
        deliverBtn = findViewById(R.id.ordersBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        //      todaRoute= getIntent().getStringExtra("route");
        user = firebaseAuth.getUid();
        loadQueueInfo();


        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog();
                if (onlineBtn.getText().equals("MAKE ME ONLINE")) {
                    final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);

                    onlineBtn.setBackgroundResource(R.color.green);

                    databaseReference.child("Users").child(firebaseAuth.getUid()).child("queue").setValue("On Queue");
                    databaseReference.child("Users").child(firebaseAuth.getUid()).child("lastQueue").setValue(currentTimestamp);
                    databaseReference.child("queue").child(String.valueOf(routeTV.getText())).child(currentTimestamp).setValue(user);

                    Toast.makeText(RiderQueue.this, "Online!", Toast.LENGTH_SHORT).show();

                    onlineBtn.setText("CURRENTLY ON QUEUE");

                    timeTV.setText(currentTimestamp);


                } else if(onlineBtn.getText().equals("CURRENTLY ON QUEUE")){

                    AlertDialog.Builder builder = new AlertDialog.Builder(RiderQueue.this);
                    builder.setTitle("Queue");
                    builder.setMessage("Are you sure you want to remove your queue?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            onlineBtn.setBackgroundResource(R.color.colorPrimary);

                            databaseReference.child("Users").child(firebaseAuth.getUid()).child("queue").setValue("Stand By");
                            databaseReference.child("Users").child(firebaseAuth.getUid()).child("pendingOrder").setValue("false");
                            databaseReference.child("queue").child(String.valueOf(routeTV.getText())).child(String.valueOf(timeTV.getText())).removeValue();

                            Toast.makeText(RiderQueue.this, "Offline!", Toast.LENGTH_SHORT).show();
                            timeTV.setText("");
                            onlineBtn.setText("MAKE ME ONLINE");


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


            }
        });

        deliverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderQueue.this, RiderDeliver.class));
                finish();
            }
        });


    }

    private void loadQueueInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(user)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String getRoute = "" + ds.child("Toda").getValue(String.class);
                            String getStatus = "" + ds.child("queue").getValue(String.class);
                            String getQueue = "" + ds.child("lastQueue").getValue(String.class);
                            statusTV.setText(getStatus);
                            routeTV.setText(getRoute);

                            if (getStatus.equals("On Queue")) {
                                onlineBtn.setBackgroundResource(R.color.green);

                                onlineBtn.setText("CURRENTLY ON QUEUE");

                                timeTV.setText(getQueue);
                            } else {
                                onlineBtn.setBackgroundResource(R.color.colorPrimary);
                                timeTV.setText("");
                                onlineBtn.setText("MAKE ME ONLINE");
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void progressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Connecting to database");
        progressDialog.setCanceledOnTouchOutside(false);
    }


}