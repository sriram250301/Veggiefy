package in.veggiefy.androidapp.ui.maindashboard.orders;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.veggiefy.androidapp.R;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersViewHolder> {

    private List<OrdersModel> order;

    public OrdersAdapter() {
        this.order = new ArrayList<>();
    }

    @NotNull
    @Override
    public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrdersViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(OrdersViewHolder holder, int position) {
        holder.setData(order.get(position));
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    public void addAll(List<OrdersModel> newOrders) {
        int initialSize = order.size();
        order.addAll(newOrders);
        notifyItemRangeInserted(initialSize, newOrders.size());
    }

    public String getLastItemId() {
        return order.get(order.size() - 1).getOrderid();

    }

}
