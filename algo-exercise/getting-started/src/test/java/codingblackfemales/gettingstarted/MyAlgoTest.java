package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.order.Side;
import org.junit.Test;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test is designed to check your algo behavior in isolation of the order book.
 *
 * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
 *
 * When you are comfortable your algo does what you expect, then you can move on to creating the MyAlgoBackTest.
 */

public class MyAlgoTest extends AbstractAlgoTest {


    @Override
    public AlgoLogic createAlgoLogic() {
        return new MarketMakingAlgo(); // Adds your custom algo logic
    }

    @Before
    public void setUp() {
        container.getState().getChildOrders().clear(); // Clear orders before each test
    }

    @Test
    public void testInitialBuyOrderCreation() throws Exception {
        send(createTick());

        // Should create one buy order when there are no active orders
        List<ChildOrder> activeOrders = container.getState().getActiveChildOrders();
        assertFalse("Expected at least one order to be created", activeOrders.isEmpty()); // checks that the activeOrders list is not empty. I am making sure I have at least one order in my list

        ChildOrder firstOrder = activeOrders.get(0);
        assertEquals("First order should be a buy order", Side.BUY, firstOrder.getSide()); // checks that the value of the first order is equal to a buy side order
        assertTrue("Buy price should be above best bid",
                firstOrder.getPrice() > container.getState().getBidAt(0).price); // checks that my order's price is higher than the best bid price
    }



    @Test
    public void testOrderCreationWithinPriceThreshold() throws Exception {
        send(createTick());
        if (!container.getState().getActiveChildOrders().isEmpty()) { // Only run these checks if we have any orders at all i.e only if active orders is not empty
            ChildOrder createdOrder = container.getState().getActiveChildOrders().get(0); // get the first order from our list of active child orders and check it
            assertEquals("Expected a BUY order", Side.BUY, createdOrder.getSide()); // it should be a buy order
            assertTrue("Expected order price to be just above best bid",
                    createdOrder.getPrice() > container.getState().getBidAt(0).price); // Did we place our order above the best bid?
        }
    }

    @Test
    public void testMaxTotalOrdersLimit() throws Exception {
        // Create more than MAX_TOTAL_ORDERS orders
        for (int i = 0; i < 11; i++) {
            send(createTick());
        }

        assertTrue("Total orders should not exceed MAX_TOTAL_ORDERS",
                container.getState().getChildOrders().size() <= 10); // We get all orders (active and filled), count how many we have and then checks if the count is less than or equal to 10. If we have less than 10, the test passes.
    }


    @Test
    public void testOrderCancellationOutsidePriceThreshold() throws Exception {
        // First create an order
        send(createTick());
        assertFalse("Should have created initial order",
                container.getState().getActiveChildOrders().isEmpty());

        // Then send tick that would make the order too far from mid price
        send(createTickWithLargeSpread()); // with this tick, the mid-price will be larger than our threshold

        assertTrue("Order should be cancelled when too far from mid price",
                container.getState().getActiveChildOrders().isEmpty());
    }



    @Test
    public void testOrderCancellationDueToExcessiveDistance() throws Exception {
        send(createTick());
        int initialOrderCount = container.getState().getActiveChildOrders().size();
        send(createTickThatShouldTriggerCancellation());
        int finalOrderCount = container.getState().getActiveChildOrders().size();
        assertTrue("Expected at least one order to be cancelled", finalOrderCount < initialOrderCount); // Final number of orders should be less than initial number
    }

    @Test
    public void testInvalidMarketData() throws Exception {
        send(createTickWithInvalidData());
        int orderCount = container.getState().getActiveChildOrders().size();
        assertEquals("Expected no orders to be created with invalid market data", 0, orderCount);
    }

    @Test
    public void testCancelOrderTooFarFromMidPrice() throws Exception {
        // First create a normal market condition to get an order created
        send(createTick());

        // Verify we have an initial buy order
        assertFalse("Should have created initial order",
                container.getState().getActiveChildOrders().isEmpty()); // our orders shouldn't be empty

        // Now send market data that would make the existing order too far from mid-price
        send(createTickThatShouldTriggerCancellation());

        // Giving the algo a chance to process
        Thread.sleep(100);

        // I want to verify that the order was cancelled
        assertTrue("Order should be cancelled when too far from mid price",
                container.getState().getActiveChildOrders().isEmpty());
    }


    @Test
    public void testCreateNewBuyOrder() throws Exception {
        send(createTick());
        container.getState().getActiveChildOrders().clear();

        send(createTickNearMidPrice());
        int activeOrderCount = container.getState().getActiveChildOrders().size();

        assertTrue("Expected at least one buy order to be created", activeOrderCount > 0);

        ChildOrder newBuyOrder = container.getState().getActiveChildOrders().get(0);
        assertEquals("Expected new order to be a BUY order", Side.BUY, newBuyOrder.getSide());
    }

    @Test
    public void testNoActionNeeded() throws Exception {
        send(createTickNearMidPrice());
        ChildOrder orderNearMidPrice = mock(ChildOrder.class);

        when(orderNearMidPrice.getSide()).thenReturn(Side.BUY);
        when(orderNearMidPrice.getPrice()).thenReturn(container.getState().getBidAt(0).price + 1);
        container.getState().getActiveChildOrders().add(orderNearMidPrice);

        send(createTick());
        assertTrue("Expected no action needed",
                container.getState().getActiveChildOrders().size() == 1);
    }


}
