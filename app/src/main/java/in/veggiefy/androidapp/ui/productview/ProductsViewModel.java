package in.veggiefy.androidapp.ui.productview;

public class ProductsViewModel {
    String productname;
    String metrics;
    String imagelink;
    String specialtext;
    int quantity;
    int price;

    public ProductsViewModel(){}


    public ProductsViewModel(String productname, String metrics, String imageLink, int quantity, int price) {
        this.productname = productname;
        this.metrics = metrics;
        this.imagelink = imageLink;
        this.quantity = quantity;
        this.price = price;
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

    public String getImageLink() {
        return imagelink;
    }

    public void setImageLink(String imagelink) {
        this.imagelink = imagelink;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSpecialtext() {
        return specialtext;
    }

    public void setSpecialtext(String specialtext) {
        this.specialtext = specialtext;
    }
}
