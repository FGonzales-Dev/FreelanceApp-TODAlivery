package com.todaliveryph.todaliverymarketdeliveryapp.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.FilterProductUser;
import com.todaliveryph.todaliverymarketdeliveryapp.R;
import com.todaliveryph.todaliverymarketdeliveryapp.activities.ShopDetailsActivity;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelProduct;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductUser filter;


    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflater
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //get data
        final ModelProduct modelProduct = productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        //set data

        holder.titleTv.setText(productTitle);
        holder.discountedNoteTv.setText(discountNote);
        holder.descriptionTv.setText(productDescription);
        holder.quantityTv.setText("( "+productQuantity+" )");
        holder.originalPriceTv.setText("₱ "+originalPrice);
        holder.discountedPriceTv.setText("₱ "+discountPrice);

        if (discountAvailable.equals("true")){
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else {
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.todalogo_icon).into(holder.productIconIv);
        }catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.todalogo_icon);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private double origCost = 0;
    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;




    public void showQuantityDialog(ModelProduct modelProduct) {

        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        //declaration:
        Spinner spinner = view.findViewById(R.id.kiloSpin);
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.adminUsernameTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        TextView quantityTv = view.findViewById(R.id.adminUseremailTv);
        TextView kiloTitleTv = view.findViewById(R.id.kiloTitle);
        TextView kiloAmountTv = view.findViewById(R.id.kgTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);


        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();


        final String price;

        if (modelProduct.getDiscountAvailable().equals("true")){

            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }else{
            discountedPriceTv.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }
        origCost = Double.parseDouble(price.replaceAll("₱",""));
        cost = Double.parseDouble(price.replaceAll("₱",""));
        finalCost = Double.parseDouble(price.replaceAll("₱",""));
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Picasso.get().load(image).placeholder(R.drawable.todalogo_icon).into(productIv);

        }catch (Exception e){
            productIv.setImageResource(R.drawable.todalogo_icon);

        }

        //set data

        titleTv.setText(""+title+" ( "+productQuantity+" )");
        pQuantityTv.setText(""+productQuantity.replace(" ","").toLowerCase());
        descriptionTv.setText(""+description);
        discountedNoteTv.setText(""+discountNote);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("₱ "+modelProduct.getOriginalPrice());
        discountedPriceTv.setText("₱ "+modelProduct.getDiscountPrice());
        finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));

        AlertDialog dialog = builder.create();
        dialog.show();

        if(pQuantityTv.getText().equals("1kg")){
            kiloTitleTv.setVisibility(View.VISIBLE);
            String[] routeItems = new String[]{"1/4kg","1/2kg","1kg"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, routeItems);
            spinner.setAdapter(adapter);
            decrementBtn.setVisibility(View.GONE);
            incrementBtn.setVisibility(View.GONE);
            quantityTv.setVisibility(View.GONE);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


                    if (spinner.getSelectedItem().equals("1kg")) {
                        finalCost =  origCost;
                        finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));
                        decrementBtn.setVisibility(View.VISIBLE);
                        incrementBtn.setVisibility(View.VISIBLE);
                        quantityTv.setVisibility(View.VISIBLE);
                        kiloAmountTv.setVisibility(View.VISIBLE);

                    }
                    else if(spinner.getSelectedItem().equals("1/4kg")){
                        finalCost =  (origCost/4);
                        finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));
                        decrementBtn.setVisibility(View.GONE);
                        incrementBtn.setVisibility(View.GONE);
                        quantityTv.setVisibility(View.GONE);
                        kiloAmountTv.setVisibility(View.GONE);
                    }
                    else if(spinner.getSelectedItem().equals("1/2kg")){
                        finalCost =  (origCost/2);
                        finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));
                        decrementBtn.setVisibility(View.GONE);
                        incrementBtn.setVisibility(View.GONE);
                        quantityTv.setVisibility(View.GONE);
                        kiloAmountTv.setVisibility(View.GONE);
                    }
                    else{
                        decrementBtn.setVisibility(View.GONE);
                        incrementBtn.setVisibility(View.GONE);
                        quantityTv.setVisibility(View.GONE);
                        kiloAmountTv.setVisibility(View.GONE);
                      //  kiloTitleTv.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        }

        else{
            kiloAmountTv.setVisibility(View.GONE);
           kiloTitleTv.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            decrementBtn.setVisibility(View.VISIBLE);
            incrementBtn.setVisibility(View.VISIBLE);
            quantityTv.setVisibility(View.VISIBLE);
        }

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));
                quantityTv.setText(""+quantity);


            }
        });

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quantity>1){
                    finalCost = finalCost - cost;
                    quantity--;
                    finalPriceTv.setText("₱ "+String.format("%.2f",finalCost));
                    quantityTv.setText(""+quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;//originalPriceTv.getText().toString().trim().replace("₱ ","");
                String totalPrice = finalPriceTv.getText().toString().trim().replace("₱ ","");
                String quantity = quantityTv.getText().toString().trim();

                //adding to db via sqlite method
                //using maven jetpack io
                addToCart(productId, title, priceEach, totalPrice, quantity) ;
                dialog.dismiss();
            }
        });

    }


    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {

        String timestamp = ""+System.currentTimeMillis();
        String itemId = productId.toString().trim()+timestamp;

        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB_TODA")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId )
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
               .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();



        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this, filterList);

        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

        //ui views
        private ImageView productIconIv;
        private TextView discountedNoteTv,titleTv,descriptionTv,addToCartTv,quantityTv,discountedPriceTv,originalPriceTv;
        private ImageView nextIv;
        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            //inside declaration

            productIconIv = itemView.findViewById(R.id.adminUserprofilelv);
            discountedNoteTv =itemView.findViewById(R.id.discountedNoteTv);
            titleTv=itemView.findViewById(R.id.adminUsernameTv);
            descriptionTv=itemView.findViewById(R.id.descriptionTv);
            addToCartTv=itemView.findViewById(R.id.addToCartTv);
            quantityTv=itemView.findViewById(R.id.adminUseremailTv);

            discountedPriceTv=itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv=itemView.findViewById(R.id.originalPriceTv);
            nextIv=itemView.findViewById(R.id.nextIv);

        }
    }// closing hold product
} // closing adapter class
