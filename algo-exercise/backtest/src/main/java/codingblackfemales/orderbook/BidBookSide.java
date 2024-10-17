package codingblackfemales.orderbook;

import codingblackfemales.orderbook.visitor.FilteringOrderBookVisitor;
import codingblackfemales.orderbook.visitor.MutatingAddOrderVisitor;
import codingblackfemales.orderbook.visitor.OrderBookVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class BidBookSide extends OrderBookSide{

    private static final Logger logger = LoggerFactory.getLogger(BidBookSide.class);

    protected BidBookSide() {
        super(Comparator.reverseOrder());
    }
}

