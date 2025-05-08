package pao.appnckh.qr_inventory_app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.activitys.EditProfileActivity;
import pao.appnckh.qr_inventory_app.activitys.GuideActivity;
import pao.appnckh.qr_inventory_app.activitys.WelcomeActivity;
import pao.appnckh.qr_inventory_app.models.Users;

public class ProfileFragment extends Fragment {
    private ImageView imgAvatar;
    private TextView txtName, txtEmail, txtPhone, txtTotalStores, txtTotalProducts;
    private LinearLayout layoutChinhSuaThongTin, layoutChinhSachBaoMat,
            layoutHuongDanSuDung, layoutDangXuat, layoutLienHe, layoutPhanHoi;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ các thành phần trong giao diện
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtTotalStores = view.findViewById(R.id.txtTotalStores);
        txtTotalProducts = view.findViewById(R.id.txtTotalProducts);

        layoutChinhSuaThongTin = view.findViewById(R.id.layoutChinhSuaThongTin);
        layoutChinhSachBaoMat = view.findViewById(R.id.layoutChinhSachBaoMat);
        layoutHuongDanSuDung = view.findViewById(R.id.layoutHuongDanSuDung);
        layoutDangXuat = view.findViewById(R.id.layoutDangXuat);
        layoutLienHe = view.findViewById(R.id.layoutLienHe);
        layoutPhanHoi = view.findViewById(R.id.layoutPhanHoi);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            loadUserData();
            loadStatistics();
        }

        // Xử lý sự kiện cho các mục menu
        setupClickListeners();

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // First set basic info from FirebaseUser
            txtName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Chưa có tên");
            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
            txtPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa có số điện thoại");

            // Load avatar
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.default_avatar);
            }

            // Then try to get additional info from Realtime Database
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Users userData = snapshot.getValue(Users.class);
                    if (userData != null) {
                        // Only update if the database has more complete information
                        if (userData.getFullName() != null && !userData.getFullName().isEmpty()) {
                            txtName.setText(userData.getFullName());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(getContext(), "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadStatistics() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference storesRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Stores");

        storesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalStores = (int) snapshot.getChildrenCount();
                int totalProducts = 0;

                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    DataSnapshot productsSnapshot = storeSnapshot.child("Products");
                    totalProducts += (int) productsSnapshot.getChildrenCount();
                }

                txtTotalStores.setText(String.valueOf(totalStores));
                txtTotalProducts.setText(String.valueOf(totalProducts));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thống kê", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        layoutChinhSuaThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        layoutChinhSachBaoMat.setOnClickListener(v -> {
            String url = "https://your-privacy-policy-url.com"; // Thay thế bằng URL thực tế
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        layoutHuongDanSuDung.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GuideActivity.class);
            startActivity(intent);
        });

        layoutLienHe.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@yourapp.com")); // Thay thế bằng email thực tế
            intent.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ hỗ trợ");
            startActivity(intent);
        });

        layoutPhanHoi.setOnClickListener(v -> showFeedbackDialog());

        layoutDangXuat.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Gửi phản hồi");
        builder.setMessage("Bạn có muốn gửi phản hồi về ứng dụng không?");
        builder.setPositiveButton("Gửi phản hồi", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:feedback@yourapp.com")); // Thay thế bằng email thực tế
            intent.putExtra(Intent.EXTRA_SUBJECT, "Phản hồi người dùng");
            startActivity(intent);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất?");
        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), WelcomeActivity.class));
            getActivity().finish();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}