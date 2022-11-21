package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminUserProfileActivity extends AppCompatActivity {

    String uid,currentUser;
    private TextView TVfrontname, TVfname, TVphone, TVaddress;
    private ImageView picProfile,id_image;
    private ProgressDialog progressDialog;
    private LinearLayout idVerificationLayout;
    private Button editBTN, approved, decline;
    private ImageButton backBTN;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_profile);


        Intent intent = getIntent();
        uid = intent.getStringExtra("userUid");
        TVfrontname = findViewById(R.id.TVfrontname);
        TVfname = findViewById(R.id.TVfname);
        TVphone = findViewById(R.id.TVphone);
        TVaddress = findViewById(R.id.TVaddress);
        id_image = findViewById(R.id.id_image);
        picProfile = findViewById(R.id.picIV);
        editBTN = findViewById(R.id.editprofileBTN);
        approved = findViewById(R.id.Approved);
        decline = findViewById(R.id.Decline);
        idVerificationLayout = findViewById(R.id.idVerificationLayout);
        firebaseAuth = FirebaseAuth.getInstance();
        backBTN =findViewById(R.id.BTNback);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        currentUser = firebaseAuth.getCurrentUser().getUid();
        checkUser();


        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Updating User...");
                progressDialog.show();
                //url received
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("riderAccepted","false");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Updated
                                progressDialog.dismiss();
                                Toast.makeText(AdminUserProfileActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed Show error message
                                Toast.makeText(AdminUserProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        approved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Updating User...");
                progressDialog.show();
                //url received
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("riderAccepted","true");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Updated
                                progressDialog.dismiss();
                                Toast.makeText(AdminUserProfileActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed Show error message
                                Toast.makeText(AdminUserProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminUserProfileActivity.this, AdminProfileEditUserActivity.class);
                intent.putExtra("userUid",uid);
                AdminUserProfileActivity.this.startActivity(intent);
//                startActivity(new Intent(AdminUserProfileActivity.this, ProfileEditUserActivity.class));
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }// create bundle closing

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null ){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }else{
            loadMyInfo();
        }
    }



    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(uid).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String uid = ""+ds.child("").getValue();
                            String email = ""+ds.child("email").getValue();
                            String name = ""+ds.child("name").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String address = ""+ds.child("address").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String accountType =""+ds.child("accountType").getValue();
                            String online = ""+ds.child("online").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String IdImage = ""+ds.child("IdImage").getValue();

                            TVfname.setText(name);
                            TVfrontname.setText(name);
                            TVphone.setText(phone);
                            TVaddress.setText(address);
                            if (accountType.equals("Rider")){
                                id_image.setVisibility(View.VISIBLE);
                                idVerificationLayout.setVisibility(View.VISIBLE);
                            }

                            try {
                                Picasso.get().load(IdImage).placeholder(R.drawable.add_profile).into(id_image);
                            }catch (Exception e){
                                id_image.setImageResource(R.drawable.add_profile);
                            }


                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.add_profile).into(picProfile);
                            }catch (Exception e){
                                picProfile.setImageResource(R.drawable.add_profile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void onBackPressed () {
        finish();
    }

    private void prepareNotificationMessage(){
        //when seller changed order status, send notif to buyer

        //prepare data  for notif

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE ="ID Verification";
        String NOTIFICATION_MESSAGE =  "ID Verification Update(s)";
        String NOTIFICATION_TYPE = "IdVerification";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            // mga sinesend
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("riderUid",uid);
            notificationBodyJo.put("adminUid",currentUser);
            notificationBodyJo.put("message","ID Verification Update");
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


} // public class closing