package pao.appnckh.qr_inventory_app.models;

import java.util.HashMap;
import java.util.Map;

public class AttendanceRecord {
    private String userId;
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private long checkInTimestamp;
    private long checkOutTimestamp;
    private String status;  // "checked_in", "checked_out", "absent"
    private String recordId; // Added recordId field

    // Constructor cần thiết cho Firebase
    public AttendanceRecord() {
    }

    public AttendanceRecord(String userId, String date, String checkInTime, long checkInTimestamp) {
        this.userId = userId;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkInTimestamp = checkInTimestamp;
        this.status = "checked_in";
    }

    // Chuyển đổi object thành Map để lưu vào Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("date", date);
        result.put("checkInTime", checkInTime);
        result.put("checkInTimestamp", checkInTimestamp);
        result.put("status", status);

        if (checkOutTime != null) {
            result.put("checkOutTime", checkOutTime);
            result.put("checkOutTimestamp", checkOutTimestamp);
        }

        return result;
    }

    // Cập nhật thông tin checkout
    public void updateCheckOut(String checkOutTime, long checkOutTimestamp) {
        this.checkOutTime = checkOutTime;
        this.checkOutTimestamp = checkOutTimestamp;
        this.status = "checked_out";
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public long getCheckInTimestamp() {
        return checkInTimestamp;
    }

    public void setCheckInTimestamp(long checkInTimestamp) {
        this.checkInTimestamp = checkInTimestamp;
    }

    public long getCheckOutTimestamp() {
        return checkOutTimestamp;
    }

    public void setCheckOutTimestamp(long checkOutTimestamp) {
        this.checkOutTimestamp = checkOutTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Added getter and setter for recordId
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
}