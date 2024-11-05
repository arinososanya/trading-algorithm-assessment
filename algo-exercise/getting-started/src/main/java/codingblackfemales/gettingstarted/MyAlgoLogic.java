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


/**
 * *
 * * My algo:
 * * Creates and cancels child orders based on the distance from the mid-price.
 * * 1. Cancels child orders that are
 * *      a. too far from the mid-price threshold (more than 5% distance)
 * *      b. partial fills
 * * 2. Handles fully filled orders by attempting to create sell orders (sell them) at the profit threshold of 2%.
 * * 3. Creates buy side child orders (above the best bid) if
 * *      a. there are less than 3 active orders
 * *      b. the order price is not too far from the mid-price threshold
 * *
 * *
 * */


public class MyAlgoLogic implements AlgoLogic { // implementing the AlgoLogic interface. This class only contains abstract methods




    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final int MAX_ACTIVE_ORDERS = 2;
    private static final int MAX_TOTAL_ORDERS = 10;
    private static final double MAX_PRICE_DISTANCE_PERCENT = 0.02;
    //    private static final long MIN_SPREAD = 2;
    private static final double PROFIT_THRESHOLD = 0.02; // initially 0.02


    @Override

    /*Initial checks and setup to:
    1. Check the state of the order book
    2. Check if the total no. of orders exceeds the max. limit
    3. Retrieve active orders and update the count
    4. Get the best ask and bid prices
    5. Checks for incomplete market data
    6. Calculates my mid-price threshold.
    */

    public Action evaluate(SimpleAlgoState state) {
            logger.info("[MYALGO] The state of the order book is:\n" + Util.orderBookToString(state));

            if (state.getChildOrders().size() > MAX_TOTAL_ORDERS) {
                return NoAction.NoAction;
            }

            List<ChildOrder> activeOrders = state.getActiveChildOrders();
            int activeOrderCount = activeOrders.size();

            final AskLevel askFarTouch = state.getAskAt(0);
            final BidLevel bidNearTouch = state.getBidAt(0);

            if (askFarTouch == null || bidNearTouch == null) {
                logger.warn("[MYALGO] Incomplete market data - unable to make decisions");
                return NoAction.NoAction;
            }

            double midPrice = (askFarTouch.price + bidNearTouch.price) / 2.0;

            boolean cancellationOccurred = false;

            // Step 1: I am checking for cancellations
        logger.info("[MYALGO] Checking {} active orders for cancellation", activeOrders.size());
        for (ChildOrder order : activeOrders) {
            logger.info("[MYALGO] Checking order: {}", order);
            if (shouldCancelOrder(order, midPrice)) {
                cancellationOccurred = true;
                logger.info("[MYALGO] Cancelling order Id: {}", order.getOrderId());
                return new CancelChildOrder(order);
            }
//            for (ChildOrder order : activeOrders) {
//                if (shouldCancelOrder(order, midPrice)) {
//                    cancellationOccurred = true;
//                    logger.info("[MYALGO] Cancelling order Id: {}", order.getOrderId());
//                    return new CancelChildOrder(order);
//                }
            }

            // Step 2: Check for sell opportunities
            for (ChildOrder order : activeOrders) {
                if (shouldCreateSellOrder(order, bidNearTouch.price)) {
                    Action sellAction = createSellOrder(order);
                    logger.info("[MYALGO] Creating sell order: {}", sellAction);
                    return sellAction;
                }
            }

            // Step 3: Create new buy order if conditions are met
            if (activeOrderCount < MAX_ACTIVE_ORDERS && canCreateBuyOrder(bidNearTouch, midPrice)) {
                Action buyAction = createBuyOrder(bidNearTouch, askFarTouch);
                logger.info("[MYALGO] Creating buy order: {}", buyAction);
                return buyAction;
            }
            return NoAction.NoAction;
        }

        // My helper methods
        private boolean shouldCancelOrder(ChildOrder order, double midPrice) {
            double priceDistance = Math.abs(order.getPrice() - midPrice) / midPrice;

            // Add debug logging
            logger.info("[MYALGO] Cancel Check - OrderId: {}, Side: {}, Price: {}, MidPrice: {}, Distance: {}%, Max Allowed: {}%",
                    order.getOrderId(),
                    order.getSide(),
                    order.getPrice(),
                    midPrice,
                    priceDistance * 100,
                    MAX_PRICE_DISTANCE_PERCENT * 100);

            // Handle partial fills for both buy and sell orders
            if (order.getFilledQuantity() > 0 && order.getFilledQuantity() < order.getQuantity()) {
                logger.info("[MYALGO] Order {} should be cancelled. Reason: Partial fill", order.getOrderId());
                return true;
            }

            // Only apply price distance check to BUY orders
            if (order.getSide() == Side.BUY && priceDistance > MAX_PRICE_DISTANCE_PERCENT) {
                logger.info("[MYALGO] Order {} should be cancelled. Reason: Buy price too far from mid", order.getOrderId());
                return true;
            }

            // For sell orders, cancel if price distance is too large (more than 2% from mid)
            if (order.getSide() == Side.SELL && priceDistance > MAX_PRICE_DISTANCE_PERCENT) {
                logger.info("[MYALGO] Order {} should be cancelled. Reason: Sell price too far from mid", order.getOrderId());
                return true;
            }

            return false;
        }

    private boolean shouldCreateSellOrder(ChildOrder order, long bidPrice) {
        // Modify logic to allow selling of partially filled orders
        return order.getSide() == Side.BUY &&
                order.getFilledQuantity() > 0 && // Allow partial fills to be considered
                (long) (order.getPrice() * (1 + PROFIT_THRESHOLD)) <= bidPrice;
    }

//        private boolean shouldCreateSellOrder (ChildOrder order,long bidPrice){
//            return order.getSide() == Side.BUY &&
//                    order.getFilledQuantity() == order.getQuantity() &&
//                    (long) (order.getPrice() * (1 + PROFIT_THRESHOLD)) >= bidPrice;
//        }

        private Action createSellOrder (ChildOrder buyOrder){
            long sellPrice = (long) (buyOrder.getPrice() * (1 + PROFIT_THRESHOLD));
            return new CreateChildOrder(Side.SELL, buyOrder.getQuantity(), sellPrice);
        }

        private boolean canCreateBuyOrder (BidLevel bidNearTouch,double midPrice){
            long buyPrice = bidNearTouch.price + 1L;
            double priceDistance = Math.abs(buyPrice - midPrice) / midPrice;
            return priceDistance <= MAX_PRICE_DISTANCE_PERCENT;
        }

        private Action createBuyOrder (BidLevel bidNearTouch, AskLevel askFarTouch){
            long buyPrice = bidNearTouch.price + 1L;
            long quantity = Math.min(100, askFarTouch.quantity); // whatever is the min value between quantity on ask side vs 100. This helps prevent my algorithm from trying to trade sizes that might be too large for the available liquidity in the market.
            return new CreateChildOrder(Side.BUY, quantity, buyPrice);
        }

    }
