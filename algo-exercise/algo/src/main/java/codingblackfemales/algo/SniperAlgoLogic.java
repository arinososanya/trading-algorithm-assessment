package codingblackfemales.algo;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static codingblackfemales.action.NoAction.NoAction;

public class SniperAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(SniperAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) { // overrides evaluate method found in AlgoLogic class, allowing for customisation

        logger.info("[SNIPERALGO] In Algo Logic...."); // an introduction

        final String book = Util.orderBookToString(state); // 'book' variable contains the current state of the order book and presents it as a string

        logger.info("[SNIPERALGO] Algo Sees Book as:\n" + book); // \n is a line break ; it shows the order book

        final AskLevel farTouch = state.getAskAt(0); // What offers are available at the Asks: AskLevel - what the seller is asking for; we're not looking at what other buyers are bidding at; farTouch - the variable for the best price

        //take as much as we can from the far touch....
        long quantity = farTouch.quantity; // letting the buyer know what is available in the market
        long price = farTouch.price;

        //until we have five child orders....
        if (state.getChildOrders().size() < 5) {
            //then keep creating a new one
            logger.info("[SNIPERALGO] Have:" + state.getChildOrders().size() + " children, want 5, sniping far touch of book with: "
                    + quantity + " @ " + price);
            return new CreateChildOrder(Side.BUY, quantity, price);
        } else {
            logger.info("[SNIPERALGO] Have:" + state.getChildOrders().size() + " children, want 5, done.");
            return NoAction;
        }
    }
}
