package codingblackfemales.orderbook;

import codingblackfemales.orderbook.order.LimitOrderFlyweight;
import codingblackfemales.orderbook.order.MarketDataOrderFlyweight;
import codingblackfemales.orderbook.visitor.FilteringOrderBookVisitor;
import codingblackfemales.orderbook.visitor.MutatingAddOrderVisitor;
import codingblackfemales.orderbook.visitor.MutatingRemoveAllMarketDataOrdersVisitor;
import codingblackfemales.orderbook.visitor.OrderBookVisitor;

import java.util.Comparator;

public abstract class OrderBookSide {
    private OrderBookLevel firstLevel;
    private final MutatingAddOrderVisitor addOrderVisitor = new MutatingAddOrderVisitor();

    private final MutatingRemoveAllMarketDataOrdersVisitor removeMarketDataOrderVisitor = new MutatingRemoveAllMarketDataOrdersVisitor();

    public boolean canMatch(OrderBookSide side, long quantity, long price){
        return false;
    }

    public OrderBookLevel getFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(OrderBookLevel level) {
        firstLevel = level;
    }

    private final Comparator<Long> comparator;

    protected OrderBookSide(Comparator<Long> comparator) {
        this.comparator = comparator;
    }

    public void accept(final OrderBookVisitor visitor){

        visitor.visitSide(this);

        var levelToVisit = getFirstLevel();

        //are we the first level...
        if(isNewFirstLevel(levelToVisit, visitor)){
            OrderBookLevel level = visitor.onNoFirstLevel();
            if(level != null) {
                if(levelToVisit != null) {
                    levelToVisit.insertFirst(levelToVisit, level);
                }
                setFirstLevel(level);
                level.accept(visitor, this);
            }
            return;
        }else {
            visitOneLevel(visitor, levelToVisit, levelToVisit.next());
        }

        levelToVisit = levelToVisit.next();

        while(levelToVisit != null){
            visitOneLevel(visitor, levelToVisit, levelToVisit.next());
            levelToVisit = levelToVisit.next();
        }
    }

    private void visitOneLevel(final OrderBookVisitor visitor, OrderBookLevel levelToVisit, OrderBookLevel nextLevel) {
        if (visitor instanceof FilteringOrderBookVisitor) {

            long priceToFind = ((FilteringOrderBookVisitor) visitor).getPrice();

            if (priceToFind == levelToVisit.getPrice()) {
                levelToVisit.accept(visitor, this);
            } else if (isBetweenLevels(levelToVisit, nextLevel, priceToFind)) {
                OrderBookLevel level = visitor.missingBookLevel(levelToVisit, nextLevel, priceToFind);
                levelToVisit.insertAfter(levelToVisit,level, nextLevel);
            } else if (isNewDeepestLevel(levelToVisit, nextLevel, priceToFind)) {
                OrderBookLevel level = visitor.missingBookLevel(levelToVisit, nextLevel, priceToFind);
                levelToVisit.last().add(level);
            }
        }else{
            levelToVisit.accept(visitor, this);
        }
    }

    MutatingAddOrderVisitor getAddOrderVisitor() {
        return addOrderVisitor;
    }

    boolean isNewFirstLevel(OrderBookLevel currentFirst, OrderBookVisitor visitor) {
        if(currentFirst == null) {
            return true;
        }

        if( visitor instanceof FilteringOrderBookVisitor) {
            return comparator.compare(((FilteringOrderBookVisitor) visitor).getPrice(), currentFirst.getPrice()) == -1;
        } else {
            return false;
        }
    }

    boolean isBetweenLevels(OrderBookLevel previous, OrderBookLevel next, long price){
        return previous != null && next != null && comparator.compare(previous.getPrice(), price) == -1 && comparator.compare(next.getPrice() , price) == 1;
    }

    boolean isNewDeepestLevel(OrderBookLevel previous, OrderBookLevel next, long price){
        return previous != null && next == null && comparator.compare(previous.getPrice(), price) == -1;
    }


    void removeMarketDataOrders(){
        this.accept(removeMarketDataOrderVisitor);
    }

    void addMarketDataOrder(MarketDataOrderFlyweight order){
        this.getAddOrderVisitor().setOrderToAdd(order);
        this.accept(this.getAddOrderVisitor());
    }

    void addLimitOrder(LimitOrderFlyweight order){
        this.getAddOrderVisitor().setOrderToAdd(order);
        this.accept(this.getAddOrderVisitor());
    }

}
