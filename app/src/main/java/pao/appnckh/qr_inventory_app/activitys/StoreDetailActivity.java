package pao.appnckh.qr_inventory_app.activitys;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.adapters.ProductAdapter;
import pao.appnckh.qr_inventory_app.models.Product;

public class StoreDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Product> productList;
    private ProductAdapter productAdapter;
    private DatabaseReference productRef;

    private String userId, storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_detail);

        TextView textStoreTitle = findViewById(R.id.textStoreTitle);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ Intent
        storeId = getIntent().getStringExtra("storeId");
        userId = getIntent().getStringExtra("userId");


        productList = new ArrayList<>();
        productAdapter = new ProductAdapter( this, productList, storeId, userId);
        recyclerView.setAdapter(productAdapter);



        if (storeId == null || userId == null) {
            Toast.makeText(this, "Thiếu dữ liệu kho!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        productRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Stores")
                .child(storeId)
                .child("Products");

        textStoreTitle.setText("Sản phẩm trong kho");

        loadProducts();
    }

    private void loadProducts() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(data.getKey()); // Gán ID nếu cần
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoreDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}