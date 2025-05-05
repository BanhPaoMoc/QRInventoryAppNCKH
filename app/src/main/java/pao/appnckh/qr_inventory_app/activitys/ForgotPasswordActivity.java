package pao.appnckh.qr_inventory_app.activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import pao.appnckh.qr_inventory_app.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private Button sendVerificationButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        sendVerificationButton = findViewById(R.id.sendVerificationButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for send verification button
        sendVerificationButton.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Vui lòng nhập email");
            return;
        }

        // Show progress bar and disable button
        progressBar.setVisibility(View.VISIBLE);
        sendVerificationButton.setEnabled(false);

        // Gửi email đặt lại mật khẩu
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    sendVerificationButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra email của bạn.",
                                Toast.LENGTH_LONG).show();

                        // Không chuyển sang màn hình OTP nữa, người dùng sẽ nhấn vào link trong email
                        finish(); // Đóng màn hình hiện tại, quay về màn hình đăng nhập
                    } else {
                        // Kiểm tra lỗi cụ thể
                        String errorMsg = "Lỗi: ";
                        if (task.getException() != null) {
                            if (task.getException().getMessage().contains("no user record")) {
                                errorMsg += "Email không tồn tại trong hệ thống";
                            } else {
                                errorMsg += task.getException().getMessage();
                            }
                        }

                        Toast.makeText(ForgotPasswordActivity.this,
                                errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}