package pao.appnckh.qr_inventory_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.adapters.StoreAdapter;
import pao.appnckh.qr_inventory_app.models.Store;

public class StoreFragment extends Fragment {

    private Context context;
    private SearchView searchView;
    private RecyclerView rvStores;
    private StoreAdapter storeAdapter;
    private List<Store> storeList; // Danh sách gốc
    private List<Store> filteredStoreList; // Danh sách đã lọc
    private DatabaseReference userStoresReference;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton fabAddStore;


    public StoreFragment() {
        // Required empty public constructor
    }


    public interface OnStoreActionListener {
        void onStoreClicked(Store store);
        void onEditStore(Store store, int position);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        // Ánh xạ view
        searchView = view.findViewById(R.id.searchView);
        rvStores = view.findViewById(R.id.rvStores);
        fabAddStore = view.findViewById(R.id.fabAddStore);
        context = getContext();

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        userStoresReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Stores");

        // Khởi tạo danh sách kho và adapter
        storeList = new ArrayList<>();
        filteredStoreList = new ArrayList<>();
        setupRecyclerView();

        // Load danh sách kho từ Firebase
        loadStores();

        // Xử lý sự kiện thêm kho
        fabAddStore.setOnClickListener(v -> showCreateStoreDialog());

        // Xử lý sự kiện tìm kiếm
        setupSearchView();

        DatabaseReference storesRef = FirebaseDatabase.getInstance().getReference("Stores");
        storesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            private DataSnapshot snapshot;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                this.snapshot = snapshot;

                // Khởi tạo list kho hàng nếu chưa có
                storeList.clear(); // Đảm bảo rằng list trước khi thêm mới phải clear

                // Duyệt qua từng store trong Firebase
                for (DataSnapshot storeSnap : snapshot.getChildren()) {
                    String storeId = storeSnap.getKey();
                    String storeName = storeSnap.child("storeName").getValue(String.class);

                    // Khởi tạo đối tượng StoreModel
                    Store store = new Store();
                    store.setStoreId(storeId);
                    store.setStoreName(storeName);

                    int totalCount = 0;
                    for (DataSnapshot productSnap : storeSnap.getChildren()) {
                        if (productSnap.hasChild("count")) {
                            Integer count = productSnap.child("count").getValue(Integer.class);
                            if (count != null) {
                                totalCount += count;
                            }
                        }
                    }
                    store.setTotalCount(totalCount);  // Cập nhật tổng số lượng

                    storeList.add(store);  // Thêm vào storeList
                }

                // Sau khi lấy xong dữ liệu từ Firebase, cập nhật RecyclerView
                updateRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hiển thị thông báo lỗi nếu Firebase bị lỗi
                Toast.makeText(getContext(), "Lỗi khi đọc kho hàng", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void updateRecyclerView() {
        storeAdapter = new StoreAdapter(getContext(), storeList, new StoreAdapter.OnStoreActionListener() {
            @Override
            public void onStoreClicked(Store store) {
                // Xử lý khi người dùng click vào một store
                Toast.makeText(getContext(), "Store clicked: " + store.getStoreName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditStore(Store store, int position) {
                // Xử lý khi người dùng chọn sửa thông tin store
                Toast.makeText(getContext(), "Edit Store: " + store.getStoreName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteStore(Store store, int position) {
                // Xử lý khi người dùng chọn xóa store
                Toast.makeText(getContext(), "Delete Store: " + store.getStoreName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvStores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStores.setAdapter(storeAdapter);
    }




    // Thiết lập RecyclerView
    private void setupRecyclerView() {
        storeAdapter = new StoreAdapter(getContext(), filteredStoreList, new StoreAdapter.OnStoreActionListener() {
            @Override
            public void onStoreClicked(Store store) {

                Toast.makeText(getContext(), "Store clicked: " + store.getStoreName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteStore(Store store, int position) {
                // Xử lý khi người dùng muốn xóa store
                deleteStore(store);
            }

            @Override
            public void onEditStore(Store store, int position) {
                // Xử lý khi người dùng muốn sửa thông tin store
                showEditStoreDialog(store);
            }
        });

        rvStores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStores.setAdapter(storeAdapter);
    }

    // Thiết lập SearchView
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Xử lý khi người dùng nhấn nút tìm kiếm
                filterStores(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Xử lý khi người dùng thay đổi nội dung tìm kiếm
                filterStores(newText);
                return true;
            }
        });
    }

    // Lọc danh sách kho
    private void filterStores(String query) {
        filteredStoreList.clear();
        if (query.isEmpty()) {
            // Nếu không có từ khóa tìm kiếm, hiển thị toàn bộ danh sách
            filteredStoreList.addAll(storeList);
        } else {
            // Lọc danh sách dựa trên từ khóa
            for (Store store : storeList) {
                if (store.getStoreName().toLowerCase().contains(query.toLowerCase())) {
                    filteredStoreList.add(store);
                }
            }
        }
        storeAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    // Load danh sách kho từ Firebase
    private void loadStores() {
        userStoresReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Store store = dataSnapshot.getValue(Store.class);
                    if (store != null) {
                        storeList.add(store);
                    }
                }
                // Cập nhật danh sách đã lọc
                filteredStoreList.clear();
                filteredStoreList.addAll(storeList);
                storeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị hộp thoại tạo kho
    private void showCreateStoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_store, null);
        builder.setView(dialogView);

        EditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý sự kiện hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý sự kiện tạo kho
        btnCreate.setOnClickListener(v -> {
            String storeName = etStoreName.getText().toString().trim();
            if (!storeName.isEmpty()) {
                createStore(storeName);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên kho", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo kho mới
    private void createStore(String storeName) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String storeId = userStoresReference.push().getKey(); // Tạo ID duy nhất cho store

        Store store = new Store(storeId, storeName, userId);
        userStoresReference.child(storeId).setValue(store)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Tạo kho thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Xóa kho
    private void deleteStore(Store store) {
        // Lấy danh sách các sản phẩm trong kho
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Stores")
                .child(store.getStoreId());

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu kho còn sản phẩm, xóa tất cả các sản phẩm
                    for (DataSnapshot productSnap : dataSnapshot.getChildren()) {
                        // Xóa từng sản phẩm
                        productSnap.getRef().removeValue();
                    }
                    Toast.makeText(getContext(), "Đã xóa hết sản phẩm trong kho", Toast.LENGTH_SHORT).show();
                }

                // Sau khi xóa hết sản phẩm, xóa kho
                userStoresReference.child(store.getStoreId()).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Xóa kho thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi đọc sản phẩm trong kho", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Hiển thị hộp thoại đổi tên kho
    private void showEditStoreDialog(Store store) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_store, null);
        builder.setView(dialogView);

        EditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        etStoreName.setText(store.getStoreName());
        btnCreate.setText("Lưu");

        AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý sự kiện hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý sự kiện lưu
        btnCreate.setOnClickListener(v -> {
            String newStoreName = etStoreName.getText().toString().trim();
            if (!newStoreName.isEmpty()) {
                updateStore(store, newStoreName);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên kho", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật tên kho
    private void updateStore(Store store, String newStoreName) {
        store.setStoreName(newStoreName);
        userStoresReference.child(store.getStoreId()).setValue(store)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}