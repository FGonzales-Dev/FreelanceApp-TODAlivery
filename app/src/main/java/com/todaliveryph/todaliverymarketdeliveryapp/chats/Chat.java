package com.todaliveryph.todaliverymarketdeliveryapp.chats;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.MemoryData;
import com.todaliveryph.todaliverymarketdeliveryapp.MessagesActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsUsersActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.RiderDeliver;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ShopDetailsActivity;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todalivery-market-delive-ace4f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;

    private final List<ChatList> chatLists = new ArrayList<>();

    private String chatKey;
    String getUserMobile = "";

    private RecyclerView chattingRecyclerView;
    private ChatAdapter chatAdapter;
    private boolean loadingFirstTime = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseAuth = FirebaseAuth.getInstance();

        final ImageView backBtn = findViewById(R.id.backBtn);
        final TextView nameTV = findViewById(R.id.name);
        final EditText messageET = findViewById(R.id.messageET);
        final CircleImageView profilePic = findViewById(R.id.profilePic);
        final ImageView sendBtn = findViewById(R.id.sendBtn);

        chattingRecyclerView = findViewById(R.id.chattingRecyclerView);

        final String getName = getIntent().getStringExtra("name");
        final String getProfilePic = getIntent().getStringExtra("profileImage");
        chatKey = getIntent().getStringExtra("chat_key");


        final String getMobile = getIntent().getStringExtra("mobile");

        getUserMobile = FirebaseAuth.getInstance().getUid();
        nameTV.setText(getName);


        try{
            Picasso.get().load(getProfilePic).into(profilePic);
        }catch (Exception e){
           profilePic.setImageResource(R.drawable.user_icon);
        }

        chattingRecyclerView.setHasFixedSize(true);
        chattingRecyclerView.setLayoutManager(new LinearLayoutManager(Chat.this));

        chatAdapter = new ChatAdapter(chatLists, Chat.this);


        chattingRecyclerView.setAdapter(chatAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (chatKey.isEmpty()) {
                    chatKey = "1";
                    if (snapshot.hasChild("chat")) {
                        chatKey = String.valueOf(snapshot.child("chat").getChildrenCount() + 1);

                    }
                }

                if (snapshot.hasChild("chat")) {

                    if (snapshot.child("chat").child(chatKey).hasChild("messages")) {
                        chatLists.clear();
                        for (DataSnapshot messagesSnapshot : snapshot.child("chat").child(chatKey).child("messages").getChildren()) {

                            if (messagesSnapshot.hasChild("msg") && messagesSnapshot.hasChild("mobile")) {

                                final String messageTimestapms = messagesSnapshot.getKey();
                                final String getMobile = messagesSnapshot.child("mobile").getValue(String.class);
                                final String getMsg = messagesSnapshot.child("msg").getValue(String.class);

                                Timestamp timestamp = new Timestamp(Long.parseLong(messageTimestapms));
                                Date date = new Date(timestamp.getTime());
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:aa", Locale.getDefault());
                                ChatList chatList = new ChatList(getMobile, getName,getMsg, simpleDateFormat.format(date),simpleTimeFormat.format(date));
                                chatLists.add(chatList);


                                chattingRecyclerView.scrollToPosition(chatLists.size()-1);
                                if (loadingFirstTime || Long.parseLong(messageTimestapms) > Long.parseLong(MemoryData.getLastMessageTS(Chat.this, chatKey))) {

                                    loadingFirstTime = false;
                                    MemoryData.saveLastMessageTS(messageTimestapms, chatKey, Chat.this);
                                    chatAdapter.updateChatList(chatLists);
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


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String getTxtMessage = messageET.getText().toString();

                final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);

                databaseReference.child("chat").child(chatKey).child("users_1").setValue(firebaseAuth.getUid());
                databaseReference.child("chat").child(chatKey).child("users_2").setValue(getMobile);
                databaseReference.child("chat").child(chatKey).child("messages").child(currentTimestamp).child("msg").setValue(getTxtMessage);
                databaseReference.child("chat").child(chatKey).child("messages").child(currentTimestamp).child("mobile").setValue(getUserMobile);

                Toast.makeText(Chat.this, "Sent!", Toast.LENGTH_SHORT).show();
                prepareNotificationMessage();
                messageET.setText("");
            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed ();
            }
        });
    }

    public void onBackPressed () {
        finish();
    }


    private void prepareNotificationMessage() {


        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Message from " + getUserMobile;
        String NOTIFICATION_MESSAGE = "You have received a new message!";
        String NOTIFICATION_TYPE = "NewMessage";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {

            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); // para ung buyer id is recognized as buyer ah yeah
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMsessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
      //          Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
           //     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         //       finish();
         //       startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
           //     Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
            //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(Chat.this,"Error notif", Toast.LENGTH_SHORT).show();
           //     finish();
            //    startActivity(intent);
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
}