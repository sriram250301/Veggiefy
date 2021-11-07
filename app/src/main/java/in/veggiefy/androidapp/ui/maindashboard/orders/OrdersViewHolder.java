package in.veggiefy.androidapp.ui.maindashboard.orders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ippopay.models.PaymentMode;

import dagger.multibindings.ElementsIntoSet;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.ui.addtocart.AddToCart;
import in.veggiefy.androidapp.ui.maindashboard.orders.orderview.OrderView;

public class OrdersViewHolder extends RecyclerView.ViewHolder {

    TextView orderIdText,orderDateText,orderValueText,payStatusText,addressTitleText,orderAddressText,orderStatusText,transactionIdText;
    ImageView rightArrowIcon,trashIcon;
    //
    DatabaseReference ordersRef;
    ValueEventListener ordersRefListener;
    //String
    String DEPOT_KEY,DC_KEY,phonenumber;
    public OrdersViewHolder(@NonNull View itemView) {
        super(itemView);
        findViews(itemView);
    }
    private void findViews(View view) {

        orderIdText=view.findViewById(R.id.order_id_textview);
        orderDateText=view.findViewById(R.id.order_date_textview);
        orderValueText=view.findViewById(R.id.order_value_textview);
        payStatusText=view.findViewById(R.id.paystatus_textView);
        addressTitleText=view.findViewById(R.id.address_title_orders_textView);
        orderAddressText=view.findViewById(R.id.address_orders_textView);
        orderStatusText=view.findViewById(R.id.order_status_textview);
        transactionIdText=view.findViewById(R.id.transaction_id_orders_textView);
        rightArrowIcon=view.findViewById(R.id.right_arrow_imageView);
        trashIcon=view.findViewById(R.id.trash_icon_orders_imageView);

    }

    public void setData(OrdersModel ordersModel) {

        boolean paid,delivery,orderclosed;
//        String amount= String.valueOf(ordersModel.getAmount());
//        Log.d("TAG", "setData: PAID::"+ String.valueOf(ordersModel.isPaystatus())+"DELIVERY::"+String.valueOf(ordersModel.isDelivery())+"ORDER CLOSED::"+ordersModel.isOrderclosed()+"ORDER ID ::"+ordersModel.getOrderid()+"AMOUNT INT"+ordersModel.getAmount());
        orderIdText.setText("ID :"+ordersModel.getOrderid());
        orderDateText.setText(ordersModel.getOrdertime());
        orderValueText.setText("₹."+ordersModel.getAmount());
        orderAddressText.setText(ordersModel.getAddress());
        //conditional

        paid=ordersModel.isPaystatus();
        if(paid){
            payStatusText.setText("Paid");
            transactionIdText.setText("id : "+ordersModel.getTransactionid());
        }
        else{
            payStatusText.setText("Unpaid");
//            transactionIdText.setText("");
        }
        //
        delivery=ordersModel.isDelivery();
        if(delivery){
            addressTitleText.setText("DELIVERY ADDRESS");
        }
        else{
            addressTitleText.setText("PICK UP AT");
        }
        //
        orderclosed=ordersModel.isOrderclosed();
        if(orderclosed){
            orderStatusText.setText("Order closed");
        }
        else{
            orderStatusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.teal_700));
            orderStatusText.setText("Live");
        }


        rightArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(itemView.getContext(), OrderView.class);
                intent.putExtra("ORDER_ID",ordersModel.getOrderid());
                Log.d("TAG", "onClick: ORDER_ID::"+ordersModel.getOrderid());
                itemView.getContext().startActivity(intent);
            }
        });

    }
}
