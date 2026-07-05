import React, { useState } from 'react';
import { api } from '../services/api';
import './OrderSummary.css';

function OrderSummary({ customerType, customerData, cart, currentMeal, onConfirm, onBack }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const getTotalAmount = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const handleConfirmOrder = async () => {
    setLoading(true);
    setError('');

    try {
      const orderData = {
        customerType: customerType,
        items: cart.map(item => ({
          menuId: item.id,
          quantity: item.quantity
        }))
      };

      // Add customer-specific data
      if (customerType === 'EMPLOYEE') {
        orderData.empId = customerData.empId;
      } else if (customerType === 'OUTSIDER') {
        orderData.outsiderName = customerData.outsiderName;
      } else if (customerType === 'GUEST') {
        orderData.hostEmpId = customerData.hostEmpId;
        orderData.purpose = customerData.purpose;
        orderData.guestCount = customerData.guestCount;
        orderData.companyEmployeeCount = customerData.companyEmployeeCount;
      }

      await api.createOrder(orderData);
      onConfirm();
    } catch (err) {
      setError(err.message || 'Failed to place order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="order-summary">
      <div className="summary-container">
        <div className="summary-header">
          <button className="back-button" onClick={onBack} disabled={loading}>
            ← Back
          </button>
          <h1>Order Summary</h1>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="summary-content">
          <div className="customer-info-section">
            <h2>Customer Information</h2>
            <div className="info-card">
              <div className="info-row">
                <span className="info-label">Type:</span>
                <span className="info-value">{customerType}</span>
              </div>
              {customerType === 'EMPLOYEE' && (
                <>
                  {customerData.employeeName && (
                    <div className="info-row">
                      <span className="info-label">Name:</span>
                      <span className="info-value">{customerData.employeeName}</span>
                    </div>
                  )}
                  {customerData.department && (
                    <div className="info-row">
                      <span className="info-label">Department:</span>
                      <span className="info-value">{customerData.department}</span>
                    </div>
                  )}
                  {customerData.role && (
                    <div className="info-row">
                      <span className="info-label">Role:</span>
                      <span className="info-value">{customerData.role}</span>
                    </div>
                  )}
                </>
              )}
              {customerType === 'OUTSIDER' && (
                <div className="info-row">
                  <span className="info-label">Name:</span>
                  <span className="info-value">{customerData.outsiderName}</span>
                </div>
              )}
              {customerType === 'GUEST' && (
                <>
                  {customerData.hostEmployeeName && (
                    <div className="info-row">
                      <span className="info-label">Host Name:</span>
                      <span className="info-value">{customerData.hostEmployeeName}</span>
                    </div>
                  )}
                  {customerData.hostDepartment && (
                    <div className="info-row">
                      <span className="info-label">Host Department:</span>
                      <span className="info-value">{customerData.hostDepartment}</span>
                    </div>
                  )}
                  {customerData.purpose && (
                    <div className="info-row">
                      <span className="info-label">Purpose:</span>
                      <span className="info-value">{customerData.purpose}</span>
                    </div>
                  )}
                  <div className="info-row">
                    <span className="info-label">Guest Count:</span>
                    <span className="info-value">{customerData.guestCount}</span>
                  </div>
                  {customerData.companyEmployeeCount !== undefined && (
                    <div className="info-row">
                      <span className="info-label">Company Employees:</span>
                      <span className="info-value">{customerData.companyEmployeeCount}</span>
                    </div>
                  )}
                </>
              )}
              <div className="info-row">
                <span className="info-label">Meal:</span>
                <span className="info-value">{currentMeal?.type}</span>
              </div>
            </div>
          </div>

          <div className="order-items-section">
            <h2>Order Items</h2>
            <div className="items-list">
              {cart.map(item => (
                <div key={item.id} className="summary-item">
                  <div className="item-details">
                    <span className="item-name">{item.itemName}</span>
                    <span className="item-quantity">Qty: {item.quantity}</span>
                  </div>
                  <div className="item-pricing">
                    <span className="item-unit-price">₹{item.price.toFixed(2)} each</span>
                    <span className="item-total-price">
                      ₹{(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                </div>
              ))}
            </div>

            <div className="summary-total">
              <div className="total-row">
                <span className="total-label">Subtotal:</span>
                <span className="total-value">₹{getTotalAmount().toFixed(2)}</span>
              </div>
              <div className="total-row grand-total">
                <span className="total-label">Total Amount:</span>
                <span className="total-value">₹{getTotalAmount().toFixed(2)}</span>
              </div>
            </div>
          </div>

          <div className="action-buttons">
            <button
              className="confirm-button"
              onClick={handleConfirmOrder}
              disabled={loading}
            >
              {loading ? 'Placing Order...' : 'Confirm Order ✓'}
            </button>
          </div>

          {customerType === 'EMPLOYEE' && (
            <div className="payment-note">
              <p>💡 This amount will be deducted from your salary at the end of the month.</p>
            </div>
          )}
          {customerType === 'OUTSIDER' && (
            <div className="payment-note">
              <p>💳 Please proceed to payment counter after confirmation.</p>
            </div>
          )}
          {customerType === 'GUEST' && (
            <div className="payment-note">
              <p>👥 This order will be charged to host employee: {customerData.hostEmployeeName || customerData.hostEmpId}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default OrderSummary;