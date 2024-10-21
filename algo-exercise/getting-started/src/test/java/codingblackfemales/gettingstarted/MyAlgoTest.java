package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.order.Side;
import org.agrona.DirectBuffer;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


// MY NOTE:
/**
 * This test is designed to check your algo behavior in isolation of the order book.
 *
 * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
 *
 * When you are comfortable your algo does what you expect, then you can move on to creating the MyAlgoBackTest.
 *
 */

// Testing the plumbing

public class MyAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //testing the data from the order book (AbstractAlgoTest) ... create a sample market data tick....
        send(createTick());

        //simple assert to check we had 3 orders created
        int expectedOrderCount = 3;
        assertEquals("Expected 3 orders to be created", expectedOrderCount, container.getState().getChildOrders().size());
    }

    // Test that order was cancelled
    @Test
    public void testCancellation() throws Exception {
        // Check if any orders were cancelled
        int totalOrders = container.getState().getChildOrders().size();
        int activeOrders = container.getState().getActiveChildOrders().size();

// Assert that total orders is greater than active orders
        assertTrue("Expected some orders to be cancelled", totalOrders > activeOrders);
    }

    // Test maximum order limit
    @Test
    public void testMaxOrderLimit() throws Exception {
        for (int i = 0; i < 5; i++) { // Sending more ticks than MAX_ORDERS
            send(createTick());
        }

        int activeOrderCount = container.getState().getActiveChildOrders().size();
        assertTrue("Expected orders to not exceed MAX_ORDERS",
                activeOrderCount <= 3); // Assuming MAX_ORDERS is 3
    }

    // Test no action when spread is too small
    @Test
    public void testNoActionOnSmallSpread() throws Exception{
        send(createTick());
        assertThat("Expected no orders to be created on small spread", container.getState().getChildOrders().size(),is(equalTo(0)));
    }

    // Test for negative spread - we don't want that!
    @Test
    public void testNoActionOnNegativeSpread() throws Exception {
        send(createTick()); // Negative spread scenario

        assertThat("Expected no action on negative spread",
                container.getState().getChildOrders().size(),
                is(equalTo(0)));
    }

    // Test order creation with sufficient spread
    @Test
    public void testOrderCreationWithSufficientSpread() throws Exception {
        send(createTick()); // Spread of 5, which is greater than MIN_SPREAD

        // Checking if an order was created
        assertThat("Expected an order to be created",
                container.getState().getActiveChildOrders().size(),
                is(equalTo(1)));

        // I also want to check the properties of the created order
        ChildOrder createdOrder = container.getState().getActiveChildOrders().get(0);
        assertThat("Expected a BUY order", createdOrder.getSide(), is(equalTo(Side.BUY)));
        assertThat("Expected order price to be at ask", createdOrder.getPrice(), is(equalTo(105L)));

        // Though I can't assert the exact price, but we can check if it's greater than 0
        assertThat("Expected order price to be positive",
                createdOrder.getPrice() > 0,
                is(true));

        // Optional: Log the actual price for informational purposes
        System.out.println("Created order price: " + createdOrder.getPrice());
    }




    // Test order cancellation based on price distance
        @Test
        public void testOrderCancellationDueToExcessiveDistance() throws Exception {
            // Create initial market data and orders
            send(createTick());

            // Change market data significantly
            send(createTick2());

            // Check if an order was cancelled
            assertThat(container.getState().getChildOrders().size(), is(equalTo(1)));
        }

    // Edge case: Test for if you cancel e.g more Can you cancel something that doesn't exist?

    }


