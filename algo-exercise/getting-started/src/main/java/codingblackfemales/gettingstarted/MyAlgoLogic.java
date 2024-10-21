package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class MyAlgoLogic implements AlgoLogic { // implementing the AlgoLogic interface. This class only contains abstract methods

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final int MAX_ACTIVE_ORDERS = 3; // The maximum no. of Active orders allowed at any given time
    private static final int MAX_TOTAL_ORDERS = 10; // The max. total number of orders (incl. active, filled, partially filled,canceled and rejected)
    private static final double MAX_PRICE_DISTANCE_PERCENT = 0.05; // 5% distance threshold (for mid-price)
//    private static final long MIN_SPREAD = 2; // I will only consider creating orders if the spread is at least 2
    private static final double PROFIT_THRESHOLD = 0.02; // The algorithm will attempt to sell when it can make at least 2% profit
    private int activeOrderCount = 0;

    /********
     *
     * My algo:
     * Creates and cancels child orders based on the distance from the mid-price.
     * 1. Cancels child orders that are
     *      a. too far from the mid-price threshold (more than 5% distance)
     *      b. partial fills
     * 2. Handles fully filled orders by attempting to create sell orders (sell them) at the profit threshold of 2%.
     * 3. Creates buy side child orders (above the best bid) if
     *      a. there are less than 3 active orders
     *      b. the order price is not too far from the mid-price threshold
     *
     * Note:
     * Creation of buy orders happens only if no cancellations or sell orders were created in the current evaluation.
     */


    @Override
    public Action evaluate(SimpleAlgoState state) { // State is an instance of SimpleAlgoState i.e it's an object of type SimpleAlgoState, that's passed to the evaluate method. The evaluate method returns an Action, which can be a CancelChildOrder, CreateChildOrder, or NoAction.
        var orderBookAsString = Util.orderBookToString(state); // checks the current state of the order book and logs it
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);


        var totalOrderCount = state.getChildOrders().size(); // get the total number of child orders, regardless of their current status (rejected, cancelled, filled, active(live) orders etc.)
        //   My exit condition... this is a safety check for if there are more than 10 child orders, then the algo does nothing
        if (totalOrderCount > MAX_TOTAL_ORDERS) {
            return NoAction.NoAction;
        }


        // Update activeOrderCount based on the current state
        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        activeOrderCount = activeOrders.size();

        // 2. Get the market data (best bid and ask, spread etc.)
        final AskLevel askFarTouch = state.getAskAt(0);
        final BidLevel bidNearTouch = state.getBidAt(0);

        // Make sure both values are not null (consider exception handling here)
        if (askFarTouch == null || bidNearTouch == null) {
            logger.warn("[MYALGO] Incomplete market data - unable to make decisions");
            return NoAction.NoAction;
        }

        // Calculate the mid-price (the core of my logic)
        double midPrice = (askFarTouch.price + bidNearTouch.price) / 2.0; // the average of the best bid and best ask prices:


        // 1. Check if we need to cancel old orders by comparing the distance of the order to the mid-price and seeing if this distance is within the threshold
        // Also, we handle partial fills
        for (ChildOrder order : activeOrders) { // For each active order, the algorithm calculates how far the order's price is from this mid-price as a percentage
            double priceDistance = Math.abs(order.getPrice() - midPrice) / midPrice; // precentage diff

            if (priceDistance > MAX_PRICE_DISTANCE_PERCENT) {
                logger.info("[MYALGO] Cancelling order {} due to excessive price distance: {}%", order.getOrderId(), String.format("%.2f", priceDistance * 100));
                activeOrderCount--;
                return new CancelChildOrder(order);
            }
            if (order.getFilledQuantity() > 0 && order.getFilledQuantity() < order.getQuantity()) {
                logger.info("[MYALGO] Cancelling partially filled order: {}", order.getOrderId());
                return new CancelChildOrder(order);
            }
        }


//     2. Handle fully filled buy orders:
        for (ChildOrder order : activeOrders) {
            if (order.getSide() == Side.BUY && order.getFilledQuantity() == order.getQuantity()) {
                long lastBuyPrice = order.getPrice();
                long potentialSellPrice = (long) (lastBuyPrice * (1 + PROFIT_THRESHOLD));
                if (potentialSellPrice <= askFarTouch.price) {
                    logger.info("[MYALGO] Creating sell order: {} @ {}", order.getQuantity(), potentialSellPrice);
                    return new CreateChildOrder(Side.SELL, order.getQuantity(), potentialSellPrice);
                }
            }
        }

// Create a new buy order if conditions are met
        if (activeOrderCount < MAX_ACTIVE_ORDERS) {
            long buyPrice = bidNearTouch.price + 1L;
            double priceDistance = Math.abs(buyPrice - midPrice) / midPrice;

            if (priceDistance <= MAX_PRICE_DISTANCE_PERCENT) {
                long quantity = Math.min(100, askFarTouch.quantity);
                logger.info("[MYALGO] Creating new buy order: {} @ {} (Distance from mid-price: {}%)",
                        quantity, buyPrice, String.format("%.2f", priceDistance * 100));
                return new CreateChildOrder(Side.BUY, quantity, buyPrice);
            }
        }

        return NoAction.NoAction;
    }
}

//
//
//
//