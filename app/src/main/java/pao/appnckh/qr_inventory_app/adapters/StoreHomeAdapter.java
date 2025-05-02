package pao.appnckh.qr_inventory_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.activitys.StoreDetailActivity;
import pao.appnckh.qr_inventory_app.models.Store;

public class StoreHomeAdapter extends RecyclerView.Adapter<StoreHomeAdapter.StoreViewHolder> {

    private List<Store> storeList;

    private Context context;
    public StoreHomeAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    public StoreHomeAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_home, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.tvStoreName.setText(store.getStoreName());

        // Truy vấn sản phẩm để đếm
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(store.getUserId())
                .child("Stores")
                .child(store.getStoreId())
                .child("Products");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int productCount = (int) snapshot.getChildrenCount();
                holder.tvTotalCount.setText("Số lượng Sản phẩm: " + productCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.tvTotalCount.setText("Lỗi tải số lượng");
            }
        });



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoreDetailActivity.class);
            intent.putExtra("storeId", store.getStoreId());
            intent.putExtra("userId", store.getUserId()); // bắt buộc phải có userId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvTotalCount;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvTotalCount = itemView.findViewById(R.id.tvTotalCount);
        }
    }
}
