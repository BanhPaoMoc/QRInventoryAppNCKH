package pao.appnckh.qr_inventory_app.activitys;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.FirebaseDatabase;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.model.Product;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtProductCode;
    private EditText edtProductName;
    private EditText edtProductPrice, edtProductCount;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        edtProductCode = findViewById(R.id.edtProductCode);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtProductCount = findViewById(R.id.edtProductCount);

        // Nhận mã sản phẩm đã quét từ intent
        String scannedProductCode = getIntent().getStringExtra("NEW_PRODUCT_CODE");

        if (scannedProductCode != null) {
            edtProductCode.setText(scannedProductCode);
            edtProductCode.setEnabled(false); // Không cho sửa nếu muốn cố định mã quét
        }

        // Xử lý nút thêm sản phẩm
        btnAddProduct.setOnClickListener(v -> addProductToFirebase());


    }

    private String sanitizeProductId(String productId) {
        // Thay thế các ký tự không hợp lệ
        return productId.replaceAll("[.#$\\[\\]/+]", "_");
    }
    private void addProductToFirebase() {
        String code = edtProductCode.getText().toString().trim();
        String name = edtProductName.getText().toString().trim();
        String priceStr = edtProductPrice.getText().toString().trim();
        String countInt = edtProductCount.getText().toString().trim();

        // Kiểm tra xem các trường có rỗng không
        if (code.isEmpty() || name.isEmpty() || priceStr.isEmpty() || countInt.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi giá thành kiểu double
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi số lượng thành kiểu Integer
        Integer count;
        try {
            count = Integer.parseInt(countInt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tiến hành sanitize mã sản phẩm để đảm bảo đường dẫn Firebase hợp lệ
        String sanitizedCode = sanitizeProductId(code);

        // Tạo đối tượng sản phẩm
        Product product = new Product(sanitizedCode, name, price, count);


        FirebaseDatabase.getInstance().getReference("Products")
                .child(sanitizedCode) // key là mã sản phẩm đã được làm sạch
                .setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn trước
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }
}