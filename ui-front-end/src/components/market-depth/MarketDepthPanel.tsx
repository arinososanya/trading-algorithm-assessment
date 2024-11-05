import React, { useMemo } from 'react';
import { PriceComponent } from './PriceComponent';
import { QuantityComponent } from './QuantityComponent';

interface MarketDepthRow {
  level: number;
  bidQuantity: number;
  bid: number;
  offer: number;
  offerQuantity: number;
  symbolLevel: string;
}

interface MarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MarketDepthPanel: React.FC<MarketDepthPanelProps> = ({ data }) => {
  const maxQuantity = useMemo(() => {
    return Math.max(...data.flatMap(row => [row.bidQuantity, row.offerQuantity]));
  }, [data]);
  return (
    <table className="w-full border-collapse">
      <thead>
        <tr className="border-b">
          <th rowSpan={2} className="p-2 text-left" style={{ width: '60px' }}>Level</th>
          <th colSpan={2} className="text-center p-2">Bid</th>
          <th colSpan={2} className="text-center p-2">Ask</th>
        </tr>
        <tr className="border-b">
          <th className="p-2 text-center">Quantity</th>
          <th className="p-2 text-center" style={{ minWidth: '100px' }}>Price</th>
          <th className="p-2 text-center" style={{ minWidth: '100px' }}>Price</th>
          <th className="p-2 text-center">Quantity</th>
        </tr>
      </thead>
      <tbody>
        {data.map((row, index) => (
          <tr key={row.symbolLevel || index}>
            <td className="p-2 text-center">{row.level}</td>
            <QuantityComponent 
              quantity={row.bidQuantity} 
              maxQuantity={maxQuantity} 
              type="bid" 
            />
            <PriceComponent 
              price={row.bid} 
              previousPrice={index > 0 ? data[index - 1].bid : undefined} 
            />
            <PriceComponent 
              price={row.offer} 
              previousPrice={index > 0 ? data[index - 1].offer : undefined} 
            />
            <QuantityComponent 
              quantity={row.offerQuantity} 
              maxQuantity={maxQuantity} 
              type="ask" 
            />
          </tr>
        ))}
      </tbody>
    </table>
  );
};