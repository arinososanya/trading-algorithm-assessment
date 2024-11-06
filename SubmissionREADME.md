# Algorithmic Trading Exercise

## Project Overview
This project implements two trading algorithms and a market depth visualisation interface.

## Repository Structure
- `src/main/java/codingblackfemales/gettingstarted/MyAlgoLogicNew.java`: Basic trading algorithm
- `src/main/java/codingblackfemales/gettingstarted/ProfitableAlgoLogic.java`: Advanced trading algorithm
- `ui-front-end/`: Market depth visualisation interface

## Trading Algorithms

### Basic Algorithm (MyAlgoLogicNew)
A market-making algorithm that manages orders based on market conditions and price thresholds. Features:
- Cancels orders beyond 5% from mid-price
- Maintains maximum 3 active orders
- Targets 2% profit threshold
- Adapts order quantity based on market liquidity

### Profitable Algorithm (ProfitableAlgoLogic)
A trading algorithm that aims to benefit from upward price movements while managing risk. Features:
- Limits to 2 maximum active orders
- Implements 5-tick stop-loss
- Dynamic sell price adjustment
- Pair-based order tracking

## Market Depth UI
A React-based visualisation showing market depth data including:
- Bid and ask levels
- Price and quantity information
- Real-time updates
- 
<img width="941" alt="Arin_Market_Depth_UI" src="https://github.com/user-attachments/assets/73f8dcfc-3760-4f5c-9578-0331d2792d05">




## How to Run
1. Clone the repository
2. Build the project using Maven
3. Run the algorithms
4. For UI: Navigate to ui-front-end and run `npm install` followed by `npm run dev`

## Testing
The project includes two test suites:

### MyAlgoTest
This test suite was designed specifically for the basic algorithm (MyAlgoLogicNew) and verifies its core functionality. While all tests pass with MyAlgoLogicNew, some tests may fail with ProfitableAlgoLogic as it implements a different trading strategy.

### MyAlgoBackTest
A more general backtesting suite that successfully validates both algorithms' behaviour in realistic market conditions. Both MyAlgoLogicNew and ProfitableAlgoLogic pass these tests, demonstrating their operational reliability.




