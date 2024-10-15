package codingblackfemales.sotw.marketdata;

public class AbstractLevel {
    public long price;
    public long quantity;
    public int level;

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public int getLevel() {return level; }


}
