package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.Chat;
import com.todaliveryph.todaliverymarketdeliveryapp.messages.Users;
import com.todaliveryph.todaliverymarketdeliveryapp.messages.UsersAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MessagesActivity extends AppCompatActivity {


    private RecyclerView messageRecyclerView;
    private List<Users> mUsers;
    private UsersAdapter usersAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FirebaseUser firebaseUser;
    EditText searchET;
    private List<String> usersList;
    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setHasFixedSize(true);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchET =findViewById(R.id.searchTxtB);
        backBtn = findViewById(R.id.backBtn1);
        usersList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
              //  latestMessage=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getSender().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getReceiver());
                    }

                    if (chat.getReceiver().equals(firebaseUser.getUid())) {
                            usersList.add(chat.getSender());

                    }
                }


                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    filter(s.toString());
                }catch (Exception e){

                }

            }
        });


    }

    private void filter(String text) {
        ArrayList<Users> filterList = new ArrayList<>();

        for(Users item : mUsers){
            if(item.getName().toLowerCase().contains(text.toLowerCase())){
                filterList.add(item);
            }
        }
        usersAdapter.filteredList(filterList);
    }

    private void readChats() {
        mUsers = new ArrayList<>();
       // Collections.sort(usersList);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Users user = snapshot.getValue(Users.class);

                    for (String id : usersList) {
                        if (user.getUid().equals(id)) {
                            if (mUsers.size() != 0) {
                                int flag = 0;
                                for (Users u : mUsers) {
                                    if (user.getUid().equals(u.getUid())) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag == 0)
                                    mUsers.add(user);
                            } else {
                                mUsers.add(user);
                            }
                        }

                    }

                }

                usersAdapter = new UsersAdapter(MessagesActivity.this, mUsers);
                messageRecyclerView.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}