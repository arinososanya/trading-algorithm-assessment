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

import java.util.ArrayList;
import java.util.List;

public class MyAlgoLogicNew implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final int MAX_ACTIVE_ORDERS = 3;
    private static final int MAX_TOTAL_ORDERS = 10;
    private static final double MAX_PRICE_DISTANCE_PERCENT = 0.05;
    private static final double PROFIT_THRESHOLD = 0.02;


    @Override
    public Action evaluate(SimpleAlgoState state) {
        try {
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
            for (ChildOrder order : activeOrders) {
                if (shouldCancelOrder(order, midPrice)) {
                    cancellationOccurred = true;
                    logger.info("[MYALGO] Cancelling order: {}", order.getOrderId());
                    return new CancelChildOrder(order);
                }
            }

            // Step 2: Check for sell opportunities
            for (ChildOrder order : activeOrders) {
                if (shouldCreateSellOrder(order, askFarTouch.price)) {
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
        } catch (Exception e) {
            logger.error("[MYALGO] An error occurred during evaluation", e);
            return NoAction.NoAction;
        }
    }

    // My helper methods
    private boolean shouldCancelOrder(ChildOrder order, double midPrice) {
        double priceDistance = Math.abs(order.getPrice() - midPrice) / midPrice;
        return priceDistance > MAX_PRICE_DISTANCE_PERCENT ||
                (order.getFilledQuantity() > 0 && order.getFilledQuantity() < order.getQuantity());
    }

    private boolean shouldCreateSellOrder(ChildOrder order, long askPrice) {
        return order.getSide() == Side.BUY &&
                order.getFilledQuantity() == order.getQuantity() &&
                (long)(order.getPrice() * (1 + PROFIT_THRESHOLD)) <= askPrice;
    }

    private Action createSellOrder(ChildOrder buyOrder) {
        long sellPrice = (long)(buyOrder.getPrice() * (1 + PROFIT_THRESHOLD));
        return new CreateChildOrder(Side.SELL, buyOrder.getQuantity(), sellPrice);
    }

    private boolean canCreateBuyOrder(BidLevel bidNearTouch, double midPrice) {
        long buyPrice = bidNearTouch.price + 1L;
        double priceDistance = Math.abs(buyPrice - midPrice) / midPrice;
        return priceDistance <= MAX_PRICE_DISTANCE_PERCENT;
    }

    private Action createBuyOrder(BidLevel bidNearTouch, AskLevel askFarTouch) {
        long buyPrice = bidNearTouch.price + 1L;
        long quantity = Math.min(100, askFarTouch.quantity);
        return new CreateChildOrder(Side.BUY, quantity, buyPrice);
    }
}