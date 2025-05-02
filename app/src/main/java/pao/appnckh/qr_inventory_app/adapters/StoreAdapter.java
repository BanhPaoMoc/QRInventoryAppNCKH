package pao.appnckh.qr_inventory_app.adapters;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.activitys.StoreDetailActivity;
import pao.appnckh.qr_inventory_app.models.Store;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private Context context;
    private List<Store> storeList ;
    private OnStoreActionListener onStoreActionListener;

    public StoreAdapter(Context context, List<Store> storeList, OnStoreActionListener onStoreActionListener) {
        this.context = context;
        this.storeList = storeList;
        this.onStoreActionListener = onStoreActionListener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view, onStoreActionListener);
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
                holder.totalCountTextView.setText("Số lượng Sản phẩm: " + productCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.totalCountTextView.setText("Lỗi tải số lượng");
            }
        });



        // Xử lý sự kiện xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (onStoreActionListener != null) {
                onStoreActionListener.onDeleteStore(store, position);
            }
        });

        // Xử lý sự kiện đổi tên
        holder.btnEdit.setOnClickListener(v -> {
            if (onStoreActionListener != null) {
                onStoreActionListener.onEditStore(store, position);
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
        TextView tvStoreName, totalCountTextView;
        ImageButton btnDelete, btnEdit;

        public StoreViewHolder(@NonNull View itemView, OnStoreActionListener onStoreActionListener) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            totalCountTextView = itemView.findViewById(R.id.totalCountTextView);

        }
    }

    public interface OnStoreActionListener {
        void onStoreClicked(Store store);
        void onEditStore(Store store, int position);
        void onDeleteStore(Store store, int position);  // Phương thức cần implement
    }


}