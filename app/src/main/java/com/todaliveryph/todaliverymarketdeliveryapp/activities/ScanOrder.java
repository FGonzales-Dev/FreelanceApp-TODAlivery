package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanOrder extends AppCompatActivity {

    private Activity mActivity;
    private Context mContext;

    private ViewGroup contentFrame;
    private ZXingScannerView zXingScannerView;
    private ArrayList<Integer> mSelectedIndices;

    private FloatingActionButton flash, focus, camera,scanAgain;
    private boolean isFlash, isAutoFocus;
    private int camId, frontCamId, rearCamId;
    String getShopId,getOrderId,user,driverName;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_order);

        getShopId = getIntent().getStringExtra("shopId");
        getOrderId = getIntent().getStringExtra("orderId");

        user = FirebaseAuth.getInstance().getUid();
        initVar();
        zXingScannerView = new ZXingScannerView(mActivity);
        setupFormats();

        initView();
        initListener();
        activateScanner();
        initFunctionality();
        loadMyInfo();
    }
    private void initFunctionality() {
        if ((ContextCompat.checkSelfPermission( mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission( mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(
                    mActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISSION_REQ);
        }

    }

    private void initVar() {
        mActivity = this;
        mContext = mActivity.getApplicationContext();


        loadCams();
    }
    private void initView() {
        contentFrame = (ViewGroup) findViewById(R.id.content_frame);

        flash = (FloatingActionButton) findViewById(R.id.flash);
        camera = (FloatingActionButton)findViewById(R.id.camera);
        scanAgain = (FloatingActionButton)findViewById(R.id.scanAgain);

    }

    private void initListener() {

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlash();
            }
        });

        scanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zXingScannerView.startCamera(rearCamId);
                initListener();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCamera();
            }
        });

        zXingScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                String resultStr = result.getText();
                zXingScannerView.stopCameraPreview();
                completeOrderScan(resultStr);

            }
        });

    }

    private void activateScanner() {
        if(zXingScannerView != null) {

            if(zXingScannerView.getParent()!=null) {
                ((ViewGroup) zXingScannerView.getParent()).removeView(zXingScannerView); // to prevent crush on re adding view
            }
            contentFrame.addView(zXingScannerView);

            if(zXingScannerView.isActivated()) {
                zXingScannerView.stopCamera();
            }
            zXingScannerView.startCamera(camId);
            zXingScannerView.setFlash(isFlash);
            zXingScannerView.setAutoFocus(true);
        }
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<>();
        if(mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<>();
            for(int i = 0; i < ZXingScannerView.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for(int index : mSelectedIndices) {
            formats.add(ZXingScannerView.ALL_FORMATS.get(index));
        }
        if(zXingScannerView != null) {
            zXingScannerView.setFormats(formats);
        }
    }



    private void toggleFlash() {
        if (isFlash) {
            isFlash = false;
            flash.setImageResource(R.drawable.ic_flash_on);
        } else {
            isFlash = true;
        }
        zXingScannerView.setFlash(isFlash);
    }


    private void toggleCamera() {

        if (camId == rearCamId) {
            camId = frontCamId;
        } else {
            camId = rearCamId;
        }
        zXingScannerView.stopCamera();
        zXingScannerView.startCamera(camId);
    }


    private void loadCams() {
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCamId = i;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                rearCamId = i;
            }
        }

    }

    private void loadMyInfo(){
        driverName ="";
        databaseReference.child("Users").child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String getDriverName = snapshot.child("name").getValue(String.class);
                    driverName = getDriverName;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void completeOrderScan(String orderId){
        if(orderId.equals(getOrderId)){
            databaseReference.child("Users").child(getShopId).child("Orders").child(orderId).child("orderStatus").setValue("Completed");
            databaseReference.child("driverOrder").child(user).child("orders").child(orderId).child("status").setValue("Completed");
            Toast.makeText(ScanOrder.this,"Successfully Completed the order!", Toast.LENGTH_LONG).show();
            prepareNotificationMessage();
            finish();
        }
        else{
            Toast.makeText(ScanOrder.this,"QR Code not found! Try again", Toast.LENGTH_LONG).show();
        }

    }

    private void prepareNotificationMessage(){
        //when seller changed order status, send notif to buyer

        //prepare data  for notif

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE ="Rider Update from "+driverName;
        String NOTIFICATION_MESSAGE =  "Rider successfully delivered the item(s)";
        String NOTIFICATION_TYPE = "RiderCompleteOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            // mga sinesend
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("riderUid",getShopId);
            notificationBodyJo.put("sellerUid",user);
            notificationBodyJo.put("orderId",getOrderId+" successfully delivered");
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //saan i sesend
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        })
        {
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



}