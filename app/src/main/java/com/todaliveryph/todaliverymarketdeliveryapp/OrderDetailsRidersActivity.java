package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ScanOrder;

import java.util.Calendar;

public class OrderDetailsRidersActivity extends AppCompatActivity {

    String getCustomerName, getCustomerPhone, getCustomerAddress, getShopId, getOrderId,getDate,getStatus,getAmount;
    TextView customerNameTV,customerAddressTV,customerPhoneTV,shopNameTV,shopAddressTV,shopPhoneTV,orderIdTV,dateTV,statusTV,amountTV,noteTV;
    Button qrBtn;
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

        qrBtn = findViewById(R.id.scanQrBtn);

        loadShopInfo();
        loadOrderInfo();

        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderDetailsRidersActivity.this, ScanOrder.class);

                intent.putExtra("orderId",getOrderId);
                intent.putExtra("shopId",getShopId);

               startActivity(intent);
            }
        });
    }

    private void loadOrderInfo(){
        customerNameTV.setText(getCustomerName);
        customerAddressTV.setText(getCustomerAddress);
        customerPhoneTV.setText(getCustomerPhone);
        orderIdTV.setText(getOrderId);
        statusTV.setText(getStatus);
        amountTV.setText(getAmount);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(getDate));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        dateTV.setText(formatedDate);

        if (statusTV.getText().equals("Pending")){
            noteTV.setVisibility(View.VISIBLE);
           statusTV.setTextColor(this.getResources().getColor(R.color.colorPrimary));
        } else if (statusTV.getText().equals("Rider Accepted")){
            noteTV.setVisibility(View.VISIBLE);
            statusTV.setTextColor(this.getResources().getColor(R.color.teal_200));
        }
        else if(statusTV.getText().equals("Completed")){
            noteTV.setVisibility(View.GONE);
            qrBtn.setVisibility(View.GONE);
            statusTV.setTextColor(this.getResources().getColor(R.color.green));
        }
        else {
            noteTV.setVisibility(View.VISIBLE);
        }
    }

    private void loadShopInfo(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(getShopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("shopName").getValue();
                        String phone = ""+snapshot.child("phone").getValue();
                        String address = ""+snapshot.child("address").getValue();


                        shopNameTV.setText(name);
                        shopAddressTV.setText(phone);
                        shopPhoneTV.setText(address);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}