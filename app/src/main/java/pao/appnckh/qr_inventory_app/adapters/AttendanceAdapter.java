package pao.appnckh.qr_inventory_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pao.appnckh.qr_inventory_app.R;
import pao.appnckh.qr_inventory_app.models.AttendanceRecord;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceRecord> attendanceRecords;

    public AttendanceAdapter(List<AttendanceRecord> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_record, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return attendanceRecords.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvCheckInTime;
        TextView tvCheckOutTime;
        TextView tvStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvRecordDate);
            tvCheckInTime = itemView.findViewById(R.id.tvCheckInTime);
            tvCheckOutTime = itemView.findViewById(R.id.tvCheckOutTime);
            tvStatus = itemView.findViewById(R.id.tvRecordStatus);
        }

        void bind(AttendanceRecord record) {
            tvDate.setText(record.getDate());
            tvCheckInTime.setText("Check in: " + record.getCheckInTime());

            if (record.getCheckOutTime() != null && !record.getCheckOutTime().isEmpty()) {
                tvCheckOutTime.setText("Check out: " + record.getCheckOutTime());
                tvCheckOutTime.setVisibility(View.VISIBLE);
            } else {
                tvCheckOutTime.setVisibility(View.GONE);
            }

            // Set status text and color
            String status = record.getStatus();
            if ("checked_in".equals(status)) {
                tvStatus.setText("Đang làm việc");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.orange));
            } else if ("checked_out".equals(status)) {
                tvStatus.setText("Đã checkout");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
            } else {
                tvStatus.setText("Không xác định");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.gray));
            }
        }
    }
}