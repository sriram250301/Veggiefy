package in.veggiefy.androidapp.ui.payment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ippopay.core.IppoPayListener;
import com.ippopay.core.IppoPayLog;
import com.ippopay.core.IppoPayPay;
import com.ippopay.models.OrderData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.ui.cart.Cart;
import in.veggiefy.androidapp.ui.login.newuser.Registered;

public class Payment extends AppCompatActivity implements IppoPayListener {

    //Database variables
    DatabaseReference cartDbRef;
    DatabaseReference depotSoldDbRef;
    DatabaseReference depotSoldTotalDbRef;
    DatabaseReference userOrdersDbRef;
    DatabaseReference regulationsDbRef;
    ValueEventListener cartlistener,regulationsDbRefListener;


    //View variables
    TextView totalAmounttextView;
    ImageView backIcon;
    Button proceedButton;
    RadioButton payNowRadioButton;
    RadioButton CODRadioButton;
    RequestQueue mRequestQueue;
    ProgressDialog progressDialog;
    //Variables
    String phonenumber;
    int TOTAL_AMOUNT;
    String ippopay_order_id,transaction_Id;
    //variables to place order
    String DEPOT_KEY,DC_KEY;
    String name,address,email,geohash;
    double lat,lang;
    String orderId,imageUrl;
    boolean isPaid=false,delivery=false,orderClosed=false,isConnected=false,onlinePayment=true;
    int deliveryTimeInDays;
    long timestamp;
    String deliveryDateString,pickupAddress,dateStringForNode ;
    int ordersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Log.d("TAG", "onCreate: __PAYMENT CLASS__");

        //HOOKS
        totalAmounttextView=findViewById(R.id.total_amount_payment_textView);
        payNowRadioButton=findViewById(R.id.paynow_radio_button);
        CODRadioButton=findViewById(R.id.cod_radio_button);
        proceedButton=findViewById(R.id.proceed_to_pay_button);
        backIcon=findViewById(R.id.back_icon_payment_imageView);

        //BUNDLE
        getBundleData();
        //SHARED PREFERENCE
        getSharedPreferencesData();
        //SET VIEWS
        totalAmounttextView.setText(String.format("TOTAL = ₹%d", TOTAL_AMOUNT));

        //CLICK EVENTS
        payNowRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                payNowRadioButton.setChecked(true);
                CODRadioButton.setChecked(false);
            }
        });
        CODRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                payNowRadioButton.setChecked(false);
                CODRadioButton.setChecked(true);
            }
        });
        //back clicked
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    if(payNowRadioButton.isChecked()){
                        if(onlinePayment){
                            //Set ProgressDialog
                            progressDialog=new ProgressDialog(Payment.this);
                            //show progressDialog
                            progressDialog.show();
                            //set contentView for progressDialog
                            progressDialog.setContentView(R.layout.progress_dialog);
                            //set transparent background
                            progressDialog.getWindow().setBackgroundDrawableResource(
                                    android.R.color.transparent);
                            callHttpCloudFunction();
                        }
                        else{
                            new AlertDialog.Builder(Payment.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Oops!")
                                    .setMessage("We are currently not accepting online payment please continue with pay-on-delivery method,We apologise for the inconvenience")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                    else{
                        getLocationDataFromFirebase();
                        Log.d("TAG", "onClick: PROCEED WITH COD");
                    }
                }
                else{
                    Toast.makeText(Payment.this, "Please check your connection", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void getBundleData() {
        Bundle bundle=getIntent().getExtras();
        TOTAL_AMOUNT=bundle.getInt("TOTAL");
        delivery=bundle.getBoolean("DELIVERY");
        Log.d("TAG", "getBundleData: BUNDLE DATA:: DELIVERY BOOLEAN"+delivery);
        deliveryTimeInDays=bundle.getInt("DELIVERY_TIME_IN_DAYS");
        pickupAddress=bundle.getString("PICKUP_ADDRESS");

    }

    private void getSharedPreferencesData() {
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        phonenumber=sharedPreferences.getString("PHONE",null);
        DEPOT_KEY=sharedPreferences.getString("DEPOTKEY","D_1");
        DC_KEY=sharedPreferences.getString("DCKEY","DC_1");
        name=sharedPreferences.getString("NAME",phonenumber);
        address=sharedPreferences.getString("ADDRESS",null);
        email=sharedPreferences.getString("EMAIL",null);
        imageUrl=sharedPreferences.getString("IMAGEURL",null);
 /*       lat=Double.valueOf(sharedPreferences.getString("LAT",null));
        lang=Double.valueOf(sharedPreferences.getString("LANG",null));*/
//        lat=Long.parseLong(sharedPreferences.getString("LAT",null));
//        lang=Long.parseLong(sharedPreferences.getString("LANG",null));
//        Log.d("TAG", "getSharedPreferencesData: LAT AND LANG FROMSHRD PREF FOR PLACING ORDER"+lat+",LANG:"+lang);
    }

    void callHttpCloudFunction() {
        mRequestQueue = Volley.newRequestQueue(Payment.this);
        String url="https://us-central1-veggiefy-d9aa4.cloudfunctions.net/order?amount="+TOTAL_AMOUNT+"&name="+name+"&phone="+phonenumber+"&email="+email;
        JsonObjectRequest mCloudRequest=new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    JSONObject orderdata = data.getJSONObject("order");
                    Log.e("TAG", "Response received:"+ response);
                    ippopay_order_id=orderdata.getString("order_id");
                    String createdtime=orderdata.getString("createdAt");
                    String amount=orderdata.getString("amount");
                    String currency=orderdata.getString("currency");
                    String message=response.getString("message");
                    onPaymentClick();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //order_data.setText(String.format(Locale.getDefault(),"%s", error.getMessage()));
            }
        });
        mRequestQueue.add(mCloudRequest);
    }

    private void onPaymentClick() {
        try {
            IppoPayLog.setLogVisible(true);
            IppoPayPay.init(this, "pk_live_0WZhCNC5l7PJ");
            Log.d("onpayment clicked", "yes");
            OrderData orderData = new OrderData();
            orderData.setOrderId(ippopay_order_id);
            orderData.setCustomColor("#454545");

            orderData.setFont(ResourcesCompat.getFont(this, R.font.fredoka_one));
            IppoPayPay.setPaymentListener(this);
            IppoPayPay.makePayment(orderData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IppoPayLog.setLogVisible(true);
    }

    @Override
    public void onTransactionSuccess(String transactionId) {
        progressDialog.dismiss();
        isPaid=true;
        transaction_Id=transactionId;
        Toast.makeText(this, "payment sucessful :"+transactionId, Toast.LENGTH_LONG).show();
//
        getLocationDataFromFirebase();
    }

    private void getLocationDataFromFirebase() {
        DatabaseReference userDbRef=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/profile/business/location/l");
        userDbRef.keepSynced(true);
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lat=snapshot.child("0").getValue(Double.class);
                lang=snapshot.child("1").getValue(Double.class);
                Log.d("TAG", "onDataChange: LAT--LANG:::RECENT::"+lat+","+lang);
                placeOrder();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onTransactionFailure(String error, String transaction_id) {
        progressDialog.dismiss();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancelled() {
        progressDialog.dismiss();
        Toast.makeText(this, "payment cancelled", Toast.LENGTH_LONG).show();

    }

    private void placeOrder() {
        Log.d("TAG", "placeOrder: EARLIER NO DELIVERY!!"+pickupAddress);
        Bundle bundle=getIntent().getExtras();
        pickupAddress=bundle.getString("PICKUP_ADDRESS");

        Log.d("TAG", "onDataChange: PLACING ORDER::"+pickupAddress);
        //TIMESTAMPS
        getTimeStamp();
        //
        depotSoldDbRef= FirebaseInit.getDatabase().getReference("MARKET/DEPOTS/"+DEPOT_KEY+"/details/sold/DC/"+DC_KEY+"/"/*+dateStringForNode+"/"*/+phonenumber);
        depotSoldTotalDbRef=FirebaseInit.getDatabase().getReference("MARKET/DEPOTS/"+DEPOT_KEY+"/details/sold/TOTAL");
        userOrdersDbRef= FirebaseInit.getDatabase().getReference().child("USERS/"+phonenumber+"/orders");

        orderId=depotSoldDbRef.push().getKey();
        //FOR CHILD "DETAILS"
        HashMap<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("name", name);
        orderDetails.put("paystatus", isPaid);
        orderDetails.put("orderid",orderId);
        orderDetails.put("amount",TOTAL_AMOUNT);
        orderDetails.put("shopimage",imageUrl);
        orderDetails.put("ordertime",getTimeStamp());
        orderDetails.put("delivery",delivery);
        orderDetails.put("orderclosed",orderClosed);
        orderDetails.put("phonenumber",phonenumber);
        Log.d("TAG", "onDataChange: PLACING ORDER:: DELIVERY BOOLEAN"+delivery);
        if(!delivery){
            orderDetails.put("address",pickupAddress);
            Log.d("TAG", "placeOrder: NO DELIVERY!!"+pickupAddress);
        }
        else{
            orderDetails.put("address",address);
        }
        if(delivery){
            orderDetails.put("deliverydate",deliveryDateString);
        }
        if(isPaid){
            orderDetails.put("transactionid",transaction_Id);
        }
        else{
            orderDetails.put("transactionid",null);
        }
        depotSoldDbRef.child(orderId+"/details").setValue(orderDetails); //depot node
        userOrdersDbRef.child(orderId+"/details").setValue(orderDetails); //user node
        //location Marker
        HashMap<String, Object> locationMarker = new HashMap<>();
        locationMarker.put("0",lat);
        locationMarker.put("1",lang);
        depotSoldDbRef.child(orderId+"/details/location/l").setValue(locationMarker);          //depot node
        depotSoldDbRef.child(orderId+"/details/location/g").setValue("geohash",geohash); //depot node
        userOrdersDbRef.child(orderId+"/details/location/l").setValue(locationMarker);         //user node
        userOrdersDbRef.child(orderId+"/details/location/g").setValue("geohash",geohash);//user node


        //FOR CHILD "ITEMS"
        itemsInOrder();

        //Empty cart and set total amount value back to zero
        DatabaseReference cartTotalRef=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber);
        cartTotalRef.child("cart").removeValue();
        allTaskDone();
        //Empty Shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    private String getTimeStamp() {
        // veggiefy regulations
        datafromRegulations();
        //
        long time= System.currentTimeMillis();
        timestamp=-1*time;
        Date date=new Date(time);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,1);
//        deliveryDate=calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy,  hh:mm aa");
        String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(time))));
        //for order node
        SimpleDateFormat ordernodeformatter = new SimpleDateFormat("dd-MMM-yyyy");
        dateStringForNode = ordernodeformatter.format(new Date(Long.parseLong(String.valueOf(time))));
        Log.d("TAG", "getTimeStamp: DATE STAMP for node!!!!!!:"+dateStringForNode);

        //delivery time estimate
        long time2= System.currentTimeMillis();
        Date date2=new Date(time2);
        Calendar calendar2=Calendar.getInstance();
        calendar2.setTime(date2);
        Log.d("TAG", "setViews: DELIVERY TIME IN DAYS::"+deliveryTimeInDays);
        calendar2.add(Calendar.DAY_OF_MONTH,deliveryTimeInDays);
        Date deliveryDate=calendar.getTime();
        SimpleDateFormat formatter2 = new SimpleDateFormat("EEE, dd MMM ");
        deliveryDateString= formatter2.format(deliveryDate);

        return dateString;
    }

    private void datafromRegulations() {

        regulationsDbRef=FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/administration");
        regulationsDbRef.keepSynced(true);
        Log.d("TAG", "onDataChange: PLACING ORDER:: *******ITEMS****  BEFORE LISTENER ");
        regulationsDbRefListener=regulationsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: REGULATION SNAP::"+snapshot);
                onlinePayment=snapshot.child("acceptonlinepayment").getValue(Boolean.class);
                regulationsDbRef.removeEventListener(regulationsDbRefListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                regulationsDbRef.removeEventListener(regulationsDbRefListener);
            }
        });
    }

    private void itemsInOrder() {
        cartDbRef=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/cart");
        Log.d("TAG", "onDataChange: PLACING ORDER:: *******ITEMS****  BEFORE LISTENER ");
        cartlistener=cartDbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Log.d("TAG", "onDataChange: ITEMS CHILD SNAP::"+snapshot.toString());
                    String prodKey=dataSnapshot.getKey();
                    String prodName=dataSnapshot.child("productname").getValue(String.class);
                    String segment=dataSnapshot.child("segment").getValue(String.class);
                    String metrics=dataSnapshot.child("metrics").getValue(String.class);
                    int quantity=dataSnapshot.child("userquantity").getValue(Integer.class);
                    depotSoldDbRef=FirebaseInit.getDatabase().getReference("MARKET/DEPOTS/"+DEPOT_KEY+"/details/sold/DC/"+DC_KEY+"/"/*+dateStringForNode+"/"*/+phonenumber+"/"+orderId+"/items/"+prodKey);
                    //individual orders
                    depotSoldDbRef.child("productname").setValue(prodName);
                    depotSoldDbRef.child("userquantity").setValue(quantity);
                    depotSoldDbRef.child("metrics").setValue(metrics);
                    //total sold in DEPOT
                    depotSoldTotalDbRef.child(prodName).child("productname").setValue(prodName);
                    depotSoldTotalDbRef.child(prodName).child("userquantity").setValue(quantity);
                    depotSoldTotalDbRef.child(prodName).child("metrics").setValue(metrics);
                    //user node
                    userOrdersDbRef.child(orderId+"/items/"+prodKey+"/productname").setValue(prodName);
                    userOrdersDbRef.child(orderId+"/items/"+prodKey+"/userquantity").setValue(quantity);
                    userOrdersDbRef.child(orderId+"/items/"+prodKey+"/metrics").setValue(metrics);
                    updateStockInDatabase(prodKey, quantity,segment);
                    Log.d("TAG --PAYMENT", "onDataChange: PRODKEY,PRODNAME,QUANTITY"+prodKey+"NAME::"+prodName+"QUANTITY::"+quantity);
                }
                if(cartDbRef!=null && cartlistener!=null){
                    cartDbRef.removeEventListener(cartlistener);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Payment.this, "CANCELLED!", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("TAG", "itemsInOrder: FINISHED FUNCTION!");
    }

    private void updateStockInDatabase(String prodKey, int quantity, String segment){
        Log.d("TAG", "UPDATE STOCK IN DB: BEFORE UPDT");
        DatabaseReference stockRef=FirebaseInit.getDatabase().getReference("MARKET/DEPOTS/"+DEPOT_KEY+"/details/stock/TOTAL/"+segment+"/"+prodKey);
        stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availQnty=snapshot.child("quantity").getValue(Integer.class);
                Log.d("TAG", "onDataChange: AVAILABLE QUANTITY::"+availQnty+"USER QNTY::"+quantity);
                if(availQnty>=quantity){
                    stockRef.child("quantity").setValue(availQnty-quantity);
                }
                else{
                    stockRef.child("quantity").setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Payment payment) {
        ConnectivityManager connectivityManager=(ConnectivityManager) payment.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        isConnected= (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
        return isConnected;
    }

    private void allTaskDone() {

        Intent intent=new Intent(Payment.this, Registered.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("orderplaced",true);
        startActivity(intent);
        Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
    }

}