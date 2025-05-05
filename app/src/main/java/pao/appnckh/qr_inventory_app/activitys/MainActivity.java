package pao.appnckh.qr_inventory_app.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.fragments.HistoryFragment;
import pao.appnckh.qr_inventory_app.fragments.HomeFragment;
import pao.appnckh.qr_inventory_app.fragments.ProfileFragment;
import pao.appnckh.qr_inventory_app.fragments.StoreFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton btnQr;

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnQr = findViewById(R.id.btn_qr);

        // Set Home Fragment as default
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();

        // Sự kiện click cho Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_stock) {
                    selectedFragment = new StoreFragment();
                } else if (itemId == R.id.nav_log) {
                    selectedFragment = new HistoryFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
                }
                return true;
            }
        });

        // Set up the Floating Action Button click listener để mở QRScannerActivity
        btnQr.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRScanActivity.class);
            startActivity(intent);
        });
    }
}