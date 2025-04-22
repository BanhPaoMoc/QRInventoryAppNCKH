package pao.appnckh.qr_inventory_app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.activitys.WelcomeActivity;


public class ProfileFragment extends Fragment {
    private ImageView imgAvatar;
    private TextView txtName, txtEmail, txtPhone;
    private Button btnLogout;

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

        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        btnLogout = view.findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            txtName.setText(user.getDisplayName());
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa có số điện thoại");

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_launcher_background); // ảnh mặc định
            }
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), WelcomeActivity.class));
            getActivity().finish();

        });
        return view;
    }
}