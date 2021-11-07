package in.veggiefy.androidapp.ui.productview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

public class ProductsView extends AppCompatActivity {


    TextView TitleSegmentText;
    RecyclerView recyclerView;
    ImageView placeHolderImage;
    //Invisible for no connection
    TextView noConnectionText;
    Button retryConnectionButton;
    ProgressDialog progressDialog;
    DatabaseReference dbDepotRef;
    DatabaseReference stockRef;
    ProductsViewAdapter adapter;
    ValueEventListener checkProdRefListener;
    //Variables
    public String SEGMENT_KEY;
    public String DC_KEY;
    public String DEPOT_KEY;
    //    static boolean calledAlready = false;
    static boolean depotKeyRecieved = false,isConnected=false,adapterSet=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_view);

        //HOOKS
        TitleSegmentText = findViewById(R.id.segment_title);
        recyclerView = findViewById(R.id.product_items_recyclerView);
        placeHolderImage = findViewById(R.id.placae_holder_productsView_imageView);
        noConnectionText = findViewById(R.id.no_connection_productsView_textView);
        retryConnectionButton = findViewById(R.id.retry_connection_productsView_button);

        //Get segment key from bundle
        Bundle bundle = getIntent().getExtras();
        SEGMENT_KEY = bundle.getString("segment");

        //SHARED PREFERENCE (reading Distribution Centre Key)
        SharedPreferences sharedPreferences = getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        DC_KEY = sharedPreferences.getString("DCKEY", "DC_1");
        DEPOT_KEY = sharedPreferences.getString("DEPOTKEY", "D_1");

        //Set title
        TitleSegmentText.setText(SEGMENT_KEY);
        if(!isConnected()){
            //visiblity set
            placeHolderImage.setVisibility(View.VISIBLE);
            noConnectionText.setVisibility(View.VISIBLE);
            retryConnectionButton.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            placeHolderImage.setImageResource(R.mipmap.no_connection_png_foreground);
            retryConnectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = getIntent();
                    finish();
                    startActivity(intent);*/
                    recreate();
                }
            });
        }
        else{
            //Set ProgressDialog
            progressDialog=new ProgressDialog(ProductsView.this);
            //show progressDialog
            progressDialog.show();
            //set contentView for progressDialog
            progressDialog.setContentView(R.layout.progress_dialog);
            //set transparent background
            progressDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            //Firebase option query
            stockRef = FirebaseInit.getDatabase().getReference().child("MARKET/DEPOTS/" + DEPOT_KEY + "/details/stock/TOTAL");
            stockRef.keepSynced(true);
            Log.d("***AFTER STOCK REF****", "SEGMENT KEY" + SEGMENT_KEY);
            FirebaseRecyclerOptions<ProductsViewModel> options =
                    new FirebaseRecyclerOptions.Builder<ProductsViewModel>()
                            .setQuery(stockRef.child(SEGMENT_KEY).orderByChild("priority"), ProductsViewModel.class)
                            .build();
            //Pass adapter
            adapterSet=true;
            adapter = new ProductsViewAdapter(options, SEGMENT_KEY);
            recyclerView.setAdapter(adapter);
            progressDialog.dismiss();
            checkProdRefListener=stockRef.child(SEGMENT_KEY).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        placeHolderImage.setVisibility(View.VISIBLE);
                        placeHolderImage.setImageResource(R.mipmap.out_of_stock_png_foreground);
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    placeHolderImage.setImageResource(R.mipmap.out_of_stock_png_foreground);
                    progressDialog.dismiss();
                }
            });
        }

    }
    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(ProductsView productsView) {
        ConnectivityManager connectivityManager=(ConnectivityManager) productsView.getSystemService(Context.CONNECTIVITY_SERVICE);

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

    @Override
    protected void onStart() {
        super.onStart();
        if(isConnected){
            if(adapterSet) adapter.startListening();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isConnected){
            if(adapterSet) adapter.stopListening();
        }
    }
}
