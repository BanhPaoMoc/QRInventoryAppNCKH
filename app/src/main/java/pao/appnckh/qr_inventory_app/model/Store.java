package pao.appnckh.qr_inventory_app.model;

public class Store {
    private String storeId;
    private String storeName;
    private String userId;
    private int itemCount;

    public Store() {
        // Constructor mặc định bắt buộc cho Firebase
    }

    public Store(String storeId, String storeName, String userId, int itemCount) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
        this.itemCount = itemCount;
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
