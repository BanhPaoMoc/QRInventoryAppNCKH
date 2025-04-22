package pao.appnckh.qr_inventory_app.model;

public class Product {
    public String code;
    public String name;
    public double price;
    public int count;

    public Product() {
        // Required for Firebase
    }

    public Product(String code, String name, double price, int count) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.count = count;
    }
}
