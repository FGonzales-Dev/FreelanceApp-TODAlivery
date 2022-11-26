package com.todaliveryph.todaliverymarketdeliveryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todaliveryph.todaliverymarketdeliveryapp.OrderDetailsRidersActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.RidersOrderMainActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.OrderDetailsUsersActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderDriver;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelOrderUser;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderDriver extends RecyclerView.Adapter<AdapterOrderDriver.HolderOrderDriver> {

    private Context context;
    private ArrayList<ModelOrderDriver> orderDriverList;

    public AdapterOrderDriver(Context context, ArrayList<ModelOrderDriver> orderDriverList) {
        this.context = context;
        this.orderDriverList = orderDriverList;
    }

    @NonNull
    @Override
    public HolderOrderDriver onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_rider, parent, false);
        return new HolderOrderDriver(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderDriver holder, int position) {

        ModelOrderDriver modelOrderDriver = orderDriverList.get(position);
        String orderId = modelOrderDriver.getOrderId();
        String customerAddress = modelOrderDriver.getCustomerAddress();
        String customerName = modelOrderDriver.getCustomerName();
        String customerPhone= modelOrderDriver.getCustomerPhone();
        String itemsAmount = modelOrderDriver.getItemsAmount();
        String itemsDeliveryFee = modelOrderDriver.getItemsDeliveryFee();
        String itemsTotalCost = modelOrderDriver.getItemsTotalCost();
        String shopId = modelOrderDriver.getShopId();
        String date = modelOrderDriver.getOrderTime();
        String status = modelOrderDriver.getStatus();
        String customerId = modelOrderDriver.getCustomerId();
        //get shop info
     //  loadShopInfo(modelOrderDriver, holder);
        holder.customerNameTv.setText(customerName);
        holder.amountTv.setText("Amount: "+itemsTotalCost);
        holder.statusTv.setText(status);
        holder.orderIdTv.setText("Order ID: "+orderId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(date));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.dateTv.setText(formatedDate);


        //change color of status depends on its status
        try {

            if (status.equals("Pending")){
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            } else if(status.equals("Completed")){
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.green));
            } else if(status.equals("Cancelled")){
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.color_Red));
            }
        }catch (Exception e){

        }



        //covert timestamp to date format



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetailsRidersActivity.class);

                intent.putExtra("orderId",orderId);
                intent.putExtra("shopId",shopId);
                intent.putExtra("customerName",customerName);
                intent.putExtra("customerPhone",customerPhone);
                intent.putExtra("customerAddress",customerAddress);
                intent.putExtra("date",date);
                intent.putExtra("status",status);
                intent.putExtra("amount",itemsAmount);
                intent.putExtra("deliveryFee",itemsDeliveryFee);
                intent.putExtra("totalCost",itemsTotalCost);
                intent.putExtra("buyerId",customerId);

                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(ModelOrderDriver modelOrderDriver, HolderOrderDriver holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderDriver.getShopId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String shopName = ""+snapshot.child("shopName").getValue();
                            holder.customerNameTv.setText(shopName);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderDriverList.size();
    }

    class HolderOrderDriver extends RecyclerView.ViewHolder{

        private TextView orderIdTv,customerNameTv,dateTv,amountTv,statusTv;

        public HolderOrderDriver(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            customerNameTv = itemView.findViewById(R.id.shopNameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);




        }
    }
}
