package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterProductSeller;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelProduct;

import java.util.ArrayList;

public class SellerSaleReportActivity extends AppCompatActivity {
    ImageButton backBtn;
    ImageView picIV;
    TextView shopNameTV, addressTV, revenueTV, itemSoldTV, itemQuantityTV, successTV, failedTV;
    double revenue;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_sale_report);

        shopNameTV = findViewById(R.id.shopnameSTV);
        backBtn = findViewById(R.id.backBTN);
        picIV = findViewById(R.id.picIV);
        addressTV = findViewById(R.id.addressSTV);
        revenueTV = findViewById(R.id.revenueValueTV);
        itemSoldTV = findViewById(R.id.soldValueTV);
        itemQuantityTV = findViewById(R.id.itemsQuantityTV);
        successTV = findViewById(R.id.successValueTV);
        failedTV = findViewById(R.id.failedValueTV);
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadReport();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }



    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String uid = "" + ds.child("").getValue();
                            String email = "" + ds.child("email").getValue();
                            String name = "" + ds.child("name").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String fee = "" + ds.child("deliveryFee").getValue();
                            String address = "" + ds.child("address").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String online = "" + ds.child("online").getValue();
                            String shopOpen = "" + ds.child("shopOpen").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            shopNameTV.setText(shopName);
                            addressTV.setText(address);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.add_profile).into(picIV);
                            } catch (Exception e) {
                                picIV.setImageResource(R.drawable.add_profile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadReport() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count= snapshot.getChildrenCount();
                        String quantity = String.valueOf(count);
                        itemQuantityTV.setText(quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count= snapshot.getChildrenCount();
                        String quantity = String.valueOf(count);
                        itemSoldTV.setText(quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ref.child(firebaseAuth.getUid()).child("Orders").orderByChild("orderStatus").equalTo("Completed")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count= snapshot.getChildrenCount();
                        String quantity = String.valueOf(count);
                        successTV.setText(quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ref.child(firebaseAuth.getUid()).child("Orders").orderByChild("orderStatus").equalTo("Cancelled")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count= snapshot.getChildrenCount();
                        String quantity = String.valueOf(count);
                        failedTV.setText(quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        revenue = 0.00;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String status = ds.child("orderStatus").getValue(String.class);
                            if(status.equals("Completed")){
                                String orderCost = ds.child("orderCost").getValue(String.class);
                               Double revenueConverted= Double.parseDouble(orderCost);
                                Toast.makeText(SellerSaleReportActivity.this, String.valueOf(revenueConverted), Toast.LENGTH_SHORT).show();
                                revenue +=revenueConverted;
                            }
                        }
                        revenueTV.setText(String.valueOf(revenue));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}