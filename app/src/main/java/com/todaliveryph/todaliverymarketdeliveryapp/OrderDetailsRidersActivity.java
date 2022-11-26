package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsSellerActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.RiderDeliver;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ScanOrder;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ShopDetailsActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.MessageActivity;

import java.util.Calendar;

public class OrderDetailsRidersActivity extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");

    FirebaseAuth firebaseAuth;
    String getCustomerName, getCustomerPhone, getCustomerAddress, getShopId, getOrderId, getDate, getStatus, getAmount,getCustomerId,getDeliveryFee,getTotalCost;
    TextView customerNameTV,customerIDTV, customerAddressTV, customerPhoneTV, shopNameTV, shopAddressTV, shopPhoneTV, orderIdTV, dateTV, statusTV, amountTV, noteTV,deliveryFeeTV,totalCostTV;
    Button chatCustomer,qrBtn;
    ImageView backBtn;
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
        getDeliveryFee = getIntent().getStringExtra("deliveryFee");
        getTotalCost = getIntent().getStringExtra("totalCost");
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
        deliveryFeeTV = findViewById(R.id.deliveryFeeTv);
        totalCostTV = findViewById(R.id.totalCostTv);
        noteTV = findViewById(R.id.noteTV);

        chatCustomer = findViewById(R.id.chatBtn);
        qrBtn = findViewById(R.id.qrBtn);
        backBtn = findViewById(R.id.backBtn);

        loadShopInfo();
        loadOrderInfo();

        chatCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();


            }
        });

        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OrderDetailsRidersActivity.this, ScanOrder.class);

                intent.putExtra("orderId", getOrderId);
                intent.putExtra("shopId",  getShopId);

                startActivity(intent);
                finish();

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadOrderInfo() {
        customerNameTV.setText(getCustomerName);
        customerAddressTV.setText(getCustomerAddress);
        customerPhoneTV.setText(getCustomerPhone);
        orderIdTV.setText(getOrderId);
        statusTV.setText(getStatus);
        amountTV.setText("₱ "+getAmount);
        deliveryFeeTV.setText("₱ "+getDeliveryFee);
        totalCostTV.setText("₱ "+getTotalCost);


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
            chatCustomer.setVisibility(View.VISIBLE);
            qrBtn.setVisibility(View.VISIBLE);
        } else if (statusTV.getText().equals("Completed")) {
            noteTV.setVisibility(View.GONE);
            statusTV.setTextColor(this.getResources().getColor(R.color.green));
            chatCustomer.setVisibility(View.GONE);
            qrBtn.setVisibility(View.GONE);
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

    private void dialPhone(){
        Intent intent = new Intent(OrderDetailsRidersActivity.this, MessageActivity.class);
        intent.putExtra("name", getCustomerName);
        intent.putExtra("receiverID",getCustomerId);
        startActivity(intent);
    }



}