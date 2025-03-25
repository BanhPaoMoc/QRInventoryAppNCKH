package pao.appnckh.qr_inventory_app.activitys;

import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import java.util.HashMap;
import java.util.Map;

import pao.appnckh.qr_inventory_app.R;

public class QRScanActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private BarcodeScanner barcodeScanner;
    private DatabaseReference userStoresReference;
    private String userId, storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        surfaceView = findViewById(R.id.surfaceView);
        barcodeScanner = BarcodeScanning.getClient();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storeId = "defaultStore"; // Có thể lấy từ Intent hoặc SharedPreferences
        userStoresReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Stores").child(storeId).child("Items");

        startScanner();
    }

    private void startScanner() {
        surfaceView.setOnClickListener(v -> scanBarcode());
    }

    private void scanBarcode() {
        ImageCapture imageCapture = new ImageCapture.Builder().build();

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                @SuppressWarnings("UnsafeOptInUsageError")
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    processImage(image);
                }
                imageProxy.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("QRScanner", "Lỗi chụp ảnh", exception);
            }
        });
    }

    private void processImage(InputImage image) {
        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String scannedUid = barcode.getRawValue();
                        checkItemInDatabase(scannedUid);
                    }
                })
                .addOnFailureListener(e -> Log.e("QRScanner", "Lỗi khi quét mã", e));
    }


    private void checkItemInDatabase(String itemUid) {
        userStoresReference.child(itemUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    showInventoryDialog(snapshot);
                } else {
                    showAddItemDialog(itemUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi truy vấn: " + error.getMessage());
            }
        });
    }

    private void showAddItemDialog(String itemUid) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Thêm sản phẩm mới")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    Map<String, Object> newItem = new HashMap<>();
                    newItem.put("uid", itemUid);
                    newItem.put("name", "Tên mặc định"); // Lấy từ EditText
                    newItem.put("price", 0);
                    newItem.put("category", "");
                    newItem.put("quantity", 0);
                    newItem.put("description", "");
                    newItem.put("dateAdded", System.currentTimeMillis());

                    userStoresReference.child(itemUid).setValue(newItem)
                            .addOnSuccessListener(unused -> Toast.makeText(QRScanActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi thêm sản phẩm", e));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showInventoryDialog(DataSnapshot snapshot) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_inventory, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Cập nhật kho hàng")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("lastUpdated", System.currentTimeMillis());
                    updateData.put("quantity", 10); // Thêm hoặc trừ số lượng

                    snapshot.getRef().updateChildren(updateData)
                            .addOnSuccessListener(unused -> Toast.makeText(QRScanActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi cập nhật kho hàng", e));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}