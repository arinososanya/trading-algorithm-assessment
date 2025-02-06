package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;

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

        return new MarketMakingAlgo(); // initialise our market making algo
    }

    @Test
    public void testMyAlgoBackTest() throws Exception {
        // Scenario 1: Initial market state - should place buy order
        send(createMarketTick1()); // simulate initial market data
        var state = container.getState(); // get current state of my algorithm

        // Verify initial buy order
        assertTrue("Should create initial buy order", !state.getActiveChildOrders().isEmpty()); // Checking that I created an order
        assertTrue("First order should be a buy",
                state.getActiveChildOrders().get(0).getSide() == Side.BUY); // Verifying that it's a buy order

        // Log current state to console
        logOrderState("After initial market", state);

        // Scenario 2: Market moves up - should get filled buy order and place sell
        send(createMarketTick2()); // send new market data
        state = container.getState();

        // Log state after market move
        logOrderState("After market move up", state);

        // Scenario 3: Final market state
        send(createMarketTick3());
        state = container.getState();

        // Log final state and trading results
        logOrderState("After final market", state);
        verifyTradingResults(state); // Calculate and display P&L
    }

    private void logOrderState(String message, SimpleAlgoState state) {
        // Print header
        System.out.println("\n" + message);

        // Printing the summary stats
        System.out.println("Active orders: " + state.getActiveChildOrders().size());
        System.out.println("Total orders: " + state.getChildOrders().size());
        System.out.println("Filled quantity: " + getFilledQuantity(state));

        // Print the details of each active order with correct formatting
        state.getActiveChildOrders().forEach(order ->
                System.out.println(String.format("Order %d: %s %d qty: %d filled: %d",
                        order.getOrderId(), // unique order ID
                        order.getSide(),
                        order.getPrice(),
                        order.getQuantity(),
                        order.getFilledQuantity()))); // How much has executed
    }

    private long getFilledQuantity(SimpleAlgoState state) { //   Calculate total filled quantity across all orders using stream operations
        return state.getChildOrders().stream()
                .map(ChildOrder::getFilledQuantity)  // Extract filled quantity from each order
                .reduce(Long::sum) // Sum all quantities
                .orElse(0L); // Return 0 if no fills
    }

    private void verifyTradingResults(SimpleAlgoState state) {
        long totalPnl = 0;
        System.out.println("\nTrading Results:");

        // Calculate P&L from filled orders by looping through all orders
        for (ChildOrder order : state.getChildOrders()) {
            if (order.getFilledQuantity() > 0) { // only consider executed orders
                System.out.println(String.format("%s order %d: price %d qty %d",
                        order.getSide(),
                        order.getOrderId(),
                        order.getPrice(),
                        order.getFilledQuantity()));

                long impact = order.getSide() == Side.BUY ? -1 : 1; // Buys reduce P&L, sells increase it. Buy order, impact - -1 (I'm spending money). Sell order, impact =1 (I receive money)
                totalPnl += impact * order.getPrice() * order.getFilledQuantity();
            }
        }

        // Print final stats

        System.out.println(String.format("Total P&L: %d", totalPnl));
        System.out.println("Total Trades: " + state.getChildOrders().size());
        System.out.println("Filled Trades: " +
                state.getChildOrders().stream()
                        .filter(o -> o.getFilledQuantity() > 0)
                        .count());
    }
}
