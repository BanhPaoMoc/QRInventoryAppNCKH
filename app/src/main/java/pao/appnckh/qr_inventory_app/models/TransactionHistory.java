package pao.appnckh.qr_inventory_app.models;

public class TransactionHistory {
    private String transactionId;
    private String productId;
    private String productName;
    private String storeId;
    private String storeName;
    private long timestamp;
    private int quantity;
    private boolean isImport; // true = nhập, false = xuất
    private int resultCount; // số lượng tồn kho sau khi nhập/xuất

    // Empty constructor for Firebase
    public TransactionHistory() {
    }

    public TransactionHistory(String transactionId, String productId, String productName, String storeId,
                              String storeName, long timestamp, int quantity, boolean isImport, int resultCount) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.timestamp = timestamp;
        this.quantity = quantity;
        this.isImport = isImport;
        this.resultCount = resultCount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isImport() {
        return isImport;
    }

    public void setImport(boolean anImport) {
        isImport = anImport;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}