package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.collection.extrusive.Map;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class MyAlgoLogic implements AlgoLogic { // implementing the AlgoLogic interface. This class only contains abstract methods

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final int MAX_ORDERS = 3;
    private static final int MAX_TOTAL_ORDERS = 10;
    private static final long MAX_PRICE_DISTANCE_PERCENT = 1000; // I'm going to assume that 1000 ticks is one day
    private static final long MIN_SPREAD = 2; // I will only consider creating orders if the spread is at least 2


    @Override
    public Action evaluate(SimpleAlgoState state) { // State is an instance of SimpleAlgoState i.e it's an object of type SimpleAlgoState, that's passed to the evaluate method. The evaluate method returns an Action, which can be a CancelChildOrder, CreateChildOrder, or NoAction.
        var orderBookAsString = Util.orderBookToString(state); // checks the current state of the order book and logs it
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        var totalOrderCount = state.getChildOrders().size(); // get the total number of child orders, regardless of their current status (rejected, cancelled, filled, active(live) orders etc.)

        //make sure we have an exit condition... this is a safety check for if there are more than 15 child orders, then the algo does nothing
        if (totalOrderCount > MAX_TOTAL_ORDERS) {
            return NoAction.NoAction;
        }

        // 1. Check if we need to cancel old orders
        List<ChildOrder> allOrders = state.getChildOrders();
        if (allOrders.size() > MAX_TOTAL_ORDERS) {
            ChildOrder oldestOrder = allOrders.get(0); //assuming orders are stored in chronological order
            logger.info("[MYALGO] Cancelling oldest order to maintain order limit");
            return new CancelChildOrder(oldestOrder);
        }

        // 1. Get the number of bid and ask levels
        int bidLevels = state.getBidLevels();
        int askLevels = state.getAskLevels();
        logger.info("[MYALGO] Market Depth - Bid Levels: {}, Ask Levels: {}", bidLevels, askLevels);

        // 2. Get the market data (best bid and ask)
        final AskLevel askFarTouch = state.getAskAt(0);
        final BidLevel bidNearTouch = state.getBidAt(0);

        // Make sure both values are not null
        if (askFarTouch == null || bidNearTouch == null) {
            logger.warn("[MYALGO] Incomplete market data - unable to make decisions");
            return NoAction.NoAction;
        }

//     If all is good, create and then calculate the spread
        final long spread = askFarTouch.price - bidNearTouch.price;
        if (spread < 0) {
            logger.warn("[MYALGO] Negative spread detected: {}. Possible market anomaly or data error.", spread);
            return NoAction.NoAction;
        }
        if (spread < MIN_SPREAD) {
            logger.info("[MYALGO] Spread too small for order creation: {}", spread);
            return NoAction.NoAction;
        }

        // So we can visually see what the best bid and ask are
        logger.info("[MYALGO] Best Bid: {} @ {}, Best Ask: {} @ {}",
                bidNearTouch.quantity, bidNearTouch.price,
                askFarTouch.quantity, askFarTouch.price);

        // Create order if conditions are met
        var activeOrderCount = state.getActiveChildOrders().size();
        if (activeOrderCount < MAX_ORDERS) {
            // Place the buy order at the ask price to cross the spread
            long price = askFarTouch.price;
            long quantity = Math.min(100, askFarTouch.quantity); // I'm still limiting quantity for risk management
            logger.info("[MYALGO] Creating new aggressive buy order: {} @ {}", quantity, price);
            return new CreateChildOrder(Side.BUY, quantity, price);

        }

    return NoAction.NoAction;
    }
}

/********
 *
 * Add your logic here....
 *
 */

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