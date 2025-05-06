package pao.appnckh.qr_inventory_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.activitys.EditProfileActivity;
import pao.appnckh.qr_inventory_app.activitys.WelcomeActivity;

public class ProfileFragment extends Fragment {
    private ImageView imgAvatar;
    private TextView txtName, txtEmail, txtPhone;
    private LinearLayout layoutChinhSuaThongTin, layoutBaoLoi, layoutChinhSachBaoMat,
            layoutHuongDanSuDung, layoutDangXuat;

    private FirebaseAuth mAuth;

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

        layoutChinhSuaThongTin = view.findViewById(R.id.layoutChinhSuaThongTin);
        layoutBaoLoi = view.findViewById(R.id.layoutBaoLoi);
        layoutChinhSachBaoMat = view.findViewById(R.id.layoutChinhSachBaoMat);
        layoutHuongDanSuDung = view.findViewById(R.id.layoutHuongDanSuDung);
        layoutDangXuat = view.findViewById(R.id.layoutDangXuat);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Hiển thị thông tin người dùng nếu đã đăng nhập
        if (user != null) {
            txtName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
            txtPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa có số điện thoại");

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.default_avatar);
            }
        }

        // Xử lý sự kiện cho các mục menu
        layoutChinhSuaThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        layoutBaoLoi.setOnClickListener(v -> {
            // Chuyển đến trang báo lỗi
            Toast.makeText(getContext(), "Báo lỗi", Toast.LENGTH_SHORT).show();
            // TODO: Mở activity báo lỗi
        });

        layoutChinhSachBaoMat.setOnClickListener(v -> {
            // Chuyển đến trang chính sách bảo mật
            Toast.makeText(getContext(), "Chính sách & Bảo mật", Toast.LENGTH_SHORT).show();
            // TODO: Mở activity chính sách bảo mật
        });

        layoutHuongDanSuDung.setOnClickListener(v -> {
            // Chuyển đến trang hướng dẫn sử dụng
            Toast.makeText(getContext(), "Hướng dẫn sử dụng", Toast.LENGTH_SHORT).show();
            // TODO: Mở activity hướng dẫn sử dụng
        });

        layoutDangXuat.setOnClickListener(v -> {
            // Đăng xuất và chuyển về trang Welcome
            mAuth.signOut();
            startActivity(new Intent(getActivity(), WelcomeActivity.class));
            getActivity().finish();
        });

        return view;
    }
}