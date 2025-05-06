package pao.appnckh.qr_inventory_app.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pao.appnckh.qr_inventory_app.models.AttendanceRecord;

public class AttendanceHelper {
    private static final String PREF_NAME = "AttendancePref";
    private static final String KEY_CURRENT_STATUS = "current_status";
    private static final String KEY_CURRENT_DATE = "current_date";
    private static final String KEY_RECORD_ID = "record_id";

    private final DatabaseReference mDatabase;
    private final FirebaseUser currentUser;
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public interface AttendanceCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface AttendanceStatusCallback {
        void onStatusLoaded(String status, String checkInTime, String checkOutTime);
    }

    public interface AttendanceHistoryCallback {
        void onHistoryLoaded(List<AttendanceRecord> records);
        void onError(String error);
    }

    public AttendanceHelper(Context context) {
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Kiểm tra và lấy trạng thái chấm công hiện tại
    public void checkCurrentAttendanceStatus(AttendanceStatusCallback callback) {
        String currentDate = getCurrentDate();
        String savedDate = sharedPreferences.getString(KEY_CURRENT_DATE, "");

        // Nếu ngày lưu trong preferences khác ngày hiện tại, reset trạng thái
        if (!currentDate.equals(savedDate)) {
            resetAttendanceStatus();
        }

        String status = sharedPreferences.getString(KEY_CURRENT_STATUS, "none");

        // Nếu đã có check in/out hôm nay, lấy dữ liệu từ Firebase
        if (!status.equals("none")) {
            String recordId = sharedPreferences.getString(KEY_RECORD_ID, "");
            if (!recordId.isEmpty()) {
                mDatabase.child("attendance").child(recordId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);
                        if (record != null) {
                            callback.onStatusLoaded(record.getStatus(),
                                    record.getCheckInTime(),
                                    record.getCheckOutTime());
                        } else {
                            callback.onStatusLoaded("none", "", "");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onStatusLoaded("none", "", "");
                    }
                });
            } else {
                callback.onStatusLoaded(status, "", "");
            }
        } else {
            callback.onStatusLoaded("none", "", "");
        }
    }

    // Thực hiện check in
    public void doCheckIn(AttendanceCallback callback) {
        if (currentUser == null) {
            callback.onError("Bạn chưa đăng nhập!");
            return;
        }

        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();
        long timestamp = System.currentTimeMillis();

        // Tạo record chấm công mới
        String recordId = mDatabase.child("attendance").push().getKey();
        AttendanceRecord record = new AttendanceRecord(currentUser.getUid(), currentDate, currentTime, timestamp);

        if (recordId != null) {
            mDatabase.child("attendance").child(recordId).setValue(record.toMap())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Lưu trạng thái vào SharedPreferences
                            saveAttendanceStatus("checked_in", currentDate, recordId);
                            callback.onSuccess("Check in thành công lúc " + currentTime);
                        } else {
                            callback.onError("Check in thất bại: " + task.getException().getMessage());
                        }
                    });
        } else {
            callback.onError("Không thể tạo bản ghi chấm công!");
        }
    }

    // Thực hiện check out
    public void doCheckOut(AttendanceCallback callback) {
        if (currentUser == null) {
            callback.onError("Bạn chưa đăng nhập!");
            return;
        }

        String recordId = sharedPreferences.getString(KEY_RECORD_ID, "");
        if (recordId.isEmpty()) {
            callback.onError("Không tìm thấy bản ghi check in!");
            return;
        }

        String currentTime = getCurrentTime();
        long timestamp = System.currentTimeMillis();

        // Cập nhật thông tin check out vào record đã tồn tại
        mDatabase.child("attendance").child(recordId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);
                if (record != null) {
                    record.updateCheckOut(currentTime, timestamp);

                    mDatabase.child("attendance").child(recordId).updateChildren(record.toMap())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Cập nhật trạng thái trong SharedPreferences
                                    saveAttendanceStatus("checked_out", getCurrentDate(), recordId);
                                    callback.onSuccess("Check out thành công lúc " + currentTime);
                                } else {
                                    callback.onError("Check out thất bại: " + task.getException().getMessage());
                                }
                            });
                } else {
                    callback.onError("Không tìm thấy bản ghi check in!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Lỗi khi truy cập database: " + error.getMessage());
            }
        });
    }

    // Lấy lịch sử chấm công trong tháng hiện tại
    public void getCurrentMonthAttendance(AttendanceHistoryCallback callback) {
        if (currentUser == null) {
            callback.onError("Bạn chưa đăng nhập!");
            return;
        }

        // Lấy thông tin tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // 0-11
        int currentYear = calendar.get(Calendar.YEAR);

        // Ngày đầu tiên của tháng
        calendar.set(currentYear, currentMonth, 1, 0, 0, 0);
        long startOfMonth = calendar.getTimeInMillis();

        // Ngày cuối cùng của tháng
        calendar.set(currentYear, currentMonth + 1, 0, 23, 59, 59);
        long endOfMonth = calendar.getTimeInMillis();

        // Query để lấy dữ liệu chấm công trong tháng hiện tại của người dùng hiện tại
        Query query = mDatabase.child("attendance")
                .orderByChild("userId").equalTo(currentUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<AttendanceRecord> records = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AttendanceRecord record = snapshot.getValue(AttendanceRecord.class);
                    if (record != null) {
                        // Thêm recordId vào đối tượng
                        record.setRecordId(snapshot.getKey());

                        // Kiểm tra xem bản ghi có thuộc tháng hiện tại không
                        if (record.getCheckInTimestamp() >= startOfMonth &&
                                record.getCheckInTimestamp() <= endOfMonth) {
                            records.add(record);
                        }
                    }
                }

                // Sắp xếp theo thời gian giảm dần (mới nhất lên đầu)
                Collections.sort(records, new Comparator<AttendanceRecord>() {
                    @Override
                    public int compare(AttendanceRecord o1, AttendanceRecord o2) {
                        return Long.compare(o2.getCheckInTimestamp(), o1.getCheckInTimestamp());
                    }
                });

                callback.onHistoryLoaded(records);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Lỗi khi tải lịch sử chấm công: " + databaseError.getMessage());
            }
        });
    }

    // Lưu trạng thái hiện tại vào SharedPreferences
    private void saveAttendanceStatus(String status, String date, String recordId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_STATUS, status);
        editor.putString(KEY_CURRENT_DATE, date);
        editor.putString(KEY_RECORD_ID, recordId);
        editor.apply();
    }

    // Reset trạng thái chấm công
    private void resetAttendanceStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_STATUS, "none");
        editor.putString(KEY_RECORD_ID, "");
        editor.apply();
    }

    // Lấy ngày hiện tại dưới dạng chuỗi
    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Lấy giờ hiện tại dưới dạng chuỗi
    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date());
    }
}