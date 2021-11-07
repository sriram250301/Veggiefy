package in.veggiefy.androidapp.ui.maindashboard.orders;

public class OrdersModel {

    //Variables

    //variables to place order
    String address,deliverydate,transactionid;
    String orderid,ordertime;
    int amount;
    boolean delivery,paystatus,orderclosed;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliverydate() {
        return deliverydate;
    }

    public void setDeliverydate(String deliverydate) {
        this.deliverydate = deliverydate;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getOrdertime() {
        return ordertime;
    }

    public void setOrdertime(String ordertime) {
        this.ordertime = ordertime;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isPaystatus() {
        return paystatus;
    }

    public void setPaystatus(boolean paystatus) {
        this.paystatus = paystatus;
    }

    public boolean isOrderclosed() {
        return orderclosed;
    }

    public void setOrderclosed(boolean orderclosed) {
        this.orderclosed = orderclosed;
    }
}
