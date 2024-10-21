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
        return new MyAlgoLogicNew();
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //testing the data from the order book (AbstractAlgoTest) ... create a sample market data tick....
        send(createTick());

        //simple assert to check we had 3 orders created
        int expectedOrderCount = 3;
        assertEquals("Expected 3 orders to be created", expectedOrderCount, container.getState().getChildOrders().size());
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


    @Test
    public void testOrderCreationWithinPriceThreshold() throws Exception {
        send(createTick());
        if (!container.getState().getActiveChildOrders().isEmpty()) {
            ChildOrder createdOrder = container.getState().getActiveChildOrders().get(0);
            assertThat("Expected a BUY order", createdOrder.getSide(), is(equalTo(Side.BUY)));
            assertTrue("Expected order price to be just above best bid",
                    createdOrder.getPrice() > container.getState().getBidAt(0).price);
        }
    }

    // Test order cancellation based on price distance
        @Test
        public void testOrderCancellationDueToExcessiveDistance() throws Exception {
            // Create initial market data and orders
            send(createTick());
            int initialOrderCount = container.getState().getActiveChildOrders().size();
            // Change market data significantly
            send(createTickThatShouldTriggerCancellation());
            int finalOrderCount = container.getState().getActiveChildOrders().size();
            assertTrue("Expected at least one order to be cancelled", finalOrderCount < initialOrderCount);
        }

    }


