import React from 'react';

interface QuantityComponentProps {
  quantity: number;
  maxQuantity: number;
  type: 'bid' | 'ask';
}

export const QuantityComponent: React.FC<QuantityComponentProps> = ({ quantity, maxQuantity, type }) => {
  const widthPercentage = (quantity / maxQuantity) * 100;
  
  return (
    <td className="p-2" style={{ 
      width: '200px',
      textAlign: 'center'
    }}>
      <div style={{
        width: '160px',
        height: '32px',
        backgroundColor: '#f0f0f0',
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        margin: '0 auto'
      }}>
        <div style={{
          position: 'absolute',
          top: 0,
          [type === 'bid' ? 'right' : 'left']: 0,
          width: `${widthPercentage}%`,
          height: '100%',
          backgroundColor: type === 'bid' ? '#0066FF' : '#FF0000',
          transition: 'width 0.3s ease-in-out'
        }} />
        <span style={{
          position: 'absolute',
          [type === 'bid' ? 'right' : 'left']: '8px',
          width: '100%',
          textAlign: type === 'bid' ? 'right' : 'left',
          zIndex: 2,
          color: '#000000', // Changed to black
          padding: '0 8px',
          fontWeight: '500' // Making it slightly bold for better readability
        }}>
          {quantity.toLocaleString()}
        </span>
      </div>
    </td>
  );
};


// import React from 'react';

// interface QuantityComponentProps {
//   quantity: number;
//   maxQuantity: number;
//   type: 'bid' | 'ask';
// }

// export const QuantityComponent: React.FC<QuantityComponentProps> = ({ quantity, maxQuantity, type }) => {
//   const percentage = Math.min((quantity / maxQuantity) * 100, 100);
  
//   return (
//     <td className="p-2 relative w-40">
//       <div className="h-8 w-full relative bg-gray-100">
//         <div
//           style={{ width: `${percentage}%` }}
//           className={`h-full ${type === 'bid' ? 'bg-blue-500 float-right' : 'bg-red-500 float-left'}`}
//         />
//         <span className="absolute inset-y-0 flex items-center px-2 z-10 text-sm">
//           {quantity}
//         </span>
//       </div>
//     </td>
//   );
// };