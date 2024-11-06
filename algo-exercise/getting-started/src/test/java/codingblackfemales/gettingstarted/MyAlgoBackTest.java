package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the spread (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */



public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {

        return new MyAlgoLogicNew(); // You can replace this with ProfitableAlgoLogic
    }

    @Test
    public void testMyAlgoBackTest() throws Exception {
        // Scenario 1: Initial market state - should place buy order
        send(createMarketTick1());
        var state = container.getState();

        // Verify initial buy order
        assertTrue("Should create initial buy order", !state.getActiveChildOrders().isEmpty());
        assertTrue("First order should be a buy",
                state.getActiveChildOrders().get(0).getSide() == Side.BUY);

        // Log initial state
        logOrderState("After initial market", state);

        // Scenario 2: Market moves up - should get fill and place sell
        send(createMarketTick2());
        state = container.getState();

        // Log state after market move
        logOrderState("After market move up", state);

        // Scenario 3: Final market state
        send(createMarketTick3());
        state = container.getState();

        // Log final state and trading results
        logOrderState("After final market", state);
        verifyTradingResults(state);
    }

    private void logOrderState(String message, SimpleAlgoState state) {
        System.out.println("\n" + message);
        System.out.println("Active orders: " + state.getActiveChildOrders().size());
        System.out.println("Total orders: " + state.getChildOrders().size());
        System.out.println("Filled quantity: " + getFilledQuantity(state));

        // Print each active order with correct formatting
        state.getActiveChildOrders().forEach(order ->
                System.out.println(String.format("Order %d: %s %d qty: %d filled: %d",
                        order.getOrderId(),
                        order.getSide(),
                        order.getPrice(),
                        order.getQuantity(),
                        order.getFilledQuantity())));
    }

    private long getFilledQuantity(SimpleAlgoState state) {
        return state.getChildOrders().stream()
                .map(ChildOrder::getFilledQuantity)
                .reduce(Long::sum)
                .orElse(0L);
    }

    private void verifyTradingResults(SimpleAlgoState state) {
        long totalPnl = 0;
        System.out.println("\nTrading Results:");

        // Calculate P&L from filled orders
        for (ChildOrder order : state.getChildOrders()) {
            if (order.getFilledQuantity() > 0) {
                System.out.println(String.format("%s order %d: price %d qty %d",
                        order.getSide(),
                        order.getOrderId(),
                        order.getPrice(),
                        order.getFilledQuantity()));

                long impact = order.getSide() == Side.BUY ? -1 : 1;
                totalPnl += impact * order.getPrice() * order.getFilledQuantity();
            }
        }

        System.out.println(String.format("Total P&L: %d", totalPnl));
        System.out.println("Total Trades: " + state.getChildOrders().size());
        System.out.println("Filled Trades: " +
                state.getChildOrders().stream()
                        .filter(o -> o.getFilledQuantity() > 0)
                        .count());
    }
}
