package in.veggiefy.androidapp.ui.maindashboard.orders.orderview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.data.LoginDataSource;
import in.veggiefy.androidapp.ui.cart.CartAdapter;
import in.veggiefy.androidapp.ui.cart.CartModel;

public class OrderViewAdapter extends FirebaseRecyclerAdapter<OrderViewModel, OrderViewAdapter.OrderViewViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public OrderViewAdapter(@NonNull FirebaseRecyclerOptions<OrderViewModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull OrderViewViewHolder holder, int position, @NonNull OrderViewModel model) {
        Log.d("TAG", "onBindViewHolder: INSIDE BINDVHOLDER");
        holder.productNameText.setText(""+model.getProductname());
        Log.d("TAG", "onBindViewHolder: INSIDE BINDVHOLDER2");
        String quantity= String.valueOf(model.getUserquantity());
        holder.userQuantityText.setText(""+quantity+" "+model.getMetrics());Log.d("TAG", "OrderbINDHolder :: prodname , qnty , metrics "+model.getProductname()+","+model.getUserquantity()+","+model.getMetrics());
    }

    @NonNull
    @Override
    public OrderViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items_layout_orderview,parent,false);
        return new OrderViewViewHolder(view);
    }

    public class OrderViewViewHolder extends RecyclerView.ViewHolder{

        TextView productNameText,userQuantityText;


        public OrderViewViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText=itemView.findViewById(R.id.productname_orderview_textView);
            userQuantityText=itemView.findViewById(R.id.product_quantity_orderview_textView);Log.d("TAG", "OrderViewViewHolder :: FOUND VIEWS ");
        }
    }
}
