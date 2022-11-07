package com.todaliveryph.todaliverymarketdeliveryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.messages.MessagesAdapter;
import com.todaliveryph.todaliverymarketdeliveryapp.messages.MessagesList;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {

    private List<MessagesList> messagesLists = new ArrayList<>();

    private String name, mobile, email,getProfilePic, getName;

    private int unseenMessages = 0;
    private String lastMessage = "";

    private String chatKey;

    private RecyclerView messageRecyclerView;
    private MessagesAdapter messagesAdapter;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);


        final CircleImageView usersProfilePic = findViewById(R.id.usersProfilePic);

        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        firebaseAuth = FirebaseAuth.getInstance();


        messageRecyclerView.setHasFixedSize(true);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messagesAdapter = new MessagesAdapter(messagesLists, MessagesActivity.this);

        messageRecyclerView.setAdapter(messagesAdapter);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Info...");
        progressDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                final String profilePicUrl = snapshot.child("Users").child(firebaseAuth.getUid()).child("profileImage").getValue(String.class);

                try{
                    Picasso.get().load(profilePicUrl).into(usersProfilePic);
                }catch (Exception e){
                    usersProfilePic.setImageResource(R.drawable.user_icon);
                }


                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               messagesLists.clear();
            //    unseenMessages = 0;
           //     lastMessage = "";
                chatKey = "";

                for (DataSnapshot dataSnapshot : snapshot.child("Users").getChildren()) {

                    String getMobile = dataSnapshot.getKey();


                    if (!getMobile.equals(firebaseAuth.getUid())) {




                        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {


                                int getChatCounts = (int) snapshot1.getChildrenCount();


                                if (getChatCounts > 0) {

                                    for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {

                                       final String getKey = dataSnapshot1.getKey();

                                        chatKey = getKey;

                                        if (dataSnapshot1.hasChild("users_1") && dataSnapshot1.hasChild("users_2") && dataSnapshot1.hasChild("messages")) {

                                            final String getUserOne = dataSnapshot1.child("users_1").getValue(String.class);
                                            final String getUserTwo = dataSnapshot1.child("users_2").getValue(String.class);
                                            getName = dataSnapshot.child("name").getValue(String.class);
                                            getProfilePic = dataSnapshot.child("profileImage").getValue(String.class);
                                            if ((getUserOne.equals(getMobile) && getUserTwo.equals(firebaseAuth.getUid())) || (getUserOne.equals(firebaseAuth.getUid()) && getUserTwo.equals(getMobile))) {


                                                for (DataSnapshot chatDataSnapshot : dataSnapshot1.child("messages").getChildren()) {

                                                    final long getMessageKey = Long.parseLong(chatDataSnapshot.getKey());
                                                    final long getLastSeenMessage = Long.parseLong(MemoryData.getLastMessageTS(MessagesActivity.this, getKey));
                                              //      Toast.makeText(MessagesActivity.this, String.valueOf(getLastSeenMessage),Toast.LENGTH_SHORT).show();
                                                    lastMessage = chatDataSnapshot.child("msg").getValue(String.class);

                                                    if (getMessageKey >= getLastSeenMessage) {
                                                       // unseenMessages++;
                                                        MessagesList messagesList = new MessagesList(getMobile, getName, lastMessage, getProfilePic, unseenMessages, chatKey);

                                                        messagesLists.add(messagesList);
                                                        messagesAdapter.updateData(messagesLists);


                                                    }


                                                }

                                            }


                                        }



                                    }



                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

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
}