package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

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
public class MyAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //create a sample market data tick....
        send(createTick());

        //simple assert to check we had 3 orders created
        int expectedOrderCount = 3;
        assertEquals("Expected 3 orders to be created", expectedOrderCount, container.getState().getChildOrders().size());
    }

    @Test
    public void testMaxOrderLimit () throws Exception{
        // Send market data multiple times
        for (int i = 0; i < 3; i++){
            send(createTick());
        }

    }

    //Examples of Parameterized Tests

//    @DisplayName(value = "String Exercises")
//    public class AppTest {
//
//        @ParameterizedTest
//        @MethodSource
//        @DisplayName("concatenate() method returns correctly concatenated string")
//        public void concatenate_ReturnsConcatenatedString(String word1, String word2, String word3, String expected) {
//            final String result = App.concatenate(word1, word2, word3);
//
//            assertThat(result, is(equalTo(expected)));
//        }
//
//        static Stream<Arguments> concatenate_ReturnsConcatenatedString() {
//            return Stream.of(
//                    arguments("Red", "Green", "Blue", "RedGreenBlue"),
//                    arguments("one", "two", "three", "onetwothree"),
//                    arguments("QUICK", "BROWN", "FOX", "QUICKBROWNFOX"),
//                    arguments("www.", "google.", "com", "www.google.com"),
//                    arguments("Intro ", "to ", "Java", "Intro to Java"));
//        }


}
