package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

public class RiderDeliver extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FirebaseAuth firebaseAuth;
    TextView sellerName, sellerAddress, sellerPhone, orderID, buyerName, buyerAddress, buyerPhone, driverName, amount, status, buyerRoute;
    String user;
    Button acceptBtn, declineBtn,scanQrBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_deliver);

        user = firebaseAuth.getInstance().getUid();

        sellerName = findViewById(R.id.sellerNameTV);
        sellerAddress = findViewById(R.id.sellerAddressTV);
        sellerPhone = findViewById(R.id.sellerPhoneTV);
        orderID = findViewById(R.id.orderIDTV);
        buyerName = findViewById(R.id.buyerNameTV);
        buyerAddress = findViewById(R.id.buyerAddressTV);
        buyerPhone = findViewById(R.id.buyerPhoneTV);
        driverName = findViewById(R.id.driverNameTV);
        amount = findViewById(R.id.amountTV);
        status = findViewById(R.id.statusTV);
        buyerRoute = findViewById(R.id.buyerRouteTV);

        acceptBtn = findViewById(R.id.acceptBtn);
        declineBtn = findViewById(R.id.declineBtn);

        loadDelivery();

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(RiderDeliver.this);
                builder.setTitle("Delivery");
                builder.setMessage("Are you sure you want to accept this order? The seller will now be notified");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        acceptOrder();
                        //    Toast.makeText(RiderDeliver.this,"You accepted the order, you are now the assigned driver!",Toast.LENGTH_SHORT).show();
                        removeFromQueue();
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


        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(RiderDeliver.this);
                builder.setTitle("Delivery");
                builder.setMessage("Are you sure you want to decline this order? You will be placed to last queue and need to queue again!");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeFromQueue();
                        //   Toast.makeText(RiderDeliver.this,"You declined the order, you need to queue again!",Toast.LENGTH_SHORT).show();
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


    }

    private void loadDelivery() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("driverOrder")) {
                    if (snapshot.child("driverOrder").hasChild(user)) {

                        DatabaseReference myRef = databaseReference.child("driverOrder").child(user).child("orders");
                        Query query = myRef.orderByKey().limitToLast(1);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    //  String getKey = childSnapshot.getKey();
                                    if (dataSnapshot.hasChildren()) {
                                        String getCustomerAddress = childSnapshot.child("customerAddress").getValue(String.class);
                                        String getCustomerName = childSnapshot.child("customerName").getValue(String.class);
                                        String getCustomerPhone = childSnapshot.child("customerPhone").getValue(String.class);
                                        String getItemsAmount = childSnapshot.child("itemsAmount").getValue(String.class);
                                        String getOrderID = childSnapshot.child("orderId").getValue(String.class);
                                        String getShopID = childSnapshot.child("shopId").getValue(String.class);
                                        String getStatus = childSnapshot.child("status").getValue(String.class);
                                        String getBuyerRoute = childSnapshot.child("route").getValue(String.class);

                                        orderID.setText(getOrderID);
                                        buyerName.setText(getCustomerName);
                                        buyerAddress.setText(getCustomerAddress);
                                        buyerPhone.setText(getCustomerPhone);
                                        buyerRoute.setText(getBuyerRoute);

                                        amount.setText(getItemsAmount);

                                        if (getStatus.equals("Pending")) {
                                            status.setText("Please respond to this order");
                                        }
                                        else if(getStatus.equals("Rider Accepted")){
                                            status.setText("You can now proceed to deliver the products");
                                        }
                                        showControls();
                                        loadShopInfo(getShopID);


                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                throw databaseError.toException();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopInfo(String shopID) {
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(shopID)) {

                    String getShopName = snapshot.child(shopID).child("name").getValue(String.class);
                    String getShopAddress = snapshot.child(shopID).child("address").getValue(String.class);
                    String getShopPhone = snapshot.child(shopID).child("phone").getValue(String.class);
                    String getDriverName = snapshot.child(user).child("name").getValue(String.class);

                    sellerName.setText(getShopName);
                    sellerAddress.setText(getShopAddress);
                    sellerPhone.setText(getShopPhone);
                    driverName.setText(getDriverName);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void acceptOrder() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("driverOrder")) {
                    if (snapshot.child("driverOrder").hasChild(user)) {
                        if (snapshot.child("driverOrder").child(user).child("orders").hasChild(orderID.getText().toString())) {
                            Toast.makeText(RiderDeliver.this, orderID.getText().toString(), Toast.LENGTH_SHORT).show();
                            DatabaseReference myRef = databaseReference.child("driverOrder").child(user).child("orders").child(orderID.getText().toString());
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    databaseReference.child("driverOrder").child(user).child("orders").child(orderID.getText().toString()).child("status").setValue("Rider Accepted");
                                    databaseReference.child("Users").child(user).child("riderAccepted").setValue("true");
                                    databaseReference.child("Users").child(user).child("lastQueue").setValue("");
                                    databaseReference.child("Users").child(user).child("onQueue").setValue("");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    throw databaseError.toException();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeFromQueue() {
        DatabaseReference myRef = databaseReference.child("queue").child(buyerRoute.getText().toString());
        Query query = myRef.orderByKey().limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String getKey = childSnapshot.getKey();
                        if (dataSnapshot.hasChildren()) {
                            //  String getDriverID = childSnapshot.getValue(String.class);
                            databaseReference.child("queue").child(buyerRoute.getText().toString()).child(getKey).removeValue();

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }
    private void showControls(){
        if (!(status.getText().toString().equals("Please respond to this order"))){
            acceptBtn.setVisibility(View.GONE);
            declineBtn.setVisibility(View.GONE);
        }
        else{
            scanQrBtn.setVisibility(View.VISIBLE);
        }

    }
}