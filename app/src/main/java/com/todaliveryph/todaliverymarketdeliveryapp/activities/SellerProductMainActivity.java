package com.todaliveryph.todaliverymarketdeliveryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterProductSeller;
import com.todaliveryph.todaliverymarketdeliveryapp.Constants;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelProduct;
import com.todaliveryph.todaliverymarketdeliveryapp.R;

import java.util.ArrayList;

public class SellerProductMainActivity extends AppCompatActivity {

    private TextView shopnameSTV, phoneSTV, addressSTV,filteredProductsTv;
    private ImageView picIV, addProductsIV;
    private EditText searchProductEt;
    private ImageButton filterProductsBtn , backBTN, reviewsBtn;
    private FirebaseAuth firebaseAuth;
    private RecyclerView productsRv;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_product_main);

        productsRv = (RecyclerView) findViewById(R.id.productsRv);
        filteredProductsTv =findViewById(R.id.filteredProductsTv);
        filterProductsBtn =findViewById(R.id.filterProductsBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        shopnameSTV = findViewById(R.id.shopnameSTV);
        phoneSTV = findViewById(R.id.phoneSTV);
        addressSTV = findViewById(R.id.addressSTV);
        picIV = findViewById(R.id.picIV);
        addProductsIV = findViewById(R.id.addProductsIV);
        firebaseAuth = FirebaseAuth.getInstance();
        backBTN = findViewById(R.id.backBTN);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        checkUser();
        loadAllProducts();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        addProductsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellerProductMainActivity.this, SellerAddProductActivity.class));
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellerProductMainActivity.this, MainSellerActivity.class));
                finish();
            }
        });

        filterProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SellerProductMainActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected =Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }else {
                                    // load filtered
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellerProductMainActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });

    }//closing create bundle



    private void loadFilteredProducts(String selected) {

        productList = new ArrayList<>();
        //get all producst from a seller
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){

                            String productCategory =""+ds.child("productCategory").getValue();

                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }


                        }
                        //setuo adapter
                        adapterProductSeller = new AdapterProductSeller(SellerProductMainActivity.this, productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {

        productList = new ArrayList<>();
        //get all producst from a seller
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setuo adapter
                        adapterProductSeller = new AdapterProductSeller(SellerProductMainActivity.this, productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user ==null ){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String uid = ""+ds.child("").getValue();
                            String email = ""+ds.child("email").getValue();
                            String name = ""+ds.child("name").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String fee = ""+ds.child("deliveryFee").getValue();
                            String address = ""+ds.child("address").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String accountType =""+ds.child("accountType").getValue();
                            String online = ""+ds.child("online").getValue();
                            String shopOpen = ""+ds.child("shopOpen").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            shopnameSTV.setText(shopName);
                            phoneSTV.setText(phone);
                            addressSTV.setText(address);


                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.add_profile).into(picIV);
                            }catch (Exception e){
                                picIV.setImageResource(R.drawable.add_profile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void onBackPressed () {
        startActivity(new Intent(SellerProductMainActivity.this, MainSellerActivity.class));
        finish();
    }


}//closing public class