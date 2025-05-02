package pao.appnckh.qr_inventory_app.models;

public class Store {
    private String storeId;
    private String storeName;
    private String userId;
    private int totalCount;

    // Constructor mặc định (bắt buộc cho Firebase)
    public Store() {
    }

    // Constructor với tham số
    public Store(String storeId, String storeName, String userId) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
        
    }

    // Getter và Setter
    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public String toString() {
        return storeName; // để hiển thị trong Spinner
    }
}