package in.veggiefy.androidapp.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.addtocart.AddToCart;

public class CartAdapter  extends FirebaseRecyclerAdapter<CartModel, CartAdapter.CartHolder> {


    DatabaseReference productDbRef;
    ValueEventListener productReflistener;
    String DEPOT_KEY;
    String phoneNumber;
    String SEGMENT_KEY;
    String PRODUCT_KEY;
    Integer userquantity;


    public CartAdapter(FirebaseRecyclerOptions<CartModel> options,String phonenumber,String DEPOT_KEY) {
        super(options);
        this.DEPOT_KEY=DEPOT_KEY;
        this.phoneNumber=phonenumber;
    }

    @Override
    protected void onBindViewHolder(@NonNull CartHolder holder, int position, @NonNull CartModel model) {


        SEGMENT_KEY=model.getSegment();
        PRODUCT_KEY= model.getProductkey();

        //
        productDbRef= FirebaseInit.getDatabase().getReference().child("MARKET/DEPOTS/"+DEPOT_KEY+"/details/stock/TOTAL/"+SEGMENT_KEY+"/"+PRODUCT_KEY);
        productReflistener=productDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String prodname,imglink,mtrcs;
                    int price,avlqnty,sumPrice=0;
                    userquantity=model.getUserquantity();
                    prodname=snapshot.child("productname").getValue(String.class);
                    imglink=snapshot.child("imagelink").getValue(String.class);
                    mtrcs=snapshot.child("metrics").getValue(String.class);

                    Log.d("TAG", "onDataChange: CARD ADAPTER SNAP 11::USERQNTY-->"+userquantity+"METRICS-->"+mtrcs);

                    price=snapshot.child("price").getValue(Integer.class);
                    avlqnty=snapshot.child("quantity").getValue(Integer.class);

                    holder.productName.setText(prodname);
                    holder.price.setText("₹ "+price+"/"+mtrcs);
                    holder.availableQuantity.setText(""+avlqnty+""+mtrcs+" available");
                    if(userquantity>avlqnty){
                        userquantity=avlqnty;
                        holder.userQuantity.setText("X "+userquantity+""+mtrcs);
                    }
                    else{
                        holder.userQuantity.setText("X "+userquantity+""+mtrcs);
                    }
                    sumPrice=userquantity*price;
                    holder.sumUpPrice.setText(String.format("= ₹%d", sumPrice));


                    if (!((Activity) holder.itemView.getContext()).isFinishing()){
                        Glide.with(holder.itemView.getContext()).load(imglink).placeholder(R.mipmap.placeholder_png_foreground).into(holder.productImage);
                    }

                    //Event
                    holder.productClickButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(holder.itemView.getContext(), AddToCart.class);
                            intent.putExtra("productname",getItem(position).getProductkey());
                            intent.putExtra("segment",getItem(position).getSegment());
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }
                else{
                    holder.productName.setText(""+model.getProductname());
                    holder.price.setText(" ");
                    holder.availableQuantity.setText("Stock unavailable");
                    holder.userQuantity.setText("X 0");
                    if (!((Activity) holder.itemView.getContext()).isFinishing())
                        holder.productImage.setImageResource(R.mipmap.placeholder_png_foreground);
                    //Event
                    holder.productClickButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(holder.itemView.getContext(),AddToCart.class);
                            intent.putExtra("productname",getRef(position).getKey());
                            intent.putExtra("segment",SEGMENT_KEY);
                            holder.itemView.getContext().startActivity(intent);
                            ((Activity) holder.itemView.getContext()).finish();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.itemView.getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });


        Log.d("TAG", "onDataChange: CART ADAPTER AVL QUANTITY LAST::"+userquantity);
        if(((Activity) holder.itemView.getContext()).isDestroyed() || ((Activity) holder.itemView.getContext()).isFinishing() ){
            productDbRef.removeEventListener(productReflistener);
        }
    }

    @NonNull
    @Override
    public CartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout,parent,false);


        return new CartAdapter.CartHolder(view);
    }

    public class CartHolder extends RecyclerView.ViewHolder{

        TextView productName,price,availableQuantity,userQuantity,sumUpPrice;
        ImageView productImage;
        Button productClickButton;

        public CartHolder(@NonNull View itemView) {
            super(itemView);

            //HOOKS
            productName=itemView.findViewById(R.id.product_name_cart_textview);
            price=itemView.findViewById(R.id.price_cart_textview);
            availableQuantity=itemView.findViewById(R.id.available_quantity_cart_textview);
            userQuantity=itemView.findViewById(R.id.user_quantity_count_cart_textview);
            productName=itemView.findViewById(R.id.product_name_cart_textview);
            productImage=itemView.findViewById(R.id.product_cart_imageView);
            productClickButton=itemView.findViewById(R.id.product_click_cart_button);
            sumUpPrice=itemView.findViewById(R.id.sum_up_price_cart_textView);
        }
    }



}
