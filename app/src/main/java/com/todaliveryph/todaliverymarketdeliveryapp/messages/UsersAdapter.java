package com.todaliveryph.todaliverymarketdeliveryapp.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.MessagesActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ShopDetailsActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.Chat;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.MessageActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.chats.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    private List<Users> mUsers;
    String theLastMessage, type, userLastMessage, user;
    private Context context;

    public UsersAdapter(Context context, List<Users> mUsers) {
        this.mUsers = mUsers;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Users users = mUsers.get(position);
        holder.name.setText(users.getName());

        lastMessage(users.getUid(), holder.lastMessage);
        try {
            Picasso.get().load(users.getProfileImage()).into(holder.profilePic);
        } catch (Exception e) {
            holder.profilePic.setImageResource(R.drawable.user_icon_black);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("name", users.getName());
                intent.putExtra("profileImage", users.getProfileImage());
                intent.putExtra("receiverID", users.getUid());
                context.startActivity(intent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void filteredList(ArrayList<Users> filterList) {
        mUsers = filterList;
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profilePic,newMessage;
        private TextView name;
        private TextView lastMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            name = itemView.findViewById(R.id.name);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            newMessage = itemView.findViewById(R.id.newMessage);
        }
    }

    private void lastMessage(String userID, TextView last_msg) {
        theLastMessage = "";
        type = "";
        userLastMessage = "";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID) && chat.getSender().equals(firebaseUser.getUid())) {

                        theLastMessage = chat.getMessage();
                        userLastMessage = chat.getSender();
                        type = chat.getType();
                        user = firebaseUser.getUid();
                    }
                }
                if (!theLastMessage.equals("")) {
                    if (userLastMessage.equals(user)) {
                        last_msg.setTextColor(Color.GRAY);
                        if (type.equals("img")) {
                            last_msg.setText("You sent an image");
                        } else {
                            last_msg.setText("You: " + theLastMessage);
                        }
                    } else {
                        if (type.equals("img")) {
                            last_msg.setText("They sent an image");
                        } else {
                            last_msg.setText(theLastMessage);
                        }
                    }

                } else {
                    last_msg.setText("No message");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
