package in.veggiefy.androidapp.ui.checkout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.ui.payment.Payment;
import in.veggiefy.androidapp.R;

public class Checkout extends AppCompatActivity {


    RadioButton radioButtonDelivery;
    RadioButton radioButtonPickup;
    TextView deliveryChargesText;
    TextView deliveryAddressText;
    TextView deliveryTimeText;
    TextView pickupTimeText;
    TextView pickupAddressText;
    ImageView backIcon;
    Button proceedToPay;
    DatabaseReference deliverRegulationDbRef;
    DatabaseReference pickupLocationDbRef;
    //Variables
    int deliveryCharge;
    String deliveryAddress;
    String deliveryHour;
    Integer deliveryTimeInDays;
    int pickupTimeInDays;
    String pickupAddress;
    String DC_KEY;
    int TOTAL_VALUE;
    boolean userChoseDelivery=false,isConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        //HOOKS
        radioButtonDelivery=findViewById(R.id.delivery_radio_button);
        radioButtonPickup=findViewById(R.id.pickup_radio_button);
        deliveryChargesText = findViewById(R.id.delivery_charges_textView);
        deliveryAddressText= findViewById(R.id.delivery_address_checkout_textView);
        proceedToPay= findViewById(R.id.proceed_to_pay_button);
        deliveryTimeText=findViewById(R.id.delivery_time_textView);
        pickupTimeText=findViewById(R.id.pickup_time_textView);
        pickupAddressText=findViewById(R.id.pickup_address_checkout_textView);
        backIcon=findViewById(R.id.back_icon_checkout_imageView);

        //BUNDLE
        Bundle bundle=getIntent().getExtras();
        TOTAL_VALUE=bundle.getInt("TOTAL");

        //SHARED PREFERERNCE
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        deliveryAddress=sharedPreferences.getString("ADDRESS","No address found");
        DC_KEY=sharedPreferences.getString("DCKEY","DC_1");

        //SET VIEWS
        //GET DATA ABOUT DELIVERY FROM DATABASE
        getDataFromDB();

        radioButtonDelivery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radioButtonDelivery.setChecked(true);
                radioButtonPickup.setChecked(false);
            }
        });

        radioButtonPickup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                radioButtonDelivery.setChecked(false);
                radioButtonPickup.setChecked(true);
            }
        });
        //back clicked
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //ProceedToPay button clicked
        proceedToPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    //INTENT
                    Intent intent=new Intent(Checkout.this, Payment.class);

                    if(radioButtonDelivery.isChecked()){
                        intent.putExtra("TOTAL",TOTAL_VALUE+deliveryCharge);
                        intent.putExtra("DELIVERY_TIME_IN_DAYS",deliveryTimeInDays);

                        userChoseDelivery=true;
                        Log.d("TAG", "onClick: CHECKED DELIVERY AND INSIDE BUNDLING");
                    }
                    else{
                        intent.putExtra("TOTAL", TOTAL_VALUE);
                        intent.putExtra("PICKUP_ADDRESS",pickupAddress);
                        userChoseDelivery=false;
                    }
                    intent.putExtra("DELIVERY",userChoseDelivery);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(Checkout.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void getDataFromDB() {
        deliverRegulationDbRef= FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/administration");
        deliverRegulationDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: DELIVERY DATA SNAPSHOT::"+snapshot.toString());
                pickupTimeInDays=snapshot.child("pickuptimeindays").getValue(Integer.class);
                deliveryCharge=snapshot.child("deliverycharge").getValue(Integer.class);
                deliveryTimeInDays=snapshot.child("deliverytimeindays").getValue(Integer.class);
                deliveryHour=snapshot.child("deliveryhour").getValue(String.class);
                Log.d("TAG", "setViews: DELIVERY TIME IN DAYS::"+deliveryTimeInDays);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        pickupLocationDbRef=FirebaseInit.getDatabase().getReference("MARKET/DISTRIBUTION_CENTRES/"+DC_KEY+"/details");
        pickupLocationDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pickupAddress = snapshot.child("address").getValue(String.class);
                setViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setViews() {
        if(deliveryCharge>0){
            deliveryChargesText.setText("Delivery charges : "+deliveryCharge);
        }
        else{
            deliveryChargesText.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
            deliveryChargesText.setText("Free delivery");
        }
        //Getting current time and adding hours value (from database) to current time

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

        //pick up time estimate
        Calendar calendar2=Calendar.getInstance();
        calendar2.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,pickupTimeInDays+1);
        Date pickupDate=calendar.getTime();
        SimpleDateFormat formatter2 = new SimpleDateFormat("EEE, dd MMM ");
        String dateString2 = formatter2.format(pickupDate);

        deliveryAddressText.setText(deliveryAddress);
        deliveryTimeText.setText("Get delivered on "+dateString+"between "+deliveryHour);
        pickupAddressText.setText(pickupAddress);
        pickupTimeText.setText("Please pick up your order from above address before "+dateString2);
    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Checkout checkout) {
        ConnectivityManager connectivityManager=(ConnectivityManager) checkout.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        isConnected= (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
        return isConnected;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}