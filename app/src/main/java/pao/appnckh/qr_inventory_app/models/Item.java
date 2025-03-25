package pao.appnckh.qr_inventory_app.models;

public class Item {
    private String itemCode;
    private String itemName;
    private String itemPrice;
    private String itemCategory;
    private String itemQuantity;
    private String itemDate;
    private String itemDescription;

    public Item() {
        // Constructor mặc định bắt buộc cho Firebase
    }

    public Item(String itemCode, String itemName, String itemPrice, String itemCategory, String itemQuantity, String itemDate, String itemDescription) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemCategory = itemCategory;
        this.itemQuantity = itemQuantity;
        this.itemDate = itemDate;
        this.itemDescription = itemDescription;
    }

    // Getter và setter
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemPrice() { return itemPrice; }
    public void setItemPrice(String itemPrice) { this.itemPrice = itemPrice; }

    public String getItemCategory() { return itemCategory; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }

    public String getItemQuantity() { return itemQuantity; }
    public void setItemQuantity(String itemQuantity) { this.itemQuantity = itemQuantity; }

    public String getItemDate() { return itemDate; }
    public void setItemDate(String itemDate) { this.itemDate = itemDate; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
}