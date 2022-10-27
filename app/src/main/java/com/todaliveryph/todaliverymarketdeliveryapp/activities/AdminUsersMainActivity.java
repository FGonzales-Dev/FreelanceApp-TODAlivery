package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterAdminUser;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelUser;

import java.util.ArrayList;

public class AdminUsersMainActivity extends AppCompatActivity {


    private ImageButton BTNback;
    private RecyclerView usersRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelUser> userList;
    private AdapterAdminUser adapterAdminUser;

    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users_main);


        Intent intent = getIntent();
        userType = intent.getStringExtra("userType");

        BTNback = findViewById(R.id.BTNback);
        usersRv = findViewById(R.id.usersRv);
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();

        BTNback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminUsersMainActivity.this, MainAdminActivity.class));
                finish();
            }
        });

    }//closing create bundle

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user ==null ){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }else{
            loadUsers();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            loadUsers();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUsers() {
        userList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("orderBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid =""+ds.getRef().getKey();

                    ModelUser modelUser =  ds.getValue(ModelUser.class);
                    if(modelUser.getAccountType().equals(userType)){
                        //add to list
                        userList.add(modelUser);
                    }
                }
                //setup adapter
                adapterAdminUser = new AdapterAdminUser(AdminUsersMainActivity.this, userList);
                //setup recycler view
                usersRv.setAdapter(adapterAdminUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}//closing public class