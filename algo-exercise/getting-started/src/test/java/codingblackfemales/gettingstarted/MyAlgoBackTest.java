package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
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
        return new MyAlgoLogic();
    } // This createAlgoLogic() method is where you specify which algorithm logic should be used in your tests.

    @Test
    public void testExampleBackTest() throws Exception {
        // Scenario 1: Initial Market state
        send(createMarketTick1());
        var state = container.getState();
        assertEquals("Should create 3 initial orders", 3, state.getChildOrders().size());

        // Scenario 2: Market moves towards us (I'm seeing changes in market conditions that becomes more favorable to my current position or the orders I've placed.)
        send(createMarketTick2());
        state = container.getState();
        long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).orElse(0L);
        assertEquals("Should have filled quantity up to algorithm's limit", 100, filledQuantity);

        //Scenario 3: Market moves away, algo should cancel some orders
        send(createMarketTick3());
        state = container.getState();

        List<ChildOrder> allOrders = state.getChildOrders();
        List<ChildOrder> activeOrders = state.getActiveChildOrders();

        long cancelledOrders = allOrders.size() - activeOrders.size();

        assertTrue("Should have cancelled at least one order", cancelledOrders > 0);


        //Scenario 4: Does my algo create new orders in response to a (favourable) change in market conditions?
        int activeOrdersBefore = state.getActiveChildOrders().size(); // how many active orders did the algo have before sending the new market data tick 4?
        send(createMarketTick4());
        state = container.getState();
        int activeOrdersAfter = state.getActiveChildOrders().size();
        assertTrue("Should have created new orders", activeOrdersAfter > activeOrdersBefore); // Has the number of active orders increased?


        // Anything to do with buying and selling

        // Can I buy?

        // Can I sell?

        // If I buy and sell, can I come out with a profit?

        // Trying to fulfil orders

        // I want to adjust the printing of the Order Book so that it prints individual orders with their IDs.
    }

}
