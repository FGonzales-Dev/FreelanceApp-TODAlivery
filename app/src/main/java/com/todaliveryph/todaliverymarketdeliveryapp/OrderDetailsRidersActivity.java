package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.MessageActivity;

import java.util.Calendar;

public class OrderDetailsRidersActivity extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");

    FirebaseAuth firebaseAuth;
    String getCustomerName, getCustomerPhone, getCustomerAddress, getShopId, getOrderId, getDate, getStatus, getAmount,getCustomerId;
    TextView customerNameTV,customerIDTV, customerAddressTV, customerPhoneTV, shopNameTV, shopAddressTV, shopPhoneTV, orderIdTV, dateTV, statusTV, amountTV, noteTV;

    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_riders);

        getCustomerName = getIntent().getStringExtra("customerName");
        getCustomerPhone = getIntent().getStringExtra("customerPhone");
        getCustomerAddress = getIntent().getStringExtra("customerAddress");
        getShopId = getIntent().getStringExtra("shopId");
        getOrderId = getIntent().getStringExtra("orderId");
        getDate = getIntent().getStringExtra("date");
        getStatus = getIntent().getStringExtra("status");
        getAmount = getIntent().getStringExtra("amount");
        getCustomerId = getIntent().getStringExtra("buyerId");

        user = firebaseAuth.getInstance().getUid();

        customerNameTV = findViewById(R.id.buyerNameTv);
        customerAddressTV = findViewById(R.id.buyerAddressTv);
        customerPhoneTV = findViewById(R.id.buyerPhoneTv);
        shopNameTV = findViewById(R.id.shopNameTv);
        shopAddressTV = findViewById(R.id.shopAddressTv);
        shopPhoneTV = findViewById(R.id.shopPhoneTv);
        orderIdTV = findViewById(R.id.orderIdTv);
        dateTV = findViewById(R.id.dateTv);
        statusTV = findViewById(R.id.orderStatusTv);
        amountTV = findViewById(R.id.amountTv);
        noteTV = findViewById(R.id.noteTV);




        loadShopInfo();
        loadOrderInfo();


    }

    private void loadOrderInfo() {
        customerNameTV.setText(getCustomerName);
        customerAddressTV.setText(getCustomerAddress);
        customerPhoneTV.setText(getCustomerPhone);
        orderIdTV.setText(getOrderId);
        statusTV.setText(getStatus);
        amountTV.setText(getAmount);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(getDate));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        dateTV.setText(formatedDate);

        if (statusTV.getText().equals("Pending")) {
            noteTV.setVisibility(View.VISIBLE);
            statusTV.setTextColor(this.getResources().getColor(R.color.colorPrimary));
        } else if (statusTV.getText().equals("Rider Accepted")) {
            noteTV.setVisibility(View.VISIBLE);
            statusTV.setTextColor(this.getResources().getColor(R.color.teal_200));
        } else if (statusTV.getText().equals("Completed")) {
            noteTV.setVisibility(View.GONE);
            statusTV.setTextColor(this.getResources().getColor(R.color.green));
        } else {
            noteTV.setVisibility(View.VISIBLE);
        }
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(getShopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("shopName").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String address = "" + snapshot.child("address").getValue();


                        shopNameTV.setText(name);
                        shopAddressTV.setText(phone);
                        shopPhoneTV.setText(address);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void dialPhone() {
        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {

                for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {

                    if (dataSnapshot1.hasChild("users_1") && dataSnapshot1.hasChild("users_2")) {

                        final String getUserOne = dataSnapshot1.child("users_1").getValue(String.class);
                        final String getUserTwo = dataSnapshot1.child("users_2").getValue(String.class);

                        if ((getUserOne.equals(getCustomerId) || getUserTwo.equals(getCustomerId))
                                && (getUserOne.equals(user) || getUserTwo.equals(user))) {
                            final String getKey = dataSnapshot1.getKey();

                            Intent intent = new Intent(OrderDetailsRidersActivity.this, MessageActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("mobile", getCustomerId);
                            intent.putExtra("name", getCustomerName);
                            intent.putExtra("profileImage", "");
                            intent.putExtra("chat_key", getKey);

                            startActivity(intent);
                        } else{
                            Intent intent = new Intent(OrderDetailsRidersActivity.this, MessageActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("mobile", getCustomerId);
                            intent.putExtra("name", getCustomerName);
                            intent.putExtra("profileImage", "");
                            intent.putExtra("chat_key", "");

                            startActivity(intent);
                        }


                    }


                }

                if (!snapshot1.hasChildren()) {
                    Intent intent = new Intent(OrderDetailsRidersActivity.this, MessageActivity.class);

                    intent.putExtra("mobile", getCustomerId);
                    intent.putExtra("name", getCustomerName);
                    intent.putExtra("profileImage", "");
                    intent.putExtra("chat_key", "");
                    startActivity(intent);
                }





            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}