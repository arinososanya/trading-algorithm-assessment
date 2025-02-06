package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action; // The Action interface is fundamental to how the algo communicates its trading decisions
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
import java.util.stream.Collectors;


/**
 * My first algorithm is a market-making algorithm that manages orders based on market conditions and price thresholds.
 *
 * The algorithm follows a three-step evaluation process on each update:
 *
 * 1. Order Risk Management:
 *    - Cancels existing orders that are too far from the mid-price (>5% distance)
 *    - Cancels partially filled orders to manage execution risk
 *    - Enforces a maximum of 10 total orders to limit exposure
 *
 * 2. Profit Taking:
 *    - Monitors fully filled buy orders for sell opportunities
 *    - Creates matching sell orders when the market moves favourably
 *    - Targets 2% profit threshold for any sell orders
 *
 * 3. Market Making:
 *    - Creates new buy orders slightly above the best bid
 *    - Maintains maximum 3 active orders at any time
 *    - Only places orders within 5% of mid-price for risk management
 *    - Adapts order quantity based on available market liquidity
 *
 * My algorithm prioritises risk management (cancellations) over profit-taking (sells)
 * and new order creation (buys). It uses mid-price as a reference point for decision-making
 * and includes safety checks for market data integrity.
 */

public class MarketMakingAlgo implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MarketMakingAlgo.class);
    private static final int MAX_TOTAL_ORDERS = 10; // Max 'orders' for the 'whole day'
    static final double MAX_PRICE_DISTANCE_PERCENT = 0.05; // This is the maximum deviation from the mid-price that we allow
    private static final double PROFIT_THRESHOLD = 0.02;


    @Override
    public Action evaluate(SimpleAlgoState state) { // Hey, here's what's happening in the market right now (state), tell me what to do.
        try {
            logger.info("[MYALGO] The state of the order book is:\n" + Util.orderBookToString(state));

            // Check total orders limit
            if (state.getChildOrders().size() > MAX_TOTAL_ORDERS) {
                logger.info("[MYALGO] Exceeded max total orders");
                return NoAction.NoAction;
            }

            // Validate market data
            final AskLevel askFarTouch = state.getAskAt(0);
            final BidLevel bidNearTouch = state.getBidAt(0);
            if (askFarTouch == null || bidNearTouch == null) {
                logger.warn("[MYALGO] Invalid market data");
                return NoAction.NoAction;
            }

            // Calculate mid price
            double midPrice = (askFarTouch.price + bidNearTouch.price) / 2.0;
            List<ChildOrder> activeOrders = state.getActiveChildOrders();

            // Check active buy orders
            List<ChildOrder> activeBuyOrders = activeOrders.stream()
                    .filter(order -> order.getSide() == Side.BUY)
                    .collect(Collectors.toList());

            if (!activeBuyOrders.isEmpty()) {
                // Check for orders that need cancellation (too far from mid price)
                for (ChildOrder buyOrder : activeBuyOrders) {
                    double priceDistance = Math.abs(buyOrder.getPrice() - midPrice) / midPrice;
                    if (priceDistance > MAX_PRICE_DISTANCE_PERCENT) {
                        logger.info("[MYALGO] Cancelling order too far from mid price: {}",
                                buyOrder.getOrderId());
                        return new CancelChildOrder(buyOrder);
                    }

                    // Check for filled buy orders that we can sell
                    if (buyOrder.getFilledQuantity() == buyOrder.getQuantity()) {
                        long sellPrice = (long) (buyOrder.getPrice() * (1 + PROFIT_THRESHOLD));
                        if (bidNearTouch.price >= sellPrice) {
                            logger.info("[MYALGO] Creating sell order for filled buy: {}",
                                    buyOrder.getOrderId());
                            return new CreateChildOrder(Side.SELL,
                                    buyOrder.getQuantity(),
                                    sellPrice);
                        }
                    }
                }
            } else {
                // If we don't have any active buy orders - check if we can create one
                long buyPrice = bidNearTouch.price + 1;
                double priceDistance = Math.abs(buyPrice - midPrice) / midPrice;

                if (priceDistance <= MAX_PRICE_DISTANCE_PERCENT) {
                    logger.info("[MYALGO] Creating new buy order at price: {}", buyPrice);
                    return new CreateChildOrder(Side.BUY,
                            Math.min(100, askFarTouch.quantity),
                            buyPrice);
                }
            }

            logger.info("[MYALGO] No action needed");
            return NoAction.NoAction;
        } catch (Exception e) {
            logger.error("[MYALGO] An error occurred during evaluation: ", e);
            return NoAction.NoAction; // Fallback to no action on error
        }
    }
}