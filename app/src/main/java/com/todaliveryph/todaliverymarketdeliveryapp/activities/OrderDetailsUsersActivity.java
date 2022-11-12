package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterOrderedItem;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderUser;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderedItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsUsersActivity extends AppCompatActivity {

    private String orderTo, orderId;
    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv ,dateTv ,orderStatusTv, shopNameTv,totalItemsTv,amountTv,addressTv;
    private RecyclerView itemsRv;
    private ImageView QrCodeIv;
    private Button btnCancelOrder;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");
        backBtn =findViewById(R.id.backBtn);
        writeReviewBtn =findViewById(R.id.writeReviewBtn);
        orderIdTv =findViewById(R.id.orderIdTv);
        dateTv =findViewById(R.id.dateTv);
        orderStatusTv =findViewById(R.id.orderStatusTv);
        shopNameTv =findViewById(R.id.shopNameTv);
        totalItemsTv =findViewById(R.id.totalItemsTv);
        amountTv =findViewById(R.id.amountTv);
        addressTv =findViewById(R.id.addressTv);
        itemsRv =findViewById(R.id.itemsRv);
        QrCodeIv = findViewById(R.id.QrCodeIv);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyinfo();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();



        btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsUsersActivity.this);

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to decline or cancel this order?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("orderStatus", ""+"Cancelled");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(orderTo).child("Orders").child(orderId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        String message =  "Order is now "+"Cancelled";
                                        Toast.makeText(OrderDetailsUsersActivity.this, message , Toast.LENGTH_SHORT).show();
                                        prepareNotificationMessage(orderId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(OrderDetailsUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        //*/**/*/*/*/*/
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                //
            }
        });

       backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(OrderDetailsUsersActivity.this, UsersOrderMainActivity.class));
                finish();
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailsUsersActivity.this, WriteReviewActivity.class );
                intent1.putExtra("shopUid",orderTo);
                startActivity(intent1);
            }
        });

    }//closing create bundle


    private void loadMyinfo() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
        ref1.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String address = ""+ds.child("address").getValue();
                            addressTv.setText(address);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){

                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            orderedItemArrayList.add(modelOrderedItem); // all items added
                        }
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this, orderedItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set items count
                        totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadOrderDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else if(orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.green));
                            btnCancelOrder.setVisibility(View.INVISIBLE);
                        } else if(orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.color_Red));
                            btnCancelOrder.setVisibility(View.INVISIBLE);
                        }

                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("₱ "+orderCost);
                        dateTv.setText(formatedDate);

                        String generatedQrText = orderIdTv.getText().toString().trim();
                        MultiFormatWriter writer =  new MultiFormatWriter();
                        try {
                            BitMatrix matrix =  writer.encode(generatedQrText, BarcodeFormat.QR_CODE,150, 150);
                            BarcodeEncoder encoder =  new BarcodeEncoder();
                            Bitmap bitmap = encoder.createBitmap(matrix);
                            QrCodeIv.setImageBitmap(bitmap);
                            InputMethodManager manager =  (InputMethodManager)getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId){
        //when user placed order, send notif to seller

        //prepare data  for notif

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE ="Order:  "+ orderId + " was cancelled by buyer";
        String NOTIFICATION_MESSAGE = "Order: "+ orderId+" was cancelled by buyer";
        String NOTIFICATION_TYPE = "NewOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            // mga sinesend
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid()); // para ung buyer id is recognized as buyer ah yeah
            notificationBodyJo.put("sellerUid",orderTo);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //saan i sesend
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }
    private void sendFcmNotification(JSONObject notificationJo, String orderId) {

        //send volley request (dependencies)

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after send fcm start order details activity
                //open order page to see details:

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // if failed, still start order activity

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //Enque volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}//closing public class