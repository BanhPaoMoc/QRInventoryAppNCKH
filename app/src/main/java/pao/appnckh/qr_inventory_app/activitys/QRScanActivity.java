package pao.appnckh.qr_inventory_app.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pao.appnckh.qr_inventory_app.R;

@androidx.camera.core.ExperimentalGetImage
public class QRScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 1001;

    private PreviewView previewView;
    private TextView scanResultText;
    private TextView barcodeTypeText;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;
    private boolean autoFinishOnScan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        previewView = findViewById(R.id.previewView);
        scanResultText = findViewById(R.id.scanResultText);
        barcodeTypeText = findViewById(R.id.barcodeTypeText);

        // Kiểm tra xem Activity được mở với cờ không tự động đóng
        if (getIntent().hasExtra("AUTO_FINISH")) {
            autoFinishOnScan = getIntent().getBooleanExtra("AUTO_FINISH", true);
        }

        // Yêu cầu quyền truy cập camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            setupCamera();
        }

        // Khởi tạo barcode scanner với nhiều định dạng
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_AZTEC
                )
                .build();
        scanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());

        // Camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Unbind trước khi gắn lại
        cameraProvider.unbindAll();

        // Bind camera provider với các use case
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                Toast.makeText(this, "Cần quyền truy cập camera để quét mã",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            // Lấy media image
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            // Process image
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (barcodes.size() > 0) {
                            Barcode barcode = barcodes.get(0);
                            String barcodeValue = barcode.getRawValue();
                            if (barcodeValue != null) {
                                // Xác định loại mã vạch
                                String barcodeType = getBarcodeTypeName(barcode.getFormat());

                                // Xử lý kết quả quét được
                                handleScanResult(barcodeValue, barcodeType);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Barcode scanning failed: " + e.getMessage()))
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private String getBarcodeTypeName(int format) {
        switch (format) {
            case Barcode.FORMAT_QR_CODE: return "QR Code";
            case Barcode.FORMAT_CODE_128: return "Code 128";
            case Barcode.FORMAT_CODE_39: return "Code 39";
            case Barcode.FORMAT_EAN_13: return "EAN-13";
            case Barcode.FORMAT_EAN_8: return "EAN-8";
            case Barcode.FORMAT_UPC_A: return "UPC-A";
            case Barcode.FORMAT_UPC_E: return "UPC-E";
            case Barcode.FORMAT_CODABAR: return "Codabar";
            case Barcode.FORMAT_ITF: return "ITF";
            case Barcode.FORMAT_PDF417: return "PDF417";
            case Barcode.FORMAT_AZTEC: return "Aztec";
            default: return "Unknown";
        }
    }

    private boolean isProcessingScan = false; // Thêm một flag để theo dõi việc quét

    private void handleScanResult(String value, String type) {
        // Kiểm tra nếu đang trong quá trình xử lý scan
        if (isProcessingScan) {
            return; // Nếu đang xử lý, không làm gì cả
        }

        // Đánh dấu bắt đầu xử lý scan
        isProcessingScan = true;

        runOnUiThread(() -> {
            // Hiển thị giá trị mã quét và loại barcode
            scanResultText.setText(value);
            barcodeTypeText.setText(type);
            scanResultText.setVisibility(View.VISIBLE);
            barcodeTypeText.setVisibility(View.VISIBLE);

            // Trả kết quả về Activity gọi
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SCAN_RESULT", value);
            resultIntent.putExtra("BARCODE_TYPE", type);
            setResult(RESULT_OK, resultIntent);

            // Kiểm tra sản phẩm trong database
            checkProductInDatabase(value, autoFinishOnScan);
        });
    }

    private void checkProductInDatabase(String barcodeValue, boolean autoFinish) {
        checkIfProductExists(barcodeValue, new ProductExistCallback() {
            @Override
            public void onCheck(boolean exists) {

                if (!isProcessingScan) {
                    return;
                }
                Intent intent;
                if (exists) {
                    // Nếu sản phẩm tồn tại, chuyển đến màn hình chi tiết sản phẩm
                    intent = new Intent(QRScanActivity.this, ProductDetailActivity.class);
                    intent.putExtra("PRODUCT_CODE", barcodeValue);
                } else {
                    // Nếu chưa có, chuyển đến màn hình thêm sản phẩm
                    intent = new Intent(QRScanActivity.this, AddProductActivity.class);
                    intent.putExtra("NEW_PRODUCT_CODE", barcodeValue);
                }
                startActivity(intent);

                // Đóng sau một chút nếu cấu hình auto finish, để người dùng kịp xem
                if (autoFinish) {
                    scanResultText.postDelayed(() -> finish(), 2500);
                }

            }
        });
    }

    private String sanitizeProductId(String productId) {
        // Thay thế các ký tự không hợp lệ
        return productId.replaceAll("[.#$\\[\\]]", "_");
    }

    private void checkIfProductExists(String productId, ProductExistCallback callback) {
        String sanitizedProductId = sanitizeProductId(productId);
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Products")
                .child(sanitizedProductId);

        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean exists = task.getResult().exists();
                callback.onCheck(exists);
            } else {
                callback.onCheck(false); // Lỗi thì xử lý như không tồn tại
            }
        });
    }

    public interface ProductExistCallback {
        void onCheck(boolean exists);
    }

}