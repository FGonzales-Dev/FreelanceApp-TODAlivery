package com.todaliveryph.todaliverymarketdeliveryapp.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private List<Chat> mChat;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> mChat) {
        this.mChat = mChat;
        this.context = context;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Chat chatList = mChat.get(position);

        if (chatList.getType().equals("text")) {
            holder.showMessages.setText(chatList.getMessage());
            holder.time.setText(chatList.getDate());
        }
        else {
            try {
                holder.showMessages.setVisibility(View.GONE);
                holder.showImgMessages.setVisibility(View.VISIBLE);
                Picasso.get().load(chatList.getMessage()).into(holder.showImgMessages);
                holder.time.setText(chatList.getDate());
            } catch (Exception e) {
                holder.showMessages.setText(chatList.getMessage());
            }

        }


    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView showMessages, time;
        public ImageView showImgMessages;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessages = itemView.findViewById(R.id.showMessage);
            time = itemView.findViewById(R.id.msgTime);
            showImgMessages = itemView.findViewById(R.id.showImgMessage);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
