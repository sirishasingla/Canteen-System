import React, { useEffect, useState } from 'react';
import './SuccessScreen.css';

function SuccessScreen({ onStartOver }) {
  const [countdown, setCountdown] = useState(10);

  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          onStartOver();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [onStartOver]);

  return (
    <div className="success-screen">
      <div className="success-container">
        <div className="success-icon">✓</div>
        <h1 className="success-title">Order Placed Successfully!</h1>
        <p className="success-message">
          Your order has been confirmed and recorded.
        </p>
        <p className="success-submessage">
          Thank you for using our canteen service!
        </p>
        
        <div className="success-actions">
          <button className="new-order-button" onClick={onStartOver}>
            Place New Order
          </button>
          <p className="auto-redirect">
            Automatically redirecting in {countdown} seconds...
          </p>
        </div>
      </div>
    </div>
  );
}

export default SuccessScreen;