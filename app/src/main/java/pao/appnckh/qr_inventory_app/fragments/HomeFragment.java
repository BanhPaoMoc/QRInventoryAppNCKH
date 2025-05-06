package pao.appnckh.qr_inventory_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.adapters.AttendanceAdapter;
import pao.appnckh.qr_inventory_app.helpers.AttendanceHelper;
import pao.appnckh.qr_inventory_app.models.AttendanceRecord;

public class HomeFragment extends Fragment {

    private TextView tvCurrentDate;
    private TextView tvCheckInTime;
    private TextView tvCheckOutTime;
    private Button btnCheckIn;
    private Button btnCheckOut;
    private RecyclerView rvAttendanceHistory;
    private ProgressBar progressHistory;
    private TextView tvHistoryTitle;
    private TextView tvNoHistory;

    private AttendanceHelper attendanceHelper;
    private FirebaseUser currentUser;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceRecord> attendanceRecords;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase Auth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Khởi tạo AttendanceHelper
        attendanceHelper = new AttendanceHelper(requireContext());

        // Ánh xạ các view
        initViews(view);

        // Hiển thị ngày hiện tại
        tvCurrentDate.setText("Ngày: " + AttendanceHelper.getCurrentDate());

        // Khởi tạo danh sách và adapter
        attendanceRecords = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceRecords);
        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAttendanceHistory.setAdapter(attendanceAdapter);

        // Kiểm tra trạng thái chấm công hiện tại
        loadAttendanceStatus();

        // Tải lịch sử chấm công
        loadAttendanceHistory();

        // Thiết lập listener cho các nút
        setupButtonListeners();
    }

    private void initViews(View view) {
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvCheckInTime = view.findViewById(R.id.tvCheckInTime);
        tvCheckOutTime = view.findViewById(R.id.tvCheckOutTime);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);

        // Khởi tạo các view mới cho lịch sử chấm công
        rvAttendanceHistory = view.findViewById(R.id.rvAttendanceHistory);
        progressHistory = view.findViewById(R.id.progressHistory);
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);
    }

    private void loadAttendanceStatus() {
        // Hiển thị trạng thái loading nếu cần

        attendanceHelper.checkCurrentAttendanceStatus(new AttendanceHelper.AttendanceStatusCallback() {
            @Override
            public void onStatusLoaded(String status, String checkInTime, String checkOutTime) {
                // Cập nhật UI dựa trên trạng thái
                updateUIBasedOnStatus(status, checkInTime, checkOutTime);
            }
        });
    }

    private void loadAttendanceHistory() {
        // Hiển thị loading
        progressHistory.setVisibility(View.VISIBLE);
        rvAttendanceHistory.setVisibility(View.GONE);
        tvNoHistory.setVisibility(View.GONE);

        // Lấy lịch sử chấm công của tháng hiện tại
        attendanceHelper.getCurrentMonthAttendance(new AttendanceHelper.AttendanceHistoryCallback() {
            @Override
            public void onHistoryLoaded(List<AttendanceRecord> records) {
                // Ẩn loading
                progressHistory.setVisibility(View.GONE);

                // Cập nhật dữ liệu vào adapter
                if (records != null && !records.isEmpty()) {
                    attendanceRecords.clear();
                    attendanceRecords.addAll(records);
                    attendanceAdapter.notifyDataSetChanged();
                    rvAttendanceHistory.setVisibility(View.VISIBLE);
                    tvNoHistory.setVisibility(View.GONE);
                } else {
                    rvAttendanceHistory.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                // Ẩn loading, hiển thị lỗi
                progressHistory.setVisibility(View.GONE);
                rvAttendanceHistory.setVisibility(View.GONE);
                tvNoHistory.setVisibility(View.VISIBLE);
                tvNoHistory.setText("Lỗi: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIBasedOnStatus(String status, String checkInTime, String checkOutTime) {
        switch (status) {
            case "checked_in":
                // Đã check in nhưng chưa check out
                btnCheckIn.setEnabled(false);
                btnCheckOut.setEnabled(true);
                btnCheckIn.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                btnCheckOut.setBackgroundTintList(getResources().getColorStateList(R.color.orange));

                // Hiển thị thời gian check in
                if (checkInTime != null && !checkInTime.isEmpty()) {
                    tvCheckInTime.setText(checkInTime);
                }
                break;

            case "checked_out":
                // Đã check in và check out
                btnCheckIn.setEnabled(false);
                btnCheckOut.setEnabled(false);
                btnCheckIn.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                btnCheckOut.setBackgroundTintList(getResources().getColorStateList(R.color.gray));

                // Hiển thị thời gian check in và check out
                if (checkInTime != null && !checkInTime.isEmpty()) {
                    tvCheckInTime.setText(checkInTime);
                }
                if (checkOutTime != null && !checkOutTime.isEmpty()) {
                    tvCheckOutTime.setText(checkOutTime);
                }
                break;

            default:
                // Chưa check in
                btnCheckIn.setEnabled(true);
                btnCheckOut.setEnabled(false);
                btnCheckIn.setBackgroundTintList(getResources().getColorStateList(R.color.orange));
                btnCheckOut.setBackgroundTintList(getResources().getColorStateList(R.color.gray));

                // Reset thời gian hiển thị
                tvCheckInTime.setText("--:--");
                tvCheckOutTime.setText("--:--");
                break;
        }
    }

    private void setupButtonListeners() {
        btnCheckIn.setOnClickListener(v -> performCheckIn());
        btnCheckOut.setOnClickListener(v -> performCheckOut());
    }

    private void performCheckIn() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để chấm công", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện check in
        attendanceHelper.doCheckIn(new AttendanceHelper.AttendanceCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                // Tải lại trạng thái sau khi check in
                loadAttendanceStatus();
                // Cập nhật lịch sử chấm công
                loadAttendanceHistory();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performCheckOut() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để chấm công", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện check out
        attendanceHelper.doCheckOut(new AttendanceHelper.AttendanceCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                // Tải lại trạng thái sau khi check out
                loadAttendanceStatus();
                // Cập nhật lịch sử chấm công
                loadAttendanceHistory();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại trạng thái khi quay lại fragment
        loadAttendanceStatus();
        // Cập nhật lịch sử chấm công
        loadAttendanceHistory();
    }
}