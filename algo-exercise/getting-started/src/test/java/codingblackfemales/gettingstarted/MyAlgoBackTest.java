package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
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
public class MyAlgoBackTest extends AbstractAlgoBackTest { // I can implement the MyAlgoBackTest, which sets up the pipes needed for my test environment

    @Override
    public AlgoLogic createAlgoLogic() { // instantiating the MyAlgoLogic class
        return new MyAlgoLogicNew();
    } // This createAlgoLogic() method is where you specify which algorithm logic should be used in your tests.

    @Test
    public void testMyAlgoBackTest() throws Exception {
        // Scenario 1: Initial market state
        send(createMarketTick1());
        var state = container.getState();
        assertTrue("Should create initial orders", !state.getActiveChildOrders().isEmpty());

        // Scenario 2: Market moves
        send(createMarketTick3());
        state = container.getState();

        // Check for order cancellations
        assertTrue("Should have cancelled some orders",
                state.getChildOrders().size() > state.getActiveChildOrders().size());

        // Scenario 3: Further market movement
        send(createMarketTick5());
        state = container.getState();

        // Check for buy and sell orders
        long buyOrders = state.getActiveChildOrders().stream()
                .filter(order -> order.getSide() == Side.BUY)
                .count();
        long sellOrders = state.getActiveChildOrders().stream()
                .filter(order -> order.getSide() == Side.SELL)
                .count();

        assertTrue("Should have buy orders", buyOrders > 0);
        assertTrue("Should have sell orders", sellOrders > 0);

        // Check for filled orders
        long filledQuantity = state.getChildOrders().stream()
                .map(ChildOrder::getFilledQuantity)
                .reduce(Long::sum)
                .orElse(0L);
        assertTrue("Should have filled orders", filledQuantity > 0);

        // Log final state
        System.out.println("Final state:");
        System.out.println("Active buy orders: " + buyOrders);
        System.out.println("Active sell orders: " + sellOrders);
        System.out.println("Total filled quantity: " + filledQuantity);
    }

}

