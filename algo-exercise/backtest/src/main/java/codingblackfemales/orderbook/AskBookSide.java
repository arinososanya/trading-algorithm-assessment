package codingblackfemales.orderbook;

import codingblackfemales.orderbook.visitor.FilteringOrderBookVisitor;
import codingblackfemales.orderbook.visitor.MutatingAddOrderVisitor;
import codingblackfemales.orderbook.visitor.OrderBookVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class AskBookSide extends OrderBookSide {

    private static final Logger logger = LoggerFactory.getLogger(AskBookSide.class);

    public AskBookSide() {
        super(Comparator.naturalOrder());
    }

}
