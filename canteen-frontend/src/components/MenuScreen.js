import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import './MenuScreen.css';

function MenuScreen({ customerType, customerData, cart, onAddToCart, onRemoveFromCart, onProceedToSummary, onBack }) {
  const [menuItems, setMenuItems] = useState([]);
  const [filteredItems, setFilteredItems] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
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
      setFilteredItems(menu);
      setError('');
    } catch (err) {
      setError('Failed to load menu items. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Filter items based on search query
  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredItems(menuItems);
    } else {
      const query = searchQuery.toLowerCase();
      const filtered = menuItems.filter(item =>
        item.itemName.toLowerCase().includes(query)
      );
      setFilteredItems(filtered);
    }
  }, [searchQuery, menuItems]);

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
        <div className="header-center">
          <h1>Select Your Items</h1>
          {customerType === 'EMPLOYEE' && customerData.employeeName && (
            <p className="customer-name">
              👤 {customerData.employeeName}
              {customerData.department && ` • ${customerData.department}`}
              {customerData.role && ` • ${customerData.role}`}
            </p>
          )}
          {customerType === 'OUTSIDER' && customerData.outsiderName && (
            <p className="customer-name">👤 {customerData.outsiderName}</p>
          )}
          {customerType === 'GUEST' && (
            <p className="customer-name">
              👥 Guest of {customerData.hostEmployeeName || customerData.hostEmpId}
              {customerData.teamName && ` • ${customerData.teamName}`}
              {customerData.guestCount && ` • ${customerData.guestCount} guests`}
            </p>
          )}
        </div>
        <div className="customer-badge">{customerType}</div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="menu-content">
        <div className="menu-items">
          <div className="menu-items-header">
            <h2>Menu Items</h2>
            <div className="search-bar">
              <input
                type="text"
                placeholder="🔍 Search items..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="search-input"
              />
              {searchQuery && (
                <button
                  className="clear-search"
                  onClick={() => setSearchQuery('')}
                  title="Clear search"
                >
                  ✕
                </button>
              )}
            </div>
          </div>
          {loading ? (
            <div className="loading">Loading items...</div>
          ) : filteredItems.length === 0 ? (
            <div className="no-items">
              {searchQuery ? `No items found matching "${searchQuery}"` : 'No items available'}
            </div>
          ) : (
            <div className="items-grid">
              {filteredItems.map(item => {
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