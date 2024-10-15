// Import necessary tools from React and other components I'm using
import React, { useMemo } from 'react';
import { PriceComponent } from './PriceComponent';
import { QuantityComponent } from './QuantityComponent';

// Defining what information each row in our market depth table will contain
interface MarketDepthRow {
  level: number;        // The order book level
  bidQuantity: number;  // How much someone wants to buy
  bid: number;          // The price they want to buy at
  offer: number;        // The price someone wants to sell at
  offerQuantity: number; // How much they want to sell
  symbolLevel: string;  // A unique identifier for each row
}

// Defining what information this component needs to work
interface MarketDepthPanelProps {
  data: MarketDepthRow[];  // An array of market depth rows
}

// Create our main component that will display the market depth panel
export const MarketDepthPanel: React.FC<MarketDepthPanelProps> = ({ data }) => {
  // Calculate the largest quantity in our data
  // We use useMemo to avoid recalculating this unless our data changes
  const maxQuantity = useMemo(() => {
    return Math.max(...data.flatMap(row => [row.bidQuantity, row.offerQuantity]));
  }, [data]);

  // If we don't have any data yet, show a loading message
  if (!data || data.length === 0) {
    return <p>Loading market depth data...</p>;
  }

  // If we have data, create and return our table
  return (
    <table className="w-full border-collapse">
      {/* The header of our table */}
      <thead>
        <tr className="bg-gray-100">
          <th className="p-2">Level</th>
          <th className="p-2">Bid Quantity</th>
          <th className="p-2">Bid Price</th>
          <th className="p-2">Ask Price</th>
          <th className="p-2">Ask Quantity</th>
        </tr>
      </thead>
      {/* The body of our table */}
      <tbody>
        {/* For each row in our data, create a table row */}
        {data.map((row, index) => (
          <tr key={row.symbolLevel} className="border-t">
            {/* Display the level */}
            <td className="p-2 text-center">{row.level}</td>
            {/* Display bid quantity using our QuantityComponent */}
            <QuantityComponent quantity={row.bidQuantity} maxQuantity={maxQuantity} type="bid" />
            {/* Display bid price using our PriceComponent */}
            <PriceComponent price={row.bid} previousPrice={index > 0 ? data[index - 1].bid : undefined} />
            {/* Display ask price using our PriceComponent */}
            <PriceComponent price={row.offer} previousPrice={index > 0 ? data[index - 1].offer : undefined} />
            {/* Display ask quantity using our QuantityComponent */}
            <QuantityComponent quantity={row.offerQuantity} maxQuantity={maxQuantity} type="ask" />
          </tr>
        ))}
      </tbody>
    </table>
  );
};









// // This component, MarketDepthPanel, receives the data prop from MarketDepthFeature and uses it to render the table.

// import React from 'react';
// import { MarketDepthFeature } from './MarketDepthFeature';
// import { MarketDepthRow } from './useMarketDepthData';
// import { PriceComponent } from './PriceComponent';

// interface { PriceComponent } from './PriceComponent';
//  MarketDepthPanelProps {
//     data: MarketDepthRow[];
//     }


// // This component displays the market depth data in a table format
// export const MarketDepthPanel = ({ data }: MarketDepthPanelProps ) => {

// if (!data) {
//     return <p>Loading market depth data...</p>;
//   }

//   // Render the table header
//   return (
//     <table>
//       <thead>
//         <tr>
//           <th>Item Number</th>
//           <th>Bid Quantity</th>
//           <th>Bid Price</th>
//           <th>Ask Quantity</th>
//           <th>Ask Price</th>
//         </tr>
//       </thead>
//       <tbody> {/* // Map over the market depth data to create table rows */}
  
//         {data.map((row) => (
//           <tr key={row.symbolLevel}>
//             <td>{row.level}</td>
//             <td>{row.bidQuantity}</td>
//             <td>{row.bid}</td>
//             <td>{row.offer}</td>
//             <td>{row.offerQuantity}</td>
//           </tr>
//         ))}
//       </tbody>
//     </table>
//   );
// };

// // You could loop the data to have the code go through the loop rather than hard coding the table

// // Export the MarketDepthPanel component for use in other parts of your application
// // export MarketDepthPanel;