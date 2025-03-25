package pao.appnckh.qr_inventory_app.models;

public class Users {
    private String uid;        // ID duy nhất của người dùng
    private String fullName;   // Họ và tên
    private String username;   // Tên người dùng
    private String email;      // Email

    // Constructor mặc định (bắt buộc cho Firebase)
    public Users() {
    }


    public Users(String uid, String fullName, String username, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
    }

    // Getter và Setter
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}