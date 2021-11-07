package in.veggiefy.androidapp.ui.maindashboard.orders.orderview;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.cart.Cart;

public class OrderView extends AppCompatActivity {

    DatabaseReference ordersRef;
    RecyclerView itemsRecyclerView;
    OrderViewAdapter orderViewAdapter;
    ImageView backIcon,trashIcon;
    //Variables
    String phonenumber;
    String ORDER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);

        //HOOKS
        itemsRecyclerView=findViewById(R.id.order_items_orderview_recyclerview);
        backIcon=findViewById(R.id.backicon_orderview_imageView);
        trashIcon=findViewById(R.id.trash_icon_orders_view_imageView);

        //BUNDLE
        Bundle bundle=getIntent().getExtras();
        ORDER_ID=bundle.getString("ORDER_ID");

        //SHARED PREFERENCE
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);

        phonenumber=sharedPreferences.getString("PHONE",null);

        //Firebase
        ordersRef= FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/orders/"+ORDER_ID);
        //Firebase option query
        Log.d("TAG", "onCreate: BEFORE ITEMS FIREBASE OPTIONS");
        FirebaseRecyclerOptions<OrderViewModel> options =
                new FirebaseRecyclerOptions.Builder<OrderViewModel>()
                        .setQuery(ordersRef.child("items"), OrderViewModel.class)
                        .build();

        orderViewAdapter=new OrderViewAdapter(options);
        itemsRecyclerView.setAdapter(orderViewAdapter);

        //click
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //trash clicked
        /*trashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(OrderView.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm deletion")
                        .setMessage("You sure you want to delete this order?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteOrder();
                            }
                        })
                        .setNegativeButton("NO",null)
                        .show();
            }
        });*/
    }

    /*private void deleteOrder() {
        DatabaseReference userNodeOrderRef,depotNodeDCOrdersRef,depotNodeTotalRef;
        userNodeOrderRef=FirebaseInit.getDatabase().getReference("");
        depotNodeDCOrdersRef=FirebaseInit.getDatabase().getReference("");
        depotNodeTotalRef=FirebaseInit.getDatabase().getReference("");
    }
*/
    @Override
    protected void onStart() {
        super.onStart();
        orderViewAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orderViewAdapter.stopListening();
    }
}