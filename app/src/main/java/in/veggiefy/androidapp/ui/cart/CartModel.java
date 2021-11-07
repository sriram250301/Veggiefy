package in.veggiefy.androidapp.ui.cart;

public class CartModel {

    //Values
    String imagelink;
    String productname;
    Integer price;
    String metrics;
    Integer quantity;
    Integer userquantity;
    String segment;
    String productkey;


    public CartModel() {
    }

    public String getImageLink() {
        return imagelink;
    }

    public void setImageLink(String imagelink) {
        this.imagelink = imagelink;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getProductkey() {
        return productkey;
    }

    public void setProductkey(String productkey) {
        this.productkey = productkey;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUserquantity() {
        return userquantity;
    }

    public void setUserquantity(Integer userquantity) {
        this.userquantity = userquantity;
    }
}
