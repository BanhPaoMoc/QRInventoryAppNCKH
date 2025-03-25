package pao.appnckh.qr_inventory_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Store;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private List<Store> storeList;
    private OnStoreActionListener onStoreActionListener;

    public StoreAdapter(List<Store> storeList, OnStoreActionListener onStoreActionListener) {
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
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName;
        ImageButton btnDelete, btnEdit;

        public StoreViewHolder(@NonNull View itemView, OnStoreActionListener onStoreActionListener) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public interface OnStoreActionListener {
        void onDeleteStore(Store store, int position);
        void onEditStore(Store store, int position);
    }
}