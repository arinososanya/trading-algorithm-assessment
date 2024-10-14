package codingblackfemales.algo;

import codingblackfemales.action.Action;
import codingblackfemales.sotw.SimpleAlgoState;

public interface AlgoLogic {
    Action evaluate(final SimpleAlgoState state); // state is the parameter of the evaluate method
}
