package codingblackfemales.sotw;

import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;

import java.util.Collections;
import java.util.List;

public interface SimpleAlgoState { // explore this - contains methods for order book. We access this using the state object in the evaluate method

    public String getSymbol();

    public int getBidLevels(); // helpful to understand how much liquidity is available at different price points
    public int getAskLevels();

    public BidLevel getBidAt(int index);
    public AskLevel getAskAt(int index);

    public List<ChildOrder> getChildOrders();

    public List<ChildOrder> getActiveChildOrders();

    public long getInstrumentId();

    }

