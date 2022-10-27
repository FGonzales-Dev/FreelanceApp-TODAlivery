package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView TVfrontname, TVfname, TVphone, TVaddress;
    private ImageView picProfile;
    private Button editBTN;
    private ImageButton backBTN;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        TVfrontname = findViewById(R.id.TVfrontname);
        TVfname = findViewById(R.id.TVfname);
        TVphone = findViewById(R.id.TVphone);
        TVaddress = findViewById(R.id.TVaddress);
        picProfile = findViewById(R.id.picIV);
        editBTN = findViewById(R.id.editprofileBTN);
        firebaseAuth = FirebaseAuth.getInstance();
        backBTN =findViewById(R.id.BTNback);

        checkUser();


        editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminProfileActivity.this, ProfileEditAdminActivity.class));
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminProfileActivity.this, SettingsActivity.class));
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
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
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

                            TVfname.setText(name);
                            TVfrontname.setText(name);
                            TVphone.setText(phone);
                            TVaddress.setText(address);


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
        startActivity(new Intent(AdminProfileActivity.this, SettingsActivity.class));
        finish();
    }

} // public class closing