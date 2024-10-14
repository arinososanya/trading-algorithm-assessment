package codingblackfemales.container;

import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sequencer.net.Consumer;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.SimpleAlgoStateImpl;
import org.agrona.DirectBuffer;

public class AlgoContainer implements Consumer {

    private final MarketDataService marketDataService;
    private final OrderService orderService;
    private final RunTrigger runTrigger;
    private final Actioner actioner;

    private AlgoLogic logic;

    private final SimpleAlgoState state; // This is a that is passed in from the container. The state object gives you access to two sets of information: 1) A current view on the market data 2) The current view of the child orders you've created, and whether they are filled or cancelled etc.

    public AlgoContainer(final MarketDataService marketDataService,
                         final OrderService orderService,
                         final RunTrigger runTrigger,
                         final Actioner actioner) {
        this.marketDataService = marketDataService;
        this.orderService = orderService;
        this.runTrigger = runTrigger;
        this.actioner = actioner;
        this.state = new SimpleAlgoStateImpl(marketDataService, orderService);
    }

    public MarketDataService getMarketDataService(){
        return marketDataService;
    }

    public OrderService getOrderService(){
        return orderService;
    }

    public void setLogic(AlgoLogic logic){
        this.logic = logic;
    }

    @Override
    public void onMessage(DirectBuffer buffer){
        if(runTrigger.shouldRun()){
            runAlgoLogic();

        }else {
            //do nothing...
        }
    }

    private void runAlgoLogic(){
        final var action = logic.evaluate(state); // what does this line mean?

        runTrigger.hasRun();

        if(action !=null && (!action.equals(NoAction.NoAction))){
            actioner.processAction(action);
        }
    }

    public SimpleAlgoState getState() {
        return state;
    }
}
