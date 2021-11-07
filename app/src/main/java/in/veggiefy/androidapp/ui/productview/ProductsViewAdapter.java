package in.veggiefy.androidapp.ui.productview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import in.veggiefy.androidapp.ui.addtocart.AddToCart;
import in.veggiefy.androidapp.R;

public class ProductsViewAdapter extends FirebaseRecyclerAdapter<ProductsViewModel,ProductsViewAdapter.ProductsViewHolder> {

    //Variables
    String SEGMENT_KEY;

    public ProductsViewAdapter(FirebaseRecyclerOptions<ProductsViewModel> options, String segment_key) {
        super(options);
        this.SEGMENT_KEY=segment_key;
        Log.d("*****TAG****", "ProductsViewAdapter: ");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ProductsViewHolder holder, int position, @NonNull ProductsViewModel model) {

        //Binding..
        holder.productname.setText(""+model.getProductname());
        holder.price.setText("₹"+model.getPrice()+"/"+model.getMetrics());
        holder.availablequantity.setText(""+model.getQuantity()+""+model.getMetrics()+" available");
        holder.specialTextView.setText(""+model.getSpecialtext());
        if (!((Activity) holder.itemView.getContext()).isFinishing()){
            Glide.with(holder.itemView.getContext()).load(model.getImageLink()).placeholder(R.mipmap.placeholder_png_foreground).into(holder.productImage);
        }
        Log.d("****TAG***", "onBindViewHolder: ");
        //Event
        holder.productSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(holder.itemView.getContext(),AddToCart.class);
                intent.putExtra("productname",getRef(position).getKey());
                intent.putExtra("segment",SEGMENT_KEY);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout,parent,false);
        return new ProductsViewHolder(view);
    }

    class ProductsViewHolder extends RecyclerView.ViewHolder{

        ImageView productImage;
        TextView productname,price,metrics,availablequantity,specialTextView;
        Button productSelectButton;
        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);
            //TextView(s)
            productname=itemView.findViewById(R.id.product_name_cart_textview);
            price=itemView.findViewById(R.id.price_cart_textview);
            availablequantity=itemView.findViewById(R.id.available_quantity_cart_textview);
            specialTextView=itemView.findViewById(R.id.user_quantity_count_cart_textview);
            //Button
            productSelectButton=itemView.findViewById(R.id.product_click_cart_button);
            //Image
            productImage=itemView.findViewById(R.id.product_cart_imageView);

            Log.d("******TAG******", "ProductsViewHolder: ");
        }
    }
}
