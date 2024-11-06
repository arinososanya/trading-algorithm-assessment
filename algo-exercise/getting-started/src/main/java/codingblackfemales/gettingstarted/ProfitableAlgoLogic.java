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

/**
 * My second, profit-focused trading algorithm that aims to benefit from upward price movements
 * while managing risk with stop-losses.
 *
 * This algorithm manages a pair-based trading strategy with the following key components:
 *
 * 1. Risk Management:
 *    - Limits to 2 maximum active orders
 *    - Implements stop-loss of 5 ticks below entry
 *    - Performs safety checks on market data integrity
 *    - Tracks buy/sell order pairs using a HashMap
 *
 * 2. Buy Side Strategy:
 *    - Only creates new buy orders when no active orders exist
 *    - Places buy orders just above best bid (+1 tick)
 *    - For first order only, allows buying at ask price to ensure entry
 *    - Uses fixed quantity of 100 units per order
 *
 * 3. Sell Side Management:
 *    - Creates sell orders for fully filled buy orders
 *    - Targets minimum 2 tick profit
 *    - Dynamically adjusts sell prices based on market movement
 *    - Cancels and replaces sell orders when better prices are available
 *
 * 4. Order Cancellation Logic:
 *    - Cancels unfilled/partial buy orders if market moves significantly
 *    - Cancels sell orders if market moves up for better opportunities
 *    - Utilises 1% price buffer for order management
 *
 * The algorithm prioritises capital preservation while attempting to
 * capture profits from market movements, using a combination of fixed
 * tick targets and dynamic price optimisation.
 */

public class ProfitableAlgoLogic implements AlgoLogic {
    private Map<ChildOrder, ChildOrder> orderPairs = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ProfitableAlgoLogic.class);
    private static final int MAX_ACTIVE_ORDERS = 2;    // Reduced to control order flow
    private static final int PROFIT_TICKS = 2;         // Minimum profit target
    private static final int STOP_LOSS_TICKS = 5;         // Minimum stop loss
    double bufferPercentage = 0.01;

    @Override
    public Action evaluate(SimpleAlgoState state) {
        try {
            List<ChildOrder> activeOrders = state.getActiveChildOrders();
            BidLevel bestBid = state.getBidAt(0);
            AskLevel bestAsk = state.getAskAt(0);
            double bufferAmount = bestBid.getPrice() * bufferPercentage;

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

            // Looking for completely filled buys that need sell orders
            for (ChildOrder buyOrder : activeOrders) {
                if (buyOrder.getSide() == Side.BUY &&
                        buyOrder.isFullyFilled()) {

                    // Check for profitable sell or cancel existing sell
                    if (hasOpenSellOrder(activeOrders, buyOrder)) {
                        ChildOrder existingSellOrder = orderPairs.get(buyOrder);
                        if (existingSellOrder.getPrice() < bestBid.price + PROFIT_TICKS) {
                            logger.info("[MYALGO] Cancelling existing sell {} and creating new sell for buy order {} at profit target",
                                    existingSellOrder.getOrderId(), buyOrder.getOrderId());
                            return cancelAndCreateSellOrder(buyOrder);
                        }
                    } else {
                        // This is where I adjust the sell price
                        long adjustedSellPrice = calculateAdjustedSellPrice(buyOrder.getPrice(), bestBid.getPrice());
                        logger.info("[MYALGO] Creating sell for fully filled buy order {} at adjusted price: {}",
                                buyOrder.getOrderId(), adjustedSellPrice);
                        return createProfitableSellOrder(buyOrder, bufferAmount);
                    }
                }
            }

            // Creating new buy only if we have no orders at all
            if (activeOrders.isEmpty()) {
                logger.info("[MYALGO] No active orders - creating new buy");
                return createBuyOrder(state, bestBid, bestAsk);
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

    private Action createProfitableSellOrder(ChildOrder buyOrder, double bufferAmount) {
        long sellPrice = buyOrder.getPrice() + PROFIT_TICKS; // Sell higher than our buy
        long stopLossPrice = buyOrder.getPrice() - STOP_LOSS_TICKS; // Set a stop-loss


        logger.info("[MYALGO] Creating sell order: qty={}, price={}, stop loss={} for buy order {}",
                buyOrder.getFilledQuantity(),
                sellPrice,
                stopLossPrice,
                buyOrder.getOrderId());

        return new CreateChildOrder(
                Side.SELL,
                buyOrder.getFilledQuantity(),
                sellPrice
        );
    }

    private Action createBuyOrder(SimpleAlgoState state, BidLevel bestBid, AskLevel bestAsk) {
        long buyPrice = bestBid.price + 1;

        // Only for the first buy order, allow buying at the ask price
        if (state.getChildOrders().isEmpty()) {
            buyPrice = Math.min(buyPrice, bestAsk.price);
        }

        return new CreateChildOrder(
                Side.BUY,
                100,  // Fixed size for now
                buyPrice
        );
    }

    private boolean shouldCancelOrder(ChildOrder order, BidLevel bestBid, AskLevel bestAsk) {
        if (order.getSide() == Side.BUY && !order.isFullyFilled()) {
            // Cancel unfilled/partially filled buy if market moved significantly
            return order.getPrice() < bestBid.price || order.getPrice() >= bestAsk.price;
        } else if (order.getSide() == Side.SELL) {
            // Cancel sell if market moved up and we can sell higher
            return order.getPrice() < bestBid.price + PROFIT_TICKS;
        }

        return false;
    }

    private Action cancelAndCreateSellOrder(ChildOrder buyOrder) {
        ChildOrder existingSellOrder = orderPairs.get(buyOrder);
        orderPairs.remove(buyOrder); // Remove the old sell order

        return new CancelChildOrder(existingSellOrder); // Cancel the old sell order
    }

    private long calculateAdjustedSellPrice(long buyPrice, long currentBestBid) {
        // Declare baseTargetPrice first
        long baseTargetPrice = buyPrice + PROFIT_TICKS;

        if (currentBestBid > buyPrice + PROFIT_TICKS) {
            // Set sell price just below the current best bid to increase fill probability
            // while still maintaining good profit
            long marketBasedPrice = currentBestBid - 1;

            // Use Math.max (note the capital M)
            return Math.max(baseTargetPrice, marketBasedPrice);
        }

        return baseTargetPrice;
    }
}