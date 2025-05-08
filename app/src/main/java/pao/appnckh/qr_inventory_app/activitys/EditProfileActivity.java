package pao.appnckh.qr_inventory_app.activitys;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.Users;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText edtName;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        }

        edtName = findViewById(R.id.edtName);
        btnSave = findViewById(R.id.btnSave);

        // Load current user data
        if (user != null) {
            edtName.setText(user.getDisplayName());
        }

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String name = edtName.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập họ tên");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Update Firebase Auth profile
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update Realtime Database
                            Users userData = new Users();
                            userData.setFullName(name);
                            userData.setEmail(user.getEmail());
                            userData.setUid(user.getUid());

                            userRef.setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi khi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Cập nhật thông tin thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}