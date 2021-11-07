package in.veggiefy.androidapp.ui.addtocart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.cart.Cart;

public class AddToCart extends AppCompatActivity {

    TextView productname;
    TextView productprice;
    TextView quantitycount;
    TextView deliverpolicy;
    TextView availablequantity;
    ImageView productimage;
    ImageView backIcon;
    ImageView trashIcon;
    Button incrementButton;
    Button decrementButton;
    Button seeCart;
    DatabaseReference productDbRef;
    DatabaseReference cartDbRef;
    DatabaseReference cartTotalDbRef;
    DatabaseReference deliverRegulationDbRef;
    ValueEventListener productDbReflistener;
    //Variables
    String SEGMENT_KEY;
    String PRODUCT_KEY;
    String DEPOT_KEY;
    String productName="Product name";
    String phonenumber;
    int price;
    int deliveryTimeInDays;
    String deliveryHour="Delivery time";
    String metrics;
    String imageLink;
    int userQuantity;
    int availableQuantity;
    boolean isConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cart);

        Log.d("**********", "ADD TO CART onCreate: *******");
        gethooks();

        checkIfItemInCart();

        setViews();

        //BUNDLE
        Bundle bundle= getIntent().getExtras();
        PRODUCT_KEY=bundle.getString("productname");
        SEGMENT_KEY=bundle.getString("segment");

        //SHARED PREFERENCES
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        DEPOT_KEY=sharedPreferences.getString("DEPOTKEY","D_1");
        phonenumber=sharedPreferences.getString("PHONE",null);

        getRegulationDataFromDB();

        getProductDataFromDB();
        //FIREBASE
        productDbRef= FirebaseInit.getDatabase().getReference().child("MARKET/DEPOTS/"+DEPOT_KEY+"/details/stock/TOTAL/"+SEGMENT_KEY+"/"+PRODUCT_KEY);
        productDbRef.keepSynced(true);
        productDbReflistener=productDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: SAPSHOT::"+snapshot.toString());
                productName=snapshot.child("productname").getValue(String.class);
                price=snapshot.child("price").getValue(Integer.class);
                availableQuantity=snapshot.child("quantity").getValue(Integer.class);
                metrics=snapshot.child("metrics").getValue(String.class);
                imageLink=snapshot.child("imagelink").getValue(String.class);

                //SET VIEWS
                Log.d("TAG", "price: "+price+"metrics::"+metrics+"imagelink::"+imageLink);

                checkIfItemInCart();

                setViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddToCart.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });
        if(!isConnected()){
            Toast.makeText(this, "Please check your connection", Toast.LENGTH_SHORT).show();
        }


        //CLICK EVENTS
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    decrementQuantity();

                }
                else{
                    Toast.makeText(AddToCart.this, "Check network connection!", Toast.LENGTH_SHORT).show();
                }
                
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    incrementQuantity();

                }
                else{
                    Toast.makeText(AddToCart.this, "Check network connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seeCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddToCart.this, Cart.class);
                startActivity(intent);
                finish();
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        trashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemInCart();
            }
        });
    }

    private void getProductDataFromDB() {
        productDbRef= FirebaseInit.getDatabase().getReference().child("MARKET/DEPOTS/"+DEPOT_KEY+"/details/stock/TOTAL/"+SEGMENT_KEY+"/"+PRODUCT_KEY);
        productDbReflistener=productDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: SAPSHOT::"+snapshot.toString());
                productName=snapshot.child("productname").getValue(String.class);
                price=snapshot.child("price").getValue(Integer.class);
                availableQuantity=snapshot.child("quantity").getValue(Integer.class);
                metrics=snapshot.child("metrics").getValue(String.class);
                imageLink=snapshot.child("imagelink").getValue(String.class);

                //SET VIEWS
                Log.d("TAG", "price: "+price+"metrics::"+metrics+"imagelink::"+imageLink);

                checkIfItemInCart();

                setViews();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddToCart.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setViews() {
        productname.setText(productName);
        availablequantity.setText(String.format("%d%s available", availableQuantity, metrics));
        productprice.setText(String.format("₹ %s/%s", price, metrics));
        Glide.with(getApplicationContext()).load(imageLink).placeholder(R.mipmap.placeholder_png_foreground).into(productimage);
        quantitycount.setText(String.valueOf(userQuantity));
        deliverpolicy.setText(String.format("Get delivered on %s by %s", getDeliveryTime(), deliveryHour));
    }

    private String getDeliveryTime() {
        //delivery time estimate
        long time= System.currentTimeMillis();
        Date date=new Date(time);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        Log.d("TAG", "setViews: DELIVERY TIME IN DAYS::"+deliveryTimeInDays);
        calendar.add(Calendar.DAY_OF_MONTH,deliveryTimeInDays);
        Date deliveryDate=calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM ");
        String dateString = formatter.format(deliveryDate);
        return dateString;
    }

    private void gethooks() {
        //HOOKS
        productname=findViewById(R.id.product_name_addToCart_TextView);
        productprice=findViewById(R.id.price_addToCart_textView);
        quantitycount=findViewById(R.id.user_quantity_count_textView);
        deliverpolicy=findViewById(R.id.deliver_policy_textView);
        availablequantity=findViewById(R.id.available_quantity_addToCart_textView);
        //image
        productimage=findViewById(R.id.product_addToCart_imageView);
        backIcon=findViewById(R.id.back_icon_addTocart_imageView);
        trashIcon=findViewById(R.id.trash_icon_addToCart_imageView);
        //buttons
        decrementButton=findViewById(R.id.quantity_decrement_button);
        incrementButton=findViewById(R.id.quantity_increment_button);
        seeCart=findViewById(R.id.see_cart_button);
    }

    private void removeItemInCart() {
        if(isConnected()){

            userQuantity=0;
            SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
            //Editor
            SharedPreferences.Editor editor;
            editor = sharedPreferences.edit();

            editor.putInt(PRODUCT_KEY,userQuantity);

            editor.apply();
            //Set view
            quantitycount.setText(String.valueOf(userQuantity));
            updateInFirebase(userQuantity);

        }
        else{
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(AddToCart addToCart) {
        ConnectivityManager connectivityManager=(ConnectivityManager) addToCart.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn !=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            isConnected=true;
        }
        else{
            isConnected=false;
        }
        return isConnected;
    }

    private void decrementQuantity() {
        int temp=userQuantity-1;
        if(temp>=0){
            --userQuantity;
            //Save in Shared preferences
            SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);


            //Editor
            SharedPreferences.Editor editor;
            editor = sharedPreferences.edit();

            editor.putInt(PRODUCT_KEY,userQuantity);

            editor.apply();
            //Set view
            quantitycount.setText(String.valueOf(userQuantity));
            updateInFirebase(userQuantity);
        }

    }

    private void incrementQuantity() {
        int temp=userQuantity+1;
        if(!(temp>availableQuantity)){
            ++userQuantity;
            //Save in Shared preferences
            SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);

            //Editor
            SharedPreferences.Editor editor;
            editor = sharedPreferences.edit();
            editor.putInt(PRODUCT_KEY,userQuantity);
            editor.apply();

            //Set view
            quantitycount.setText(String.valueOf(userQuantity));
            updateInFirebase(userQuantity);
        }

        else
            {
            Toast.makeText(this, "maximum quantity reached", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkIfItemInCart() {

        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();

        userQuantity=sharedPreferences.getInt(PRODUCT_KEY,0);
        if(userQuantity>availableQuantity)
        {
            userQuantity=availableQuantity;
            updateInFirebase(userQuantity);
            editor.putInt(PRODUCT_KEY,userQuantity);
            editor.apply();
        }
    }

    void updateInFirebase(int userquantity) {

    cartDbRef=FirebaseInit.getDatabase().getReference().child("USERS/"+phonenumber);
    cartTotalDbRef=cartDbRef.child("TOTAL");
    if(userquantity!=0){
        cartDbRef.child("cart").child(PRODUCT_KEY).child("userquantity").setValue(userQuantity);
        cartDbRef.child("cart").child(PRODUCT_KEY).child("segment").setValue(SEGMENT_KEY);
        cartDbRef.child("cart").child(PRODUCT_KEY).child("productkey").setValue(PRODUCT_KEY);
        cartDbRef.child("cart").child(PRODUCT_KEY).child("productname").setValue(productName);
        cartDbRef.child("cart").child(PRODUCT_KEY).child("metrics").setValue(metrics);

    }
    else{
        cartDbRef.child("cart").child(PRODUCT_KEY).removeValue();
    }

    }

    private void getRegulationDataFromDB() {
        deliverRegulationDbRef=FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/administration");
        deliverRegulationDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: DELIVERY DATA SNAPSHOT::"+snapshot.toString());
                deliveryTimeInDays=snapshot.child("deliverytimeindays").getValue(Integer.class);
                deliveryHour=snapshot.child("deliveryhour").getValue(String.class);
                Log.d("TAG", "setViews: DELIVERY TIME IN DAYS::"+deliveryTimeInDays);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (productDbRef != null && productDbReflistener!=null) {
            productDbRef.removeEventListener(productDbReflistener);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        super.onStop();
        if (productDbRef != null && productDbReflistener != null) {
            productDbRef.removeEventListener(productDbReflistener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
