package pao.appnckh.qr_inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashscreenActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000; // 3 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen); // Đặt layout

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Chuyển sang WelcomeActivity
            startActivity(new Intent(SplashscreenActivity.this, WelcomeActivity.class));
            finish(); // Đóng SplashScreen
        }, SPLASH_DELAY);
    }

}