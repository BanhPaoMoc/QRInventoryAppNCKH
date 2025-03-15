package pao.appnckh.qr_inventory_app.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import pao.appnckh.qr_inventory_app.R;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // Mã yêu cầu Google Sign-In
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ImageView btnGoogleLogin, btnBack;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy ID token từ Firebase Console
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Ánh xạ view từ XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnBack = findViewById(R.id.btnBack);

        // Xử lý nút đăng nhập
        btnLogin.setOnClickListener(view -> handleLogin());

        // Xử lý nút đăng ký
        tvRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xử lý nút quên mật khẩu
        tvForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Xử lý nút back
        btnBack.setOnClickListener(view -> finish());

        // Xử lý Google Sign-In
        btnGoogleLogin.setOnClickListener(view -> signInWithGoogle());
    }

    // Xử lý đăng nhập bằng email và mật khẩu
    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được để trống");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được để trống");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Bắt đầu đăng nhập bằng Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Nhận kết quả sau khi đăng nhập Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign-In", "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Xác thực tài khoản Google với Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Xác thực Google thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
