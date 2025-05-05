package pao.appnckh.qr_inventory_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // Define your ViewHolder and other necessary methods here
    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, textProductPrice, textProductBarCode, textProductId;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textProductName);
            quantityText = itemView.findViewById(R.id.textProductQuantity);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductBarCode = itemView.findViewById(R.id.textProductBarCode);
            textProductId = itemView.findViewById(R.id.textProductId);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameText.setText(product.getName());
        holder.textProductPrice.setText("Giá: " + product.getPrice() + " VNĐ");
        holder.quantityText.setText("Số lượng: " + product.getCount());
        holder.textProductBarCode.setText("Mã vạch: " + product.getCode());
        holder.textProductId.setText("Mã sản phẩm: " + product.getProductId());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}