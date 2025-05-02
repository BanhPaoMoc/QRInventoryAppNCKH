package pao.appnckh.qr_inventory_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Store;

public class StoreHomeAdapter extends RecyclerView.Adapter<StoreHomeAdapter.StoreViewHolder> {

    private List<Store> storeList;

    public StoreHomeAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.tvStoreName.setText(store.getStoreName());
        holder.tvTotalCount.setText("Số lượng: " + store.getTotalCount());
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
