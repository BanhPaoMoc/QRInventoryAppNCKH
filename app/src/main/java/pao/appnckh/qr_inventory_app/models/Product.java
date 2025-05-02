package pao.appnckh.qr_inventory_app.models;

public class Product {
    private String storeId;
    private String userId;
    private String productId;
    public String code;
    public String name;
    public double price;
    public int count;

    public Product() {
        // Required for Firebase
    }

    public Product(String storeId, String code, String name, double price, int count) {
        this.storeId = storeId;
        this.code = code;
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public Product(String storeId, String userId, String productId, String code, String name, double price, int count) {
        this.storeId = storeId;
        this.userId = userId;
        this.productId = productId;
        this.code = code;
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
