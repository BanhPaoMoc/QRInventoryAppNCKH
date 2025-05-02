package pao.appnckh.qr_inventory_app.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Product;
import pao.appnckh.qr_inventory_app.models.Store;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtProductCode;

    private Button btnAddProduct;
    private Spinner spinnerStores;
    String userId = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        edtProductCode = findViewById(R.id.edtProductCode);
        btnAddProduct = findViewById(R.id.btnAddProduct);


        spinnerStores = findViewById(R.id.spinnerStores);
        List<Store> storeList = new ArrayList<>();

        ArrayAdapter<Store> storeAdapterNew = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, storeList);
        storeAdapterNew.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStores.setAdapter(storeAdapterNew);
        String scannedProductCode = getIntent().getStringExtra("NEW_PRODUCT_CODE");

        spinnerStores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Store selectedStore = (Store) parent.getItemAtPosition(position);
                String storeId = selectedStore.getStoreId();

                String code = scannedProductCode;
                if (code.isEmpty()) return;

                String sanitizedCode = sanitizeProductId(code);

                DatabaseReference productRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(userId)
                        .child("Stores")
                        .child(storeId)
                        .child("Products");

                productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(AddProductActivity.this, "Sản phẩm đã tồn tại trong kho " + selectedStore.getStoreName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddProductActivity.this, "Sản phẩm chưa tồn tại trong kho " + selectedStore.getStoreName(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Không cần thiết xử lý nếu chỉ dùng để thông báo
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không xử lý gì ở đây
            }
        });


        DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Stores");

        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeList.clear();
                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    Store store = storeSnapshot.getValue(Store.class);
                    storeList.add(store);
                }
                storeAdapterNew.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this, "Không thể load kho hàng", Toast.LENGTH_SHORT).show();
            }
        });


        // Nhận mã sản phẩm đã quét từ intent

        if (scannedProductCode != null) {
            edtProductCode.setText(scannedProductCode);
            edtProductCode.setEnabled(false); // Không cho sửa nếu muốn cố định mã quét
        }

        // Xử lý nút thêm sản phẩm
        btnAddProduct.setOnClickListener(v -> {
            String code = scannedProductCode;


            Store selectedStore = (Store) spinnerStores.getSelectedItem();
            if (selectedStore == null) {
                Toast.makeText(this, "Vui lòng chọn kho hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String storeId = selectedStore.getStoreId();
            String sanitizedCode = sanitizeProductId(code);

            DatabaseReference productRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("Stores")
                    .child(storeId)
                    .child("Products");

            productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Product existingProduct = snapshot.getValue(Product.class);
                        showEditQuantityDialog(existingProduct, storeId);
                    } else {
                        showAddProductDialog(storeId, sanitizedCode);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddProductActivity.this, "Lỗi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private String sanitizeProductId(String productId) {
        // Thay thế các ký tự không hợp lệ
        return productId.replaceAll("[.#$\\[\\]/+]", "_");
    }


    private void showEditQuantityDialog(Product product, String storeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sản phẩm đã tồn tại");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(view);

        TextView txtName = view.findViewById(R.id.txtProductName);
        TextView txtCurrentCount = view.findViewById(R.id.txtCurrentCount);
        EditText edtQuantity = view.findViewById(R.id.edtQuantityInput);

        txtName.setText(product.getName());
        txtCurrentCount.setText("Tồn kho: " + product.getCount());

        builder.setPositiveButton("Nhập", (dialog, which) -> {
            handleUpdateQuantity(edtQuantity, product, storeId, true);
        });

        builder.setNegativeButton("Xuất", (dialog, which) -> {
            handleUpdateQuantity(edtQuantity, product, storeId, false);
        });

        builder.setNeutralButton("Đóng", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void handleUpdateQuantity(EditText edtQuantity, Product product, String storeId, boolean isImport) {
        String quantityStr = edtQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng phải là số nguyên dương", Toast.LENGTH_SHORT).show();
            return;
        }

        int newCount = isImport ? product.getCount() + quantity : product.getCount() - quantity;
        if (newCount < 0) {
            Toast.makeText(this, "Không đủ hàng để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        product.setCount(newCount);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Stores")
                .child(storeId)
                .child("Products")
                .child(product.getProductId())
                .setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, (isImport ? "Nhập" : "Xuất") + " thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddProductDialog(String storeId, String productCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(view);

        EditText edtProductCode = view.findViewById(R.id.edtDialogProductCode);
        EditText edtProductName = view.findViewById(R.id.edtDialogProductName);
        EditText edtProductPrice = view.findViewById(R.id.edtDialogProductPrice);
        EditText edtProductCount = view.findViewById(R.id.edtDialogProductCount);
        TextView textViewStoreName = view.findViewById(R.id.textViewStoreName);
        Button btnAddProduct = view.findViewById(R.id.btnAddProduct);
        Button btnCloseDialog = view.findViewById(R.id.btnCloseDialog);

        // Gán mã sản phẩm
        edtProductCode.setText(productCode);
        edtProductCode.setEnabled(false);

        // Tạo dialog và hiển thị sớm để có thể cập nhật UI khi dữ liệu tới
        builder.setTitle("Thêm sản phẩm mới");
        AlertDialog dialog = builder.create();
        dialog.show();

        // Lấy tên kho theo storeId
        DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Stores");

        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    Store store = storeSnapshot.getValue(Store.class);
                    if (store != null && store.getStoreId().equals(storeId)) {
                        textViewStoreName.setText("Kho đã chọn: " + store.getStoreName());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this, "Không thể load kho hàng", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddProduct.setOnClickListener(v -> {
            String name = edtProductName.getText().toString().trim();
            String priceStr = edtProductPrice.getText().toString().trim();
            String countStr = edtProductCount.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || countStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Product newProduct = new Product(storeId, productCode, name, price, count);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("Stores")
                    .child(storeId)
                    .child("Products")
                    .child(productCode)
                    .setValue(newProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show());
        });

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
    }



}