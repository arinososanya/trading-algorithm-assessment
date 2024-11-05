package codingblackfemales.algo;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCancelAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(AddCancelAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[ADDCANCELALGO] In Algo Logic....");

        final String book = Util.orderBookToString(state); // converting the current state of the order book to a string and then logging it (line below)

        logger.info("[ADDCANCELALGO] Algo Sees Book as:\n" + book); // logs the current state of the order book

        var totalOrderCount = state.getChildOrders().size(); // get the total number of child orders in the current state

        //make sure we have an exit condition... this is a safety check for if there are more than 20 child orders, then the algo does nothing
        if (totalOrderCount > 20) {
            logger.info("There are over 20 child orders in the order book");
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

// No t