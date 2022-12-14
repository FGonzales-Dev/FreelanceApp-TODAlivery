package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderDriver extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");

    FirebaseAuth firebaseAuth;
    String getRoute, getOrderID, getCustomerName, getOrderAmount, getCustomerAddress, getCustomerID, getCustomerPhone,getTotalOrderAmount,getDeliveryFee;
    TextView driverName, driverNumber, driverStatus, driverRoute, driverStatusNote, driverID;
    Button informBtn;
    String user,getRiderPendingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_driver);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getUid();
        getRoute = getIntent().getStringExtra("route");
        getOrderID = getIntent().getStringExtra("orderID");
        getCustomerName = getIntent().getStringExtra("name");
        getCustomerAddress = getIntent().getStringExtra("address");
        getCustomerPhone = getIntent().getStringExtra("phone");
        getOrderAmount = getIntent().getStringExtra("amount");
        getCustomerID = getIntent().getStringExtra("buyerID");
        getTotalOrderAmount = getIntent().getStringExtra("totalAmount");
        getDeliveryFee = getIntent().getStringExtra("deliveryFee");

        driverName = findViewById(R.id.driverNameTV);
        driverNumber = findViewById(R.id.driverNumberTV);
        driverStatus = findViewById(R.id.driverStatusTV);
        driverRoute = findViewById(R.id.driverRouteTV);
        driverID = findViewById(R.id.driverIDTV);
        driverStatus = findViewById(R.id.driverStatusTV);
        driverStatusNote = findViewById(R.id.driverStatusNoteTV);

        informBtn = findViewById(R.id.informBtn);

        getFirstQueue();
        controlButton();

        informBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getRiderPendingOrder.equals("false")){
                    driverStatus.setText("Pending");
                    driverStatusNote.setVisibility(View.VISIBLE);
                    Toast.makeText(OrderDriver.this, "Rider has been informed! Wait for respond", Toast.LENGTH_SHORT).show();
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("shopId").setValue(firebaseAuth.getUid());
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("orderId").setValue(getOrderID);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("customerName").setValue(getCustomerName);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("customerAddress").setValue(getCustomerAddress);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("itemsAmount").setValue(getOrderAmount);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("itemsDeliveryFee").setValue(getDeliveryFee);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("itemsTotalCost").setValue(getTotalOrderAmount);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("customerPhone").setValue(getCustomerPhone);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("status").setValue("Pending");
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("route").setValue(getRoute);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("orderTime").setValue(getOrderID);
                    databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID).child("customerId").setValue(getCustomerID);
                    databaseReference.child("Users").child(driverID.getText().toString()).child("pendingOrder").setValue("true");
                    prepareNotificationMessage();
                }
                else{
                    vibrate();
                    Toast.makeText(OrderDriver.this, "Rider has pending order. Wait until the queue refreshed!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void getFirstQueue() {
        getRiderPendingOrder ="";
        DatabaseReference myRef = databaseReference.child("queue").child(getRoute);
        Query query = myRef.orderByKey().limitToFirst(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        //  String getKey = childSnapshot.getKey();
                        if (dataSnapshot.hasChildren()) {
                            String getDriverID = childSnapshot.getValue(String.class);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(getDriverID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String name = "" + snapshot.child("name").getValue();
                                            String phone = "" + snapshot.child("phone").getValue();
                                            String route = "" + snapshot.child("Toda").getValue();
                                            String pendingRiderOrder = "" + snapshot.child("pendingOrder").getValue();
                                            getRiderPendingOrder = pendingRiderOrder;
                                            driverName.setText(name);
                                            driverNumber.setText(phone);
                                            driverRoute.setText(route);
                                            driverID.setText(getDriverID);
                                            checkStatus();
                                            controlButton();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }
                }else{
                    driverName.setText("No driver yet");
                    driverNumber.setText("Empty");
                    driverRoute.setText("Empty");
                    driverID.setText("");
                    driverStatus.setText("");
                    driverStatusNote.setText("");
                    controlButton();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }

    private void checkStatus() {
        DatabaseReference myRef = databaseReference.child("driverOrder").child(driverID.getText().toString()).child("orders").child(getOrderID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    if (dataSnapshot.child("status").getValue().equals("Pending")) {
                        driverStatus.setText("Pending");
                        driverStatusNote.setVisibility(View.VISIBLE);
                    }
                    if (dataSnapshot.child("status").getValue().equals("Rider Accepted")) {
                        finish();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private void controlButton() {
        if (!driverName.getText().toString().equals("No driver yet")) {
            informBtn.setVisibility(View.VISIBLE);
        }
        else{
            informBtn.setVisibility(View.GONE);
        }
        if (driverStatus.getText().equals("Driver Accepted")) {
            informBtn.setText("CONTACT DRIVER");
        }
    }

    private void prepareNotificationMessage() {
        //when seller changed order status, send notif to buyer

        //prepare data  for notif

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New order in your location ";
        String NOTIFICATION_MESSAGE = getOrderID;
        String NOTIFICATION_TYPE = "Rider";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            // mga sinesend
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("riderUid", driverID.getText().toString());
            notificationBodyJo.put("sellerUid", firebaseAuth.getUid());
            notificationBodyJo.put("orderId", getOrderID);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //saan i sesend
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {

        //send volley request (dependencies)

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // send failed
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);
                return headers;
            }
        };

        //Enque volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void vibrate(){

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

}