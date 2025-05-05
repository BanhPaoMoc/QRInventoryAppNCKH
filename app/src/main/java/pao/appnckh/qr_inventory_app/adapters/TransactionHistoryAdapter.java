package pao.appnckh.qr_inventory_app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.TransactionHistory;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {

    private List<TransactionHistory> transactionList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public TransactionHistoryAdapter(List<TransactionHistory> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionHistory transaction = transactionList.get(position);

        // Format timestamp to readable date
        String formattedDate = dateFormat.format(new Date(transaction.getTimestamp()));

        // Set transaction type with appropriate color
        String transactionType = transaction.isImport() ? "Nhập kho: " : "Xuất kho: ";
        holder.txtTransactionType.setText(transactionType);
        holder.txtTransactionType.setTextColor(transaction.isImport() ?
                Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        // Set values to views
        holder.txtTransactionQuantity.setText(String.valueOf(transaction.getQuantity()));
        holder.txtProductName.setText("Sản phẩm: " + transaction.getProductName());
        holder.txtStoreName.setText("Kho: " + transaction.getStoreName());
        holder.txtTransactionDate.setText(formattedDate);
        holder.txtResultCount.setText("Tồn: " + transaction.getResultCount());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtTransactionType;
        TextView txtTransactionDate;
        TextView txtTransactionQuantity;
        TextView txtResultCount;
        TextView txtStoreName;
        TextView txtProductName;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTransactionType = itemView.findViewById(R.id.txtTransactionType);
            txtTransactionDate = itemView.findViewById(R.id.txtTransactionDate);
            txtTransactionQuantity = itemView.findViewById(R.id.txtTransactionQuantity);
            txtResultCount = itemView.findViewById(R.id.txtResultCount);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtProductName = itemView.findViewById(R.id.txtProductName); // Added initialization
        }
    }
}