package in.veggiefy.androidapp.ui.maindashboard.orders.orderview;

public class OrderViewModel {

    String productname;
    String metrics;
    int userquantity;



    public OrderViewModel() {
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public int getUserquantity() {
        return userquantity;
    }

    public void setUserquantity(int userquantity) {
        this.userquantity = userquantity;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }
}
