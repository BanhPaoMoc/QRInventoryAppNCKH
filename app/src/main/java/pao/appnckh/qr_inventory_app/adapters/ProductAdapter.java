package pao.appnckh.qr_inventory_app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // Define your ViewHolder and other necessary methods here
    private List<Product> productList;
    public Context context;
    private String userId, storeId;

    public ProductAdapter(Context context, List<Product> productList,  String storeId, String userId) {
        this.context = context;
        this.productList = productList;
        this.userId = userId;
        this.storeId = storeId;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, textProductPrice, textProductBarCode, textProductId;
        ImageView btnDeleteProduct, btnEditProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textProductName);
            quantityText = itemView.findViewById(R.id.textProductQuantity);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductBarCode = itemView.findViewById(R.id.textProductBarCode);
            textProductId = itemView.findViewById(R.id.textProductId);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
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
        holder.btnEditProduct.setOnClickListener(v -> editProduct(position));
        holder.btnDeleteProduct.setOnClickListener(v -> deleteProduct(position, context));
    }

    private void deleteProduct(int position, Context context) {
        Product product = productList.get(position);

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá sản phẩm \"" + product.getName() + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    String pid = product.getProductId();
                    if (pid == null) {
                        Toast.makeText(context, "Lỗi: productId không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(userId)
                            .child("Stores")
                            .child(storeId)
                            .child("Products")
                            .child(pid);

                    ref.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                productList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, productList.size());
                                Toast.makeText(context, "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi xoá sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void editProduct(int position) {
        Product product = productList.get(position);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_product, null);

        EditText etProductCode = view.findViewById(R.id.etProductCode);
        EditText etProductName = view.findViewById(R.id.etProductName);
        EditText etProductPrice = view.findViewById(R.id.etProductPrice);
        EditText etProductQuantity = view.findViewById(R.id.etProductQuantity);
        Button btnSave = view.findViewById(R.id.btnEditSave);
        Button btnCancel = view.findViewById(R.id.btnEditCancel);

        // Set existing data
        etProductName.setText(product.getName());
        etProductPrice.setText(String.valueOf(product.getPrice()));
        etProductQuantity.setText(String.valueOf(product.getCount()));
        etProductCode.setText(product.getCode());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        btnSave.setOnClickListener(v -> {
            String name = etProductName.getText().toString();
            double price = Double.parseDouble(etProductPrice.getText().toString());
            int count = Integer.parseInt(etProductQuantity.getText().toString());
            String code = etProductCode.getText().toString();

            product.setName(name);
            product.setPrice(price);
            product.setCount(count);
            product.setCode(code);

            notifyItemChanged(position);

            DatabaseReference productRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("Stores")
                    .child(storeId)
                    .child("Products")
                    .child(product.getProductId());

            productRef.setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        notifyItemChanged(position);
                        Toast.makeText(context, "Đã cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}