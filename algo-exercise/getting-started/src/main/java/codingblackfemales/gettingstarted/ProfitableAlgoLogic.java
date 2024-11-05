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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfitableAlgoLogic implements AlgoLogic {
    private Map<ChildOrder, ChildOrder> orderPairs = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ProfitableAlgoLogic.class);
    private static final int MAX_ACTIVE_ORDERS = 2;    // Reduced to control order flow
    private static final int PROFIT_TICKS = 2;         // Minimum profit target

    @Override
    public Action evaluate(SimpleAlgoState state) {
        try {
            List<ChildOrder> activeOrders = state.getActiveChildOrders();
            BidLevel bestBid = state.getBidAt(0);
            AskLevel bestAsk = state.getAskAt(0);

            logger.info("[MYALGO] The state of the order book is:\n" + Util.orderBookToString(state));
            logger.info("[MYALGO] Active orders: {}", activeOrders.size());

            // Basic safety checks
            if (bestBid == null || bestAsk == null || activeOrders.size() >= MAX_ACTIVE_ORDERS) {
                logger.info("[MYALGO] Safety check failed - no action");
                return NoAction.NoAction;
            }

            // First check for orders that need canceling
            for (ChildOrder order : activeOrders) {
                if (shouldCancelOrder(order, bestBid, bestAsk)) {
                    logger.info("[MYALGO] Cancelling order: {}", order);
                    return new CancelChildOrder(order);
                }
            }

            // Look for completely filled buys that need sell orders
            for (ChildOrder order : activeOrders) {
                if (order.getSide() == Side.BUY &&
                        order.getFilledQuantity() == order.getQuantity() && // Only fully filled orders
                        !hasOpenSellOrder(activeOrders, order)) {

                    logger.info("[MYALGO] Creating sell for fully filled buy order {} at profit target",
                            order.getOrderId());
                    return createProfitableSellOrder(order);
                }
            }

            // Create new buy only if we have no orders at all
            if (activeOrders.isEmpty()) {
                logger.info("[MYALGO] No active orders - creating new buy");
                return createBuyOrder(bestBid, bestAsk);
            }

            logger.info("[MYALGO] No action needed");
            return NoAction.NoAction;

        } catch (Exception e) {
            logger.error("[MYALGO] Error in evaluate", e);
            return NoAction.NoAction;
        }
    }

    private boolean hasOpenSellOrder(List<ChildOrder> activeOrders, ChildOrder buyOrder) {
        return activeOrders.stream().anyMatch(order -> order.getSide() == Side.SELL && orderPairs.containsKey(order));
    }

    private Action createProfitableSellOrder(ChildOrder buyOrder) {
        long sellPrice = buyOrder.getPrice() + PROFIT_TICKS; // Sell higher than our buy

        logger.info("[MYALGO] Creating sell order: qty={}, price={} for buy order {}",
                buyOrder.getFilledQuantity(),
                sellPrice,
                buyOrder.getOrderId());

        return new CreateChildOrder(
                Side.SELL,
                buyOrder.getFilledQuantity(),
                sellPrice
        );
    }

    private Action createBuyOrder(BidLevel bestBid, AskLevel bestAsk) {
        // Place buy order one tick above best bid to be competitive
        long buyPrice = bestBid.price + 1;

        // Don't buy if our price would cross the spread
        if (buyPrice >= bestAsk.price) {
            return NoAction.NoAction;
        }

        return new CreateChildOrder(
                Side.BUY,
                100,  // Fixed size for now
                buyPrice
        );
    }

    private boolean shouldCancelOrder(ChildOrder order, BidLevel bestBid, AskLevel bestAsk) {
        // Cancel orders that are too old
        if (order.getAgeInTicks() > 10) {
            return true;
        }

        if (order.getSide() == Side.BUY && !order.isFullyFilled()) {
            // Cancel unfilled/partially filled buy if market moved significantly
            return order.getPrice() < bestBid.price || order.getPrice() >= bestAsk.price;
        } else if (order.getSide() == Side.SELL) {
            // Cancel sell if market moved up and we can sell higher
            return order.getPrice() < bestBid.price + PROFIT_TICKS;
        }

        return false;
    }
}