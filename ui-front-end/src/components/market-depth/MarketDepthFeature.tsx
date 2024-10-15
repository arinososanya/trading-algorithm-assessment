// import { Placeholder } from "../placeholder";
import { MarketDepthPanel } from "./MarketDepthPanel";
import { MarketDepthRow, useMarketDepthData } from "./useMarketDepthData"; // This component, MarketDepthFeature fetches the market depth data using useMarketDepthData and returns it as a prop.
import { schemas } from "../../data/algo-schemas";

// prettier-ignore
const testData: MarketDepthRow[] = [ // a JavaScript array with some TypeScript syntax
  { symbolLevel:"1230", level: 0, bid: 1000, bidQuantity: 500, offer: 1010, offerQuantity: 700 }, // The bid and ask offer values are the prices
  { symbolLevel:"1231", level: 1, bid: 990, bidQuantity: 700, offer: 1012, offerQuantity: 400 },
  { symbolLevel:"1232", level: 2, bid: 985, bidQuantity: 1200, offer: 1013, offerQuantity: 800 },
  { symbolLevel:"1233", level: 3, bid: 984, bidQuantity: 1300, offer: 1018, offerQuantity: 750 },
  { symbolLevel:"1234", level: 4, bid: 970, bidQuantity: 800, offer: 1021, offerQuantity: 900 },
  { symbolLevel:"1235", level: 5, bid: 969, bidQuantity: 700, offer: 1026, offerQuantity: 1500 },
  { symbolLevel:"1236", level: 6, bid: 950, bidQuantity: 750, offer: 1027, offerQuantity: 1500 },
  { symbolLevel:"1237", level: 7, bid: 945, bidQuantity: 900, offer: 1029, offerQuantity: 2000 },
  { symbolLevel:"1238", level: 8, bid: 943, bidQuantity: 500, offer: 1031, offerQuantity: 500 },
  { symbolLevel:"1239", level: 9, bid: 940, bidQuantity: 200, offer: 1024, offerQuantity: 800 },
];

/**
 * TODO
 */
export const MarketDepthFeature = () => {
  const data = useMarketDepthData(schemas.prices); // this is a hook. It is using the useMarketDepthData hook to fetch the market depth data. This hook is likely a custom hook defined in your project,
  //responsible for making API calls or accessing data from a data source.getting back the data for you from the Market Depth Source - it gets back 10 rows of data from the source and keeps re-rendering. The placeholder would update everytime the data comes in
  return <MarketDepthPanel data={testData} />; // Replace 'Placeholder' with your own React component
};
