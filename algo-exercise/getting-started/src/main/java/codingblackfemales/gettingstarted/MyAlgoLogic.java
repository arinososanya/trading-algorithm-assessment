package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic { // implementing the AlgoLogic interface. This class only contains abstract methods

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) { // State is an instance of SimpleAlgoState i.e it's an object of type SimpleAlgoState, that's passed to the evaluate method. The evaluate method returns an Action, which can be a CancelChildOrder, CreateChildOrder, or NoAction.

        var orderBookAsString = Util.orderBookToString(state); // checks the current state of the order book and logs it

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        var totalOrderCount = state.getChildOrders().size(); // get the total number of child orders in the current state

        //make sure we have an exit condition... this is a safety check for if there are more than 20 child orders, then the algo does nothing
        if (totalOrderCount > 20) {
            return NoAction.NoAction;
        }

        final var activeOrders = state.getActiveChildOrders(); // getting all the current live/active child orders
        if (activeOrders.size() > 0) {
            final var option = activeOrders.stream().findFirst();

            if (option.isPresent()) { // if there are active orders, it attempts to cancel the first one. If there's no first order (which shouldn't happen if size>0), it does nothing
                var childOrder = option.get();
                logger.info("[ADDCANCELALGO] Cancelling order:" + childOrder);
                return new CancelChildOrder(childOrder);
            }
            else{
                return NoAction.NoAction;
            }
        } else {
            BidLevel level = state.getBidAt(0);
            final long price = level.price;
            final long quantity = level.quantity;
            logger.info("[ADDCANCELALGO] Adding order for" + quantity + "@" + price);
            return new CreateChildOrder(Side.BUY, quantity, price);
        }
        }


    }

/********
 *
 * Add your logic here....
 *
 */


 /*
        Mine: Get all active orders
      var activeOrders = state.getActiveChildOrders();

        // If I have active orders, cancel the oldest one. Oldest one = > 10 ticks
        if(!activeOrders.isEmpty()){

        }

        // If the no of active orders is below the maximum limit, (MAX_ORDERS), then my algo will consider adding a new buy order just above the best bid price


        // If we didn't cancel or create an order, do nothing
        return NoAction.NoAction;*/

        /* To make profit: Sell the asset for more than I bought it for. The smaller the spread,
        the quicker you might be able to make a profit, but potentially a smaller one.
        create a buy and sell threshold in your logic with your
        algo only buying when your price is at that threshold or below it
        and selling when it reaches your sell threshold or exceeds it.
        *
        * */