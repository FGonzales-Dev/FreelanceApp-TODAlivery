package com.todaliveryph.todaliverymarketdeliveryapp.chats;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.MessagesActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.MainUserActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.SellerAddProductActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.messages.Users;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");

    MessageAdapter chatAdapter;
    List<Chat> mchat;
    ImageView btnSend,imgSend;
    EditText txtMessage;
    String user;
    ImageView backBtn,imgPreview;
    String getReceiver,senderName,uri;
    String getName;
    String getProfilePic;
    RecyclerView recyclerView;

    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;

    private Uri image_uri;
    private String[]cameraPermissions;
    private String[]storagePermissions;
    private Bitmap bitmap;
    private static final int PICK_IMAGE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView =findViewById(R.id.chattingRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        user = FirebaseAuth.getInstance().getUid();
        btnSend= findViewById(R.id.sendBtn);
        imgSend= findViewById(R.id.imageBtn);
        txtMessage = findViewById(R.id.messageET);
        imgPreview = findViewById(R.id.imgPreview);

        backBtn = findViewById(R.id.backBtn);
        final CircleImageView profilePic = findViewById(R.id.profilePic);
        final TextView nameTV = findViewById(R.id.name);
        getReceiver = getIntent().getStringExtra("receiverID");
        getName = getIntent().getStringExtra("name");

        uri = getIntent().getStringExtra("uri");

        nameTV.setText(getName);



        databaseReference.child("Users").child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        databaseReference.child("Users").child(getReceiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getProfilePic = snapshot.child("profileImage").getValue(String.class);
                try{
                    Picasso.get().load(getProfilePic).into(profilePic);
                }catch (Exception e){
                    profilePic.setImageResource(R.drawable.user_icon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        readMessages(user, getReceiver);



        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtMessage.isShown()){
                    String msg = txtMessage.getText().toString();
                    if(!msg.equals("")){
                        sendMessage(user,getReceiver,msg);
                        Toast.makeText(MessageActivity.this,"Sent!",Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(msg);
                    }
                    else{
                        Toast.makeText(MessageActivity.this,"You can't send empty message!",Toast.LENGTH_SHORT).show();
                    }
                    txtMessage.setText("");
                }
                else{
                    sendImgMessage(user,getReceiver);
                    Toast.makeText(MessageActivity.this,"Sent!",Toast.LENGTH_SHORT).show();
                    prepareNotificationMessage("Sent an image!");
                    imgPreview.setVisibility(View.GONE);
                    txtMessage.setVisibility(View.VISIBLE);
                }
                txtMessage.setText("");
                }

        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void sendMessage(String sender, String receiver, String message){
        Date date = new Date();
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        String strDate;
        strDate = formatter.format(date);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("type","text");
        hashMap.put("time",currentTimestamp);
        hashMap.put("date",strDate);

        ref.child("Chats").push().setValue(hashMap);
    }
    private void sendImgMessage(String sender, String receiver){
        final String timestampImg = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        String filePathAndName = "Chat_images/" +" "+ user+"_"+getReceiver +"_"+timestampImg;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //onSuccess get Url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadImageUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            //url image received
                            Date date = new Date();
                            SimpleDateFormat formatter;
                            formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
                            String strDate;
                            strDate = formatter.format(date);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
                            final String imgUri = downloadImageUri.toString();
                            HashMap<String, Object> hashMap =new HashMap<>();
                            hashMap.put("sender",sender);
                            hashMap.put("receiver",receiver);
                            hashMap.put("message",imgUri);
                            hashMap.put("type","img");
                            hashMap.put("time",currentTimestamp);
                            hashMap.put("date",strDate);

                            ref.child("Chats").push().setValue(hashMap);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to upload image
                        Toast.makeText(MessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void readMessages(String myID, String userID){
        mchat = new ArrayList<>();
        databaseReference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren() ){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                        chat.getReceiver().equals(userID) && chat.getSender().equals(myID)) {
                        mchat.add(chat);
                    }
                    chatAdapter = new MessageAdapter(MessageActivity.this, mchat);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void prepareNotificationMessage( String message){
        //when seller changed order status, send notif to buyer

        //prepare data  for notif

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE ="New message from "+ senderName;
        String NOTIFICATION_MESSAGE =  message;
        String NOTIFICATION_TYPE = "Chat";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            // mga sinesend
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("oppoUid",getReceiver);
            notificationBodyJo.put("myUid",user);
            notificationBodyJo.put("message",message);
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

    private void showImagePicDialog() {
        // options to display in dialog
        String[] options = {"Camera","Gallery"};
        //dialog popout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select image from")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle the clicks
                        if (which == 0){
                            // camera selected
                            if (checkCameraPermission()){
                                //permission allowed
                                pickFromCamera();
                            }else{
                                //not allowed
                                requestCameraPermission();
                            }
                        }else{
                            //gallery selected
                            if (checkStoragePermission()){
                                //permission allowed
                                pickfromGallery();
                            }else{
                                //not allowed
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickfromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        //image picked
                        Intent data = result.getData();
                        image_uri = data.getData();
                        Toast.makeText(MessageActivity.this, "Uploaded ready to send", Toast.LENGTH_SHORT).show();
                        imgPreview.setImageURI(image_uri);
                        imgPreview.setVisibility(View.VISIBLE);
                        txtMessage.setVisibility(View.GONE);
                    }else{
                        //cancelled
                        Toast.makeText(MessageActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Bundle bundle = result.getData().getExtras();
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                        bitmap = (Bitmap) bundle.get("data");

                        image_uri = getImageUri(MessageActivity.this ,bitmap);
                        Toast.makeText(MessageActivity.this, "Uploaded ready to send", Toast.LENGTH_SHORT).show();
                        imgPreview.setImageURI(image_uri);
                        imgPreview.setVisibility(View.VISIBLE);
                        txtMessage.setVisibility(View.GONE);
                    }
                }
            }
    );

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Camera Permission Denied ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowed
                        pickfromGallery();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Gallery Permission Denied ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                imgPreview.setImageURI(image_uri);
            }else if (requestCode ==IMAGE_PICK_CAMERA_CODE){
                imgPreview.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}