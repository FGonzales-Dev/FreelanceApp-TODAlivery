package com.todaliveryph.todaliverymarketdeliveryapp.activities;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.util.ArrayList;
import java.util.List;

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
    String getShopId,getOrderId,user;
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

    private void completeOrderScan(String orderId){
        if(orderId.equals(getOrderId)){
            databaseReference.child("Users").child(getShopId).child("Orders").child(orderId).child("orderStatus").setValue("Completed");
            databaseReference.child("driverOrder").child(user).child("orders").child(orderId).child("status").setValue("Completed");
            Toast.makeText(ScanOrder.this,"Successfully Completed the order!", Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            Toast.makeText(ScanOrder.this,"QR Code not found! Try again", Toast.LENGTH_LONG).show();
        }

    }


}