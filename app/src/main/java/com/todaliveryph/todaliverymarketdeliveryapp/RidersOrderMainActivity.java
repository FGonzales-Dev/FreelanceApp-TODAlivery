package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.LoginActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.MainRiderActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.MainUserActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.UsersOrderMainActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterOrderDriver;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterOrderUser;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderDriver;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderUser;

import java.util.ArrayList;

public class RidersOrderMainActivity extends AppCompatActivity {
    private ImageButton BTNback;
    private RecyclerView ordersRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelOrderDriver> ordersList;
    private AdapterOrderDriver adapterOrderDriver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riders_order_main);

        BTNback = findViewById(R.id.BTNback);
        ordersRv = findViewById(R.id.usersRv);
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();

        BTNback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RidersOrderMainActivity.this, MainRiderActivity.class));
                finish();
            }
        });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user ==null ){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
                        for (DataSnapshot ds: snapshot.getChildren()){
                            loadOrders();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadOrders() {
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverOrder");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid =""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverOrder").child(uid).child("orders") ;
                    ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            ModelOrderDriver modelOrderDriver =  ds.getValue(ModelOrderDriver.class);
                                            //add to list
                                            ordersList.add(modelOrderDriver);
                                        }
                                        //setup adapter
                                        adapterOrderDriver= new AdapterOrderDriver(RidersOrderMainActivity.this, ordersList);
                                        //setup recycler view
                                        ordersRv.setAdapter(adapterOrderDriver);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}