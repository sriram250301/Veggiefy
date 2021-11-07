package in.veggiefy.androidapp.ui.cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import in.veggiefy.androidapp.ui.checkout.Checkout;
import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference cartDbRef;
    DatabaseReference cartDbRef2;
    ValueEventListener cartListener;
    DatabaseReference regulationsDbRef;
    ValueEventListener regulationsDbRefListener;
    DatabaseReference productDbRef;
    ValueEventListener cartRefListener;
    CartAdapter cartAdapter;
    Button checkoutButton;
    TextView totalAmounttextView;
    ImageView backIcon;
    ImageView trashIcon;
    ImageView placeHolderImage;
    Button retryButton;
    TextView noConnectionText;
    ProgressDialog progressDialog;
    //Variables
    String phonenumber,businessType;
    String DEPOT_KEY;
    int TOTAL_AMOUNT=0;
    boolean isConnected=false,isAdapterSet=false,acceptingOrders=true;
    boolean valid=false;
    int checkoutLimit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        //HOOKS
        recyclerView=findViewById(R.id.cart_items_recyclerView);
        checkoutButton=findViewById(R.id.checkout_button);
        totalAmounttextView=findViewById(R.id.total_checkout_amount_textView);
        backIcon=findViewById(R.id.back_icon_cart_imageView);
        trashIcon=findViewById(R.id.trash_icon_cart_imageView);
        //
        placeHolderImage=findViewById(R.id.place_holder_Cart_imageView);
        noConnectionText=findViewById(R.id.no_connection_Cart_textView);
        retryButton=findViewById(R.id.retry_connection_cart_button);


        //SHARED PREFERENCE
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);

        phonenumber=sharedPreferences.getString("PHONE",null);
        DEPOT_KEY=sharedPreferences.getString("DEPOTKEY","D_1");
        businessType=sharedPreferences.getString("BUSINESS_TYPE","Whole seller");

        if(!isConnected()){
                //visiblity set
                placeHolderImage.setVisibility(View.VISIBLE);
                noConnectionText.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
                placeHolderImage.setImageResource(R.mipmap.no_connection_png_foreground);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recreate();
                    }
                });
        }
        else{
            //Set ProgressDialog
            progressDialog=new ProgressDialog(Cart.this);
            //show progressDialog
            progressDialog.show();
            //set contentView for progressDialog
            progressDialog.setContentView(R.layout.progress_dialog);
            //set transparent background
            progressDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            //FIREBASE
            cartDbRef= FirebaseInit.getDatabase().getReference().child("USERS/"+phonenumber);
            cartDbRef.keepSynced(true);
            productDbRef= FirebaseInit.getDatabase().getReference().child("MARKET/DEPOTS/"+DEPOT_KEY+"/details/stock/TOTAL");
            productDbRef.keepSynced(true);

            Log.d("TAG", "CART onCreate: BEFORE FIRE QUERY");
            //Firebase option query
            FirebaseRecyclerOptions<CartModel> options =
                    new FirebaseRecyclerOptions.Builder<CartModel>()
                            .setQuery(cartDbRef.child("cart"), CartModel.class)
                            .build();
            Log.d("TAG", "CART onCreate: AFTER FIRE QUERY");

            isAdapterSet=true;
            cartAdapter=new CartAdapter(options,phonenumber,DEPOT_KEY);
            recyclerView.setAdapter(cartAdapter);

            //GET TOTAL
            getTotal();
            if (cartDbRef != null && cartListener != null) {
                cartDbRef.removeEventListener(cartListener);
            }
            getCheckoutlimit();

            progressDialog.dismiss();
        }

        //CHECKOUT CLICKED
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isConnected()) {
                    if(validateCheckout()){
                        checkIfAcceptingOrders();
                    }
                    else{
                        int insuffientAmount=checkoutLimit-TOTAL_AMOUNT;
                        new AlertDialog.Builder(Cart.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Insufficient amount")
                                .setMessage("You need to add items worth ₹"+insuffientAmount+" more to checkout")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
                else{
                    Toast.makeText(Cart.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //back clicked
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //clear cart clicked
        trashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemInCart();
            }
        });

        //SET TOTAL VIEW
        totalAmounttextView.setText("TOTAL ₹ 0");
    }

    private void checkIfAcceptingOrders() {

        DatabaseReference regulationsRef=FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/administration");
        regulationsRef.keepSynced(true);
        regulationsDbRefListener=regulationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                acceptingOrders=snapshot.child("acceptingorders").getValue(Boolean.class);
                if(acceptingOrders){
                    regulationsRef.removeEventListener(regulationsDbRefListener);
                    Log.d("TAG", "onClick: CHECKOUT LIMIT::"+checkoutLimit);
                    Intent intent = new Intent(Cart.this, Checkout.class);
                    intent.putExtra("TOTAL", TOTAL_AMOUNT);
                    startActivity(intent);
//                    finish();
                }
                else{
                    new AlertDialog.Builder(Cart.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Too late/early for placing orders")
                            .setMessage("Orders need to be placed between 4am and 7pm to be delivered the next morning")
                            .setPositiveButton("OK", null)
                            .show();
                    regulationsRef.removeEventListener(regulationsDbRefListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                regulationsRef.removeEventListener(regulationsDbRefListener);
                Toast.makeText(Cart.this, "Something went wrong,try later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkIfCartEmpty() {
        Log.d("TAG", "checkIfCartEmpty: INSIDE IFEMPTY()");
        cartDbRef2=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/cart");
        cartDbRef2.keepSynced(true);
        cartRefListener=cartDbRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: SNAP EMPTY CART first"+snapshot);
                if(!snapshot.exists()){
                    Log.d("TAG", "onDataChange: SNAP EMPTY CART inside exist"+snapshot);
                    recyclerView.setVisibility(View.INVISIBLE);
                    placeHolderImage.setVisibility(View.VISIBLE);

                    placeHolderImage.setImageResource(R.mipmap.out_of_stock_png_foreground);
                }
                else{
                    Log.d("TAG", "onDataChange: SNAP EMPTY CART in else:"+snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                placeHolderImage.setVisibility(View.VISIBLE);
                placeHolderImage.setImageResource(R.mipmap.out_of_stock_png_foreground);
            }
        });


    }

    private void removeItemInCart() {
        if(isConnected()){
            new AlertDialog.Builder(Cart.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Please confirm")
                    .setMessage("Sure you want to clear cart?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
                            //Editor
                            SharedPreferences.Editor editor;
                            editor = sharedPreferences.edit();

                            editor.clear();

                            editor.apply();
                            //Empty cart and set total amount value back to zero
                            DatabaseReference cartTotalRef=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber);
                            cartTotalRef.child("cart").removeValue();
                            // revise total
                            TOTAL_AMOUNT=0;
                            setTotal();
                        }
                    })
                    .setNegativeButton("NO",null)
                    .show();
        }
        else{
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getTotal() {
        cartListener=cartDbRef.child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.d("TAG", "onDataChange: CART TOTAL VALUE::SNAPSHOT" + snapshot.toString());
                        TOTAL_AMOUNT=0;
                        int userquantity;
                        String prodKey = dataSnapshot.getKey();
                        String segment = snapshot.child(prodKey).child("segment").getValue(String.class);
                        userquantity = snapshot.child(prodKey).child("userquantity").getValue(Integer.class);
                        Log.d("TAG", "onDataChange: PRODUCTKEY::" + prodKey + "USER QUANTITY::" + userquantity + "SEGMENT::" + segment + "DEPOT KEY::" + DEPOT_KEY);
                        if(prodKey!=null && segment!=null) {
                            productDbRef.child(segment).child(prodKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        int price = snapshot.child("price").getValue(Integer.class);
                                        TOTAL_AMOUNT = TOTAL_AMOUNT + userquantity * price;
                                        Log.d("TAG", "onDataChange: TOTAL AMOUNT IN CART ACTIVITY::" + TOTAL_AMOUNT);
                                        setTotal();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (cartDbRef != null && cartListener != null) {
            cartDbRef.removeEventListener(cartListener);
        }

    }

    private void setTotal() {
        totalAmounttextView.setText("TOTAL ₹" + TOTAL_AMOUNT);
    }

    private void getCheckoutlimit() {
        regulationsDbRef=FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/users/checkoutlimit");
        regulationsDbRef.keepSynced(true);
        regulationsDbRefListener=regulationsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: CART CHECKOUT LIMIT::"+snapshot.toString());
                Log.d("TAG", "onDataChange: businessType::"+businessType);
                if(snapshot.child(businessType).getValue(Integer.class)!=null){
                    checkoutLimit=snapshot.child(businessType).getValue(Integer.class);
                    Log.d("TAG", "onDataChange: CHECKOUT LIMIT ::"+checkoutLimit);
                    regulationsDbRef.removeEventListener(regulationsDbRefListener);
                    checkIfCartEmpty();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Cart.this, "Error,Please try later", Toast.LENGTH_SHORT).show();
                regulationsDbRef.removeEventListener(regulationsDbRefListener);
            }
        });
    }

    private boolean validateCheckout() {

        if(TOTAL_AMOUNT>=checkoutLimit){
            valid=true;
            Log.d("TAG", "validateCheckout: VALID!");
        }

        return valid;
    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Cart cart) {
        ConnectivityManager connectivityManager=(ConnectivityManager) cart.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        isConnected= (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
        return isConnected;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isConnected()){
            if(isAdapterSet) cartAdapter.startListening();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isConnected()){
            if(isAdapterSet) cartAdapter.stopListening();
        }
        if (cartDbRef != null && cartListener != null) {
            cartDbRef.removeEventListener(cartListener);
        }
        if (cartDbRef2 != null && cartRefListener != null) {
            cartDbRef2.removeEventListener(cartRefListener);
        }
//        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (cartDbRef != null && cartListener != null) {
            cartDbRef.removeEventListener(cartListener);
        }
        if (cartDbRef2 != null && cartRefListener != null) {
            cartDbRef2.removeEventListener(cartRefListener);
        }
        TOTAL_AMOUNT=0;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cartDbRef != null && cartListener != null) {
            cartDbRef.removeEventListener(cartListener);
        }
        if (cartDbRef2 != null && cartRefListener != null) {
            cartDbRef2.removeEventListener(cartRefListener);
        }
        TOTAL_AMOUNT=0;

        finish();
//
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartDbRef != null && cartListener != null) {
            cartDbRef.removeEventListener(cartListener);
        }
        if (cartDbRef2 != null && cartRefListener != null) {
            cartDbRef2.removeEventListener(cartRefListener);
        }
        TOTAL_AMOUNT=0;
    }

}