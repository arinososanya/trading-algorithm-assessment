import React, { useRef, useEffect, useState } from 'react';

interface PriceComponentProps {
  price: number;
  previousPrice?: number;
}

export const PriceComponent: React.FC<PriceComponentProps> = ({ price, previousPrice }) => {
  const lastPrice = useRef(price);
  const [direction, setDirection] = useState<'up' | 'down' | null>(null);

  useEffect(() => {
    if (price !== lastPrice.current) {
      setDirection(price > lastPrice.current ? 'up' : 'down');
      lastPrice.current = price;
    }
  }, [price]);

  return (
    <td style={{ 
      textAlign: 'center',  // Center align the text
      padding: '8px',
      minWidth: '100px'     // Ensure consistent column width
    }} className={`
      ${direction === 'up' ? 'text-green-500' : ''}
      ${direction === 'down' ? 'text-red-500' : ''}
    `}>
      {price.toFixed(2)}
    </td>
  );
};