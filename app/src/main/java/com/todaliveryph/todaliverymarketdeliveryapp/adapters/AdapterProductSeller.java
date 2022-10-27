package com.todaliveryph.todaliverymarketdeliveryapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.FilterProduct;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.SellerEditProductActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelProduct;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductsSeller> implements Filterable {

    private Context context;
    private FilterProduct filter;
    public ArrayList<ModelProduct> productList, filterList;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    class HolderProductsSeller extends RecyclerView.ViewHolder{

        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, quantityTv, discountedPriceTv,originalPriceTV;

        public HolderProductsSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv = itemView.findViewById(R.id.adminUserprofilelv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.adminUsernameTv);
            quantityTv = itemView.findViewById(R.id.adminUseremailTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTV = itemView.findViewById(R.id.originalPriceTV);



        }
    }

    @NonNull
    @Override
    public HolderProductsSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductsSeller(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductsSeller holder, int position) {
        final ModelProduct modelProduct = productList.get(position);

        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("₱ "+discountPrice);
        holder.originalPriceTV.setText("₱ "+originalPrice);

        if (discountAvailable.equals("true")){
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTV.setPaintFlags(holder.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else {
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTV.setPaintFlags(0);

        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.todalogo_icon).into(holder.productIconIv);
        }catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.todalogo_icon);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // handle the : item when clicked and show in bottom sheet
                detailsBottomSheet(modelProduct);// model product contains the item selected
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog =  new BottomSheetDialog(context);
        // iflate the view in bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);



        //inside bottom sheet
        ImageButton backBTN= view.findViewById(R.id.backBTN);
        ImageButton editBTN= view.findViewById(R.id.editBTN);
        ImageButton deleteBTN= view.findViewById(R.id.deleteBTN);
        ImageView productIconIV= view.findViewById(R.id.productIconIV);
        TextView discountedNoteTv= view.findViewById(R.id.discountedNoteTv);
        TextView titleTv= view.findViewById(R.id.adminUsernameTv);
        TextView descriptionTv= view.findViewById(R.id.descriptionTv);
        TextView categoryTv= view.findViewById(R.id.categoryTv);
        TextView quantityTv= view.findViewById(R.id.adminUseremailTv);
        TextView discountedPriceTv= view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv= view.findViewById(R.id.originalPriceTv);

        //get data from database
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //setting data inside bottom sheet

        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountedNoteTv.setText(discountNote);
        discountedPriceTv.setText("₱ "+discountPrice);
        originalPriceTv.setText("₱ "+originalPrice);

        if (discountAvailable.equals("true")){
           discountedPriceTv.setVisibility(View.VISIBLE);
           discountedNoteTv.setVisibility(View.VISIBLE);
           originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else {
            discountedPriceTv.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);
            originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.todalogo_icon).into(productIconIV);
        }catch (Exception e){
            productIconIV.setImageResource(R.drawable.todalogo_icon);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetDialog.dismiss();

                Intent intent = new Intent(context, SellerEditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);
            }
        });

        //delete click
        deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetDialog.dismiss();


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete "+title)
                        .setMessage("Are you sure you want to delete this product?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProduct(id); // id is the timestap wherin your product uid
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismissed
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Successfully Removed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {

        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }




}
