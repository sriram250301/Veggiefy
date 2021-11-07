package in.veggiefy.androidapp.ui.maindashboard.orders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

public class Orders extends AppCompatActivity {


    private RecyclerView mRV;
    private OrdersAdapter mAdapter;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading = false;
    private final int mPostsPerPage = 5;
    boolean isConnected=false;
    DatabaseReference ordersRef;
    ValueEventListener orderEventListener;
    ValueEventListener orderCheckListener;
    FirebaseRecyclerPagingAdapter firebaseRecyclerPagingAdapter;
    ImageView backIcon;
    ImageView placeHolderImage;
    TextView placeHolderText;
    //String
    String DEPOT_KEY,DC_KEY,phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        //HOOKS
        getHooks();
        //SHARED PREF
        getSharedPreferencesData();
        //views
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRV.setLayoutManager(mLayoutManager);

        mAdapter = new OrdersAdapter();
        checkForOrders();
//        getOrders(null);
        mRV.setAdapter(mAdapter);

        mRV.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && mTotalItemCount <= (mLastVisibleItemPosition + mPostsPerPage)) {
                    getOrders(mAdapter.getLastItemId());
                    mIsLoading = true;
                }
            }
        });
    }

    private void checkForOrders() {
        ordersRef=FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/orders");
        ordersRef.keepSynced(true);
        orderEventListener=ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    getOrders(null);
                }
                else{
                    Log.d("TAG", "onDataChange: ELSE IF ORDER SNAP::"+snapshot);
                    placeHolderImage.setVisibility(View.VISIBLE);
                    placeHolderText.setVisibility(View.VISIBLE);
                    placeHolderImage.setImageResource(R.mipmap.no_orders_foreground);
                    placeHolderText.setText("You have no open orders");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                    placeHolderImage.setVisibility(View.VISIBLE);
                    placeHolderText.setVisibility(View.VISIBLE);
                    placeHolderImage.setImageResource(R.mipmap.no_orders_foreground);
                    placeHolderText.setText("Something went wrong, try again later");
            }
        });
        if (ordersRef != null && orderCheckListener != null) {
            ordersRef.removeEventListener(orderCheckListener);
        }
    }

    private void getHooks() {
        mRV=findViewById(R.id.orders_recyclerview);
        backIcon=findViewById(R.id.back_icon_orders_imageView);
        placeHolderImage=findViewById(R.id.place_holder_orders_imageView);
        placeHolderText=findViewById(R.id.place_holder_orders_textView);
        //set visibility
        placeHolderImage.setVisibility(View.GONE);
        placeHolderText.setVisibility(View.GONE);
    }

    private void getSharedPreferencesData() {
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        phonenumber=sharedPreferences.getString("PHONE",null);
        DEPOT_KEY=sharedPreferences.getString("DEPOTKEY","D_1");
        DC_KEY=sharedPreferences.getString("DCKEY","DC_1");

    }

    private void getOrders(String nodeId) {
        Query query;

        if (nodeId == null)
            query = FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/orders")
                    .orderByKey()
                    .limitToFirst(mPostsPerPage);
        else
            query = FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/orders")
                    .orderByKey()
                    .startAfter(nodeId)
                    .limitToFirst(mPostsPerPage);

        orderEventListener=query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    OrdersModel order;
                    List<OrdersModel> orderModels = new ArrayList<>();
                    Log.d("TAG", "onDataChange: DATASNAP::"+snapshot);
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        if(orderSnapshot.child("details").exists() && orderSnapshot.child("details").getValue(OrdersModel.class)!=null){
                            Log.d("TAG", "onDataChange: ORDER SNAP::"+orderSnapshot);
                            Log.d("TAG", "onDataChange: ORDER SNAP ONLY DETAILS::"+orderSnapshot.child("details"));
                            if(!orderSnapshot.child("details").child("orderclosed").getValue(Boolean.class)){

                                orderModels.add(orderSnapshot.child("details").getValue(OrdersModel.class));

                            }
                        }
                    }
                    mAdapter.addAll(orderModels);
                    mIsLoading = false;
                }
                query.removeEventListener(orderEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                query.removeEventListener(orderEventListener);
            }
        });
//
    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Orders orders) {
        ConnectivityManager connectivityManager=(ConnectivityManager) orders.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        isConnected= (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
        return isConnected;
    }
}