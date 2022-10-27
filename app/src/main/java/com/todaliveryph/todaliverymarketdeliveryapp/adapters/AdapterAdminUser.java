package com.todaliveryph.todaliverymarketdeliveryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.AdminUserProfileActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsUsersActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelUser;

import java.util.ArrayList;

public class AdapterAdminUser extends RecyclerView.Adapter<AdapterAdminUser.HolderUser>{

    private Context context;
    private ArrayList<ModelUser> userArrayList;

    public AdapterAdminUser(Context context, ArrayList<ModelUser> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public HolderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_user, parent, false);
        return new HolderUser(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderUser holder, int position) {

        ModelUser modelUser = userArrayList.get(position);
        String uid = modelUser.getUid();
        String name = modelUser.getName();
        String profile = modelUser.getProfileImage();

        loadUserDetail(modelUser, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open orders, we need keys there: uid
                Intent intent = new Intent(context, AdminUserProfileActivity.class);
                intent.putExtra("userUid",uid);
                context.startActivity(intent);
            }
        });

    }

    private void loadUserDetail(ModelUser modelUser, HolderUser holder) {
        String uid = modelUser.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String name = ""+snapshot.child("name").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        holder.adminUsernameTv.setText(name);
                        holder.adminUseremailTv.setText(email);

                        try{
                            Picasso.get().load(profileImage).placeholder(R.drawable.review_icon_person).into(holder.adminUserprofilelv);
                        }catch (Exception e){
                          holder.adminUserprofilelv.setImageResource(R.drawable.review_icon_person);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    class HolderUser extends RecyclerView.ViewHolder{

        private ImageView adminUserprofilelv;
        private TextView adminUsernameTv, adminUseremailTv;

        public HolderUser(@NonNull View itemView) {
            super(itemView);
            adminUserprofilelv = itemView.findViewById(R.id.adminUserprofilelv);
            adminUsernameTv = itemView.findViewById(R.id.adminUsernameTv);
            adminUseremailTv = itemView.findViewById(R.id.adminUseremailTv);
        }
    }

}
