package codingblackfemales.sotw;

import messages.order.Side;

import java.time.Instant;

// I'm creating a Trade class to give me information on recent market activity which can be an indication of short-term market trends and be used in SimpleAlgoState and then MyAlgoLogic
public class Trade {
private final long price;
private final long quantity;
private final Side side;
private final Instant timestamp;

// My constructor
public Trade(long price, long quantity, Side side, Instant timestamp){
    this.price = price;
    this.quantity = quantity;
    this.side = side;
    this.timestamp = timestamp;
}

public long getPrice(){
    return price;
}

public long getQuantity(){
    return quantity;
}

public Side getSide(){
    return side;
}

public Instant getTimestamp(){
    return timestamp;
}
@Override
public String toString(){
    return "Trade{" + "price=" + price +
            ", quantity=" + quantity +
            ", side=" + side +
            ", timestamp=" + timestamp +
            "}";
}

}
