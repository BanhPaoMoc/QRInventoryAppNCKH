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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Product;
import pao.appnckh.qr_inventory_app.models.Store;
import pao.appnckh.qr_inventory_app.models.TransactionHistory;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtProductCode;

    private Button btnChooseStore, btnCancel;
    private Spinner spinnerStores;
    String userId = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        edtProductCode = findViewById(R.id.edtProductCode);
        btnChooseStore = findViewById(R.id.btnChooseStore);
        btnCancel = findViewById(R.id.btnCancelAddProduct);
        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

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
                        .child("Products")
                        .child(sanitizedCode);

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
        btnChooseStore.setOnClickListener(v -> {
            String code = scannedProductCode;
            if (code == null || code.isEmpty()) {
                Toast.makeText(AddProductActivity.this,
                        "Chưa có mã sản phẩm để xử lý",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Store selectedStore = (Store) spinnerStores.getSelectedItem();
            if (selectedStore == null) {
                Toast.makeText(AddProductActivity.this,
                        "Vui lòng chọn kho hàng",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String storeId = selectedStore.getStoreId();
            String sanitizedCode = sanitizeProductId(code);

            // Trỏ trực tiếp đến sản phẩm đã quét trong kho
            DatabaseReference productRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("Stores")
                    .child(storeId)
                    .child("Products")
                    .child(sanitizedCode);

            productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Lấy thông tin sản phẩm hiện có
                        Product existingProduct = snapshot.getValue(Product.class);
                        // Mở dialog Nhập/Xuất với thông tin product
                        showEditQuantityDialog(existingProduct, storeId, sanitizedCode);

                    } else {
                        // Mở dialog thêm sản phẩm mới
                        showAddProductDialog(storeId, sanitizedCode);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddProductActivity.this,
                            "Lỗi truy vấn dữ liệu: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    private String sanitizeProductId(String productId) {
        // Thay thế các ký tự không hợp lệ
        return productId.replaceAll("[.#$\\[\\]/+]", "_");
    }


    private void showEditQuantityDialog(
            Product product,
            String storeId,
            String sanitizedCode
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sản phẩm đã tồn tại");

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(view);

        TextView txtName         = view.findViewById(R.id.txtProductName);
        TextView txtProductPrice = view.findViewById(R.id.txtProductPrice);
        TextView txtCurrentCount = view.findViewById(R.id.txtCurrentCount);
        EditText edtQuantity     = view.findViewById(R.id.edtQuantityInput);
        Button btnNhap           = view.findViewById(R.id.btnNhap);
        Button btnXuat           = view.findViewById(R.id.btnXuat);

        // set ban đầu
        txtName.setText("Tên sản phẩm: " + product.getName());
        txtProductPrice.setText("Giá sản phẩm: " + product.getPrice());
        txtCurrentCount.setText("Tồn kho: " + product.getCount());

        AlertDialog dialog = builder.create();
        dialog.show();

        btnNhap.setOnClickListener(v -> {
            handleUpdateQuantity(
                    edtQuantity,
                    product,
                    storeId,
                    true,
                    sanitizedCode,
                    txtCurrentCount  // truyền vào đây
            );
        });
        btnXuat.setOnClickListener(v -> {
            handleUpdateQuantity(
                    edtQuantity,
                    product,
                    storeId,
                    false,
                    sanitizedCode,
                    txtCurrentCount  // truyền vào đây
            );
        });
    }

    private void handleUpdateQuantity(
            EditText edtQuantity,
            Product product,
            String storeId,
            boolean isImport,
            String sanitizedCode,
            TextView txtCurrentCount // thêm tham số này
    ) {
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
        // Create transaction history record
        String transactionId = FirebaseDatabase.getInstance().getReference().push().getKey();
        TransactionHistory transaction = new TransactionHistory(
                transactionId,
                product.getProductId(),
                product.getName(),
                storeId,
                ((Store) spinnerStores.getSelectedItem()).getStoreName(),
                System.currentTimeMillis(),
                quantity,
                isImport,
                newCount
        );

        // Update both product count and save transaction history
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        // Update product count
        updates.put("Stores/" + storeId + "/Products/" + sanitizedCode + "/count", newCount);
        // Save transaction history
        updates.put("TransactionHistory/" + transactionId, transaction);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            (isImport ? "Nhập" : "Xuất") + " thành công!",
                            Toast.LENGTH_SHORT).show();
                    txtCurrentCount.setText("Tồn kho: " + newCount);
                    edtQuantity.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Lỗi khi cập nhật: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
        // cập nhật model
        product.setCount(newCount);

        // ghi vào Firebase
        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Stores")
                .child(storeId)
                .child("Products")
                .child(sanitizedCode)
                .setValue(product)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this,
                            (isImport ? "Nhập" : "Xuất") + " thành công!",
                            Toast.LENGTH_SHORT).show();

                    txtCurrentCount.setText("Tồn kho: " + product.getCount());

                    edtQuantity.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Lỗi khi cập nhật sản phẩm: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddProductDialog(String storeId, String productCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(view);

        EditText edtProductCode    = view.findViewById(R.id.edtDialogProductCode);
        EditText edtProductName    = view.findViewById(R.id.edtDialogProductName);
        EditText edtProductPrice   = view.findViewById(R.id.edtDialogProductPrice);
        EditText edtProductCount   = view.findViewById(R.id.edtDialogProductCount);
        TextView textViewStoreName = view.findViewById(R.id.textViewStoreName);
        Button btnAddProduct       = view.findViewById(R.id.btnAddProduct);
        Button btnCloseDialog      = view.findViewById(R.id.btnCloseDialog);

        // Gán mã sản phẩm và khóa lại
        edtProductCode.setText(productCode);
        edtProductCode.setEnabled(false);

        builder.setTitle("Thêm sản phẩm mới");
        AlertDialog dialog = builder.create();
        dialog.show();

        // Lấy thông tin Store
        DatabaseReference storeRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Stores")
                .child(storeId);

        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AddProductActivity.this,
                            "Kho không tồn tại hoặc đã bị xóa",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                Store store = snapshot.getValue(Store.class);
                if (store != null) {
                    textViewStoreName.setText("Kho đã chọn: " + store.getStoreName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this,
                        "Không thể load thông tin kho: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnAddProduct.setOnClickListener(v -> {
            String name     = edtProductName.getText().toString().trim();
            String priceStr = edtProductPrice.getText().toString().trim();
            String countStr = edtProductCount.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || countStr.isEmpty()) {
                Toast.makeText(AddProductActivity.this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AddProductActivity.this,
                        "Giá sản phẩm không hợp lệ",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AddProductActivity.this,
                        "Số lượng sản phẩm không hợp lệ",
                        Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddProductActivity.this,
                                "Thêm sản phẩm thành công!",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddProductActivity.this,
                                "Lỗi khi thêm sản phẩm: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
    }





}