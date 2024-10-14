package codingblackfemales.action;

import codingblackfemales.sequencer.Sequencer;

public interface Action { // The Action interface defines a single method apply that takes a Sequencer as an argument.

    void apply(final Sequencer sequencer);

}
