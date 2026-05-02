import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import './MenuScreen.css';

function MenuScreen({ customerType, cart, onAddToCart, onRemoveFromCart, onProceedToSummary, onBack }) {
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAllMenuItems();
  }, []);

  const loadAllMenuItems = async () => {
    try {
      setLoading(true);
      const menu = await api.getAllMenuItems();
      setMenuItems(menu);
      setError('');
    } catch (err) {
      setError('Failed to load menu items. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getTotalAmount = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const getTotalItems = () => {
    return cart.reduce((total, item) => total + item.quantity, 0);
  };

  const handleProceed = () => {
    if (cart.length === 0) {
      setError('Please add at least one item to your cart');
      return;
    }
    onProceedToSummary(null); // No meal restriction
  };

  if (loading) {
    return (
      <div className="menu-screen">
        <div className="loading">Loading menu...</div>
      </div>
    );
  }

  return (
    <div className="menu-screen">
      <div className="menu-header">
        <button className="back-button" onClick={onBack}>← Back</button>
        <h1>Select Your Items</h1>
        <div className="customer-badge">{customerType}</div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="menu-content">
        <div className="menu-items">
          <h2>Menu Items</h2>
          {loading ? (
            <div className="loading">Loading items...</div>
          ) : menuItems.length === 0 ? (
            <div className="no-items">No items available for this meal</div>
          ) : (
            <div className="items-grid">
              {menuItems.map(item => {
                const cartItem = cart.find(c => c.id === item.id);
                const quantity = cartItem ? cartItem.quantity : 0;

                return (
                  <div key={item.id} className="menu-item-card">
                    <div className="item-info">
                      <h3>{item.itemName}</h3>
                      <p className="item-price">₹{item.price.toFixed(2)}</p>
                    </div>
                    <div className="item-actions">
                      {quantity === 0 ? (
                        <button
                          className="add-button"
                          onClick={() => onAddToCart(item)}
                        >
                          Add +
                        </button>
                      ) : (
                        <div className="quantity-controls">
                          <button
                            className="qty-button"
                            onClick={() => onRemoveFromCart(item.id)}
                          >
                            −
                          </button>
                          <span className="quantity">{quantity}</span>
                          <button
                            className="qty-button"
                            onClick={() => onAddToCart(item)}
                          >
                            +
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="cart-sidebar">
          <h2>Your Cart</h2>
          {cart.length === 0 ? (
            <div className="empty-cart">
              <p>🛒</p>
              <p>Your cart is empty</p>
            </div>
          ) : (
            <>
              <div className="cart-items">
                {cart.map(item => (
                  <div key={item.id} className="cart-item">
                    <div className="cart-item-info">
                      <span className="cart-item-name">{item.itemName}</span>
                      <span className="cart-item-qty">x{item.quantity}</span>
                    </div>
                    <span className="cart-item-price">
                      ₹{(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
              <div className="cart-summary">
                <div className="cart-total">
                  <span>Total ({getTotalItems()} items)</span>
                  <span className="total-amount">₹{getTotalAmount().toFixed(2)}</span>
                </div>
                <button className="proceed-button" onClick={handleProceed}>
                  Proceed to Checkout →
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default MenuScreen;