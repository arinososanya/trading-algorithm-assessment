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
    private static final long MIN_SPREAD = 2; // I will only consider creating orders if the spread is at least 2
    private static final double PROFIT_THRESHOLD = 0.02; // The algorithm will attempt to sell when it can make at least 2% profit

    @Override
    public Action evaluate(SimpleAlgoState state) { // State is an instance of SimpleAlgoState i.e it's an object of type SimpleAlgoState, that's passed to the evaluate method. The evaluate method returns an Action, which can be a CancelChildOrder, CreateChildOrder, or NoAction.
        var orderBookAsString = Util.orderBookToString(state); // checks the current state of the order book and logs it
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        /********
         *
         * My algo:
         * Creates and cancels child orders based on the distance from the mid-price.
         * 1. Cancels child orders that are too far from the mid-price threshold (more than 5% distance).
         * 2. Creates child orders that are within the threshold and if there are less than 3 active orders.
         *    - If these conditions are met, we create an aggressive buy order just above the best bid (ask?) price.
         *    - The quantity is set to 100 or the available quantity at the best ask (whichever is smaller)
         * 3. Then, check for filled buy orders.
         *     - If a buy order is partially filled, we try to sell the remaining quantity.
         *     - If a buy order is fully filled, then we sell it. The sell price is set to make at least a 2% profit.
         */


        var totalOrderCount = state.getChildOrders().size(); // get the total number of child orders, regardless of their current status (rejected, cancelled, filled, active(live) orders etc.)
        //   My exit condition... this is a safety check for if there are more than 10 child orders, then the algo does nothing
        if (totalOrderCount > MAX_TOTAL_ORDERS) {
            return NoAction.NoAction;
        }


        // 2. Get the market data (best bid and ask, spread etc.)
        final AskLevel askFarTouch = state.getAskAt(0);
        final BidLevel bidNearTouch = state.getBidAt(0);

        // For display’s sake, get the number of bid and ask levels (NECESSARY?)
        int bidLevels = state.getBidLevels();
        int askLevels = state.getAskLevels();
        logger.info("[MYALGO] Market Depth - Bid Levels: {}, Ask Levels: {}", bidLevels, askLevels);

        // Make sure both values are not null (consider exception handling here)
        if (askFarTouch == null || bidNearTouch == null) {
            logger.warn("[MYALGO] Incomplete market data - unable to make decisions");
            return NoAction.NoAction;
        }

        // 1. Check if we need to cancel old orders by comparing the distance of the order to the mid-price and seeing if this distance is within the threshold

        // Calculate the mid-price
        double midPrice = (askFarTouch.price + bidNearTouch.price) / 2.0; // the average of the best bid and best ask prices:

        // Check all active orders (iterate) for distance from the mid-price
        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        for (ChildOrder order : activeOrders) { // For each active order, the algorithm calculates how far the order's price is from this mid-price as a percentage
            double priceDistance = Math.abs(order.getPrice() - midPrice) / midPrice; // precentage diff

            if (priceDistance > MAX_PRICE_DISTANCE_PERCENT) {
                logger.info("[MYALGO] Cancelling order {} due to excessive price distance: {}%", order.getOrderId(), String.format("%.2f", priceDistance * 100));
                return new CancelChildOrder(order);
            }
        }


//     If all is good, create and then calculate the spread (You can use this to take advantage of the market and make profit)
        final long spread = askFarTouch.price - bidNearTouch.price;
        if (spread < 0) {
            logger.warn("[MYALGO] Negative spread detected: {}. Possible market anomaly or data error.", spread);
            return NoAction.NoAction;
        }
        if (spread < MIN_SPREAD) {
            logger.info("[MYALGO] Spread too small for order creation: {}", spread);
            /* return NoAction.NoAction;*/
        } else logger.info("[MYALGO] Spread is: {}", spread);

        // So we can visually see what the best bid and ask are
        logger.info("[MYALGO] Best Bid: {} @ {}, Best Ask: {} @ {}",
                bidNearTouch.quantity, bidNearTouch.price,
                askFarTouch.quantity, askFarTouch.price);

        // CREATING ORDERS

        // Calculate the mid-price

        var activeOrderCount = activeOrders.size();
        if (activeOrderCount < MAX_ACTIVE_ORDERS) {
            long buyPrice = bidNearTouch.price + 1L; // Place the buy order just above the best bid

            // Check if the proposed price is within the acceptable range of the mid-price
            double priceDistance = Math.abs(buyPrice - midPrice) / midPrice;

            if (priceDistance <= MAX_PRICE_DISTANCE_PERCENT) {
                long quantity = Math.min(100, askFarTouch.quantity); // I'm still limiting quantity for risk management
                logger.info("[MYALGO] Creating new buy order: {} @ {} (Distance from mid-price: {}%)",
                        quantity, buyPrice, String.format("%.2f", priceDistance * 100));
                return new CreateChildOrder(Side.BUY, quantity, buyPrice);
                // Create buy order if conditions are met
            } else {
                logger.info("[MYALGO] Proposed order price too far from mid-price. Distance: {}%",
                        String.format("%.2f", priceDistance * 100));
            }
        }

        // Check if there are any unfilled orders and place new buy orders
        for (ChildOrder order : activeOrders) {
            if (order.getSide() == Side.BUY) {
                long filledQuantity = order.getFilledQuantity();
                long totalQuantity = order.getQuantity();

                // If the filled quantity is less than the total quantity, that means that the buy order was partially filled.
                if (filledQuantity < totalQuantity) {
                    long remainingQuantity = totalQuantity - filledQuantity;
                    long newBuyPrice = bidNearTouch.price + 1L; // Placing my order just above the best bid

                    logger.info("[MYALGO] Creating new buy order for remaining quantity: {} @ {}", remainingQuantity, newBuyPrice);
                    return new CreateChildOrder(Side.BUY, remainingQuantity, newBuyPrice);
                } else if (filledQuantity == totalQuantity) { // then the initial buy order was completely filled
                    long lastBuyPrice = order.getPrice();
                    long potentialSellPrice = (long) (lastBuyPrice * (1 + PROFIT_THRESHOLD)); // profit threshold. This will allow the user to set the percentage of profit they want to make.
                    if (potentialSellPrice <= askFarTouch.price) {
                        // Calculating the actual profit
                        long profit = (potentialSellPrice - lastBuyPrice) * filledQuantity;
                        double profitPercentage = (PROFIT_THRESHOLD * 100);
                        logger.info("[MYALGO] Buy order filled: {} @ {}", filledQuantity, lastBuyPrice);
                        logger.info("[MYALGO] Creating sell order: {} @ {}", filledQuantity, potentialSellPrice);
                        logger.info("[MYALGO] Expected profit: {} ticks ({}%)",
                                profit, String.format("%.2f", profitPercentage));

                        return new CreateChildOrder(Side.SELL, filledQuantity, potentialSellPrice);
                    }
                }
            }
        }
        // If none of the conditions for creating and cancelling orders are met
        return NoAction.NoAction;
    }
}






            // OLD LOGIC
//        // Check if we have any filled buy orders to potentially sell. Hard code values. Check if it was fil
//        for(ChildOrder order : activeOrders) {
//            if (order.getSide() == Side.BUY && order.getFilledQuantity() > 0){
//                long remainingQuantity = order.getQuantity() - order.getFilledQuantity(); // not needed
//                if (remainingQuantity > 0){
//                    long potentialSellPrice = (long)(order.getPrice() * (1 + PROFIT_THRESHOLD)); // profit threshold. This will allow the user to set the percentage of profit they want to make.
//                    if (potentialSellPrice <= askFarTouch.price){
//                        logger.info("[MYALGO] Creating sell order for partially filled buy order: {} @ {}", remainingQuantity, potentialSellPrice);
//                        return new CreateChildOrder(Side.SELL, order.getFilledQuantity(), potentialSellPrice);
//                    }
//



/*
Overview: Adding and Cancelling
1. Get the best bid and ask and then calculate the spread.
I also want to log the number of bid and ask levels for a broader view of market depth.

2. I want to allow for up to 3 multiple active orders at different levels below the current market price
because if the price drops significantly, I might fill multiple orders and acquire a larger position at a good average price.



 /*
        Mine:
        Evaluate the current market state using SimpleAlgoState state method


        Get current active orders
      var activeOrders = state.getActiveChildOrders();

        // If I have active orders, cancel the old or oldest one. Oldest one = > 10 ticks
        if(!activeOrders.isEmpty()){

        }

        // If the no of active orders is below the maximum limit, (MAX_ORDERS), then my algo will consider adding a new buy order just above the best bid price


        // If we didn't cancel or create an order, do nothing
        return NoAction.NoAction;*/

        /* To make profit: I want to sell the asset for more than I bought it for. The smaller the spread,
        the quicker you might be able to make a profit, but potentially a smaller one.
        create a buy and sell threshold in your logic with your
        algo only buying when your price is at that threshold or below it
        and selling when it reaches your sell threshold or exceeds it.
        We could use the new trade method in SimpleAlgoState to analyse this.
        *
        * */