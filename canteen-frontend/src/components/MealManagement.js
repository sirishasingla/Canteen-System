import React, { useState, useEffect } from 'react';
import './MealManagement.css';
import { api } from '../services/api';

const MealManagement = ({ onBack }) => {
  const [activeTab, setActiveTab] = useState('meals');
  const [meals, setMeals] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
  const [currentItem, setCurrentItem] = useState(null);

  // Form states
  const [mealForm, setMealForm] = useState({
    id: null,
    type: 'BREAKFAST',
    startTime: '',
    endTime: ''
  });

  const [menuForm, setMenuForm] = useState({
    id: null,
    itemName: '',
    price: '',
    mealId: '',
    isActive: true
  });

  useEffect(() => {
    loadMeals();
    loadMenuItems();
  }, []);

  const loadMeals = async () => {
    try {
      const data = await api.getMeals();
      setMeals(data);
    } catch (err) {
      setError('Failed to load meals');
    }
  };

  const loadMenuItems = async () => {
    try {
      const data = await api.getAllMenuItemsForManagement();
      setMenuItems(data);
    } catch (err) {
      setError('Failed to load menu items');
    }
  };

  // Meal Management Functions
  const handleAddMeal = () => {
    setModalMode('add');
    setMealForm({
      id: null,
      type: 'BREAKFAST',
      startTime: '',
      endTime: ''
    });
    setShowModal(true);
    setError('');
  };

  const handleEditMeal = (meal) => {
    setModalMode('edit');
    setMealForm({
      id: meal.id,
      type: meal.type,
      startTime: meal.startTime,
      endTime: meal.endTime
    });
    setShowModal(true);
    setError('');
  };

  const handleSaveMeal = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (modalMode === 'add') {
        await api.createMeal(mealForm);
        alert('Meal added successfully');
      } else {
        await api.updateMeal(mealForm.id, mealForm);
        alert('Meal updated successfully');
      }
      setShowModal(false);
      await loadMeals();
    } catch (err) {
      setError(err.message || `Failed to ${modalMode} meal`);
    } finally {
      setLoading(false);
    }
  };

  // Menu Item Management Functions
  const handleAddMenuItem = () => {
    setModalMode('add');
    setMenuForm({
      id: null,
      itemName: '',
      price: '',
      mealId: '',
      isActive: true
    });
    setShowModal(true);
    setError('');
  };

  const handleEditMenuItem = (item) => {
    setModalMode('edit');
    setMenuForm({
      id: item.id,
      itemName: item.itemName,
      price: item.price,
      mealId: item.meal?.id || '',
      isActive: item.isActive
    });
    setShowModal(true);
    setError('');
  };

  const handleSaveMenuItem = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = {
        itemName: menuForm.itemName,
        price: parseFloat(menuForm.price),
        mealId: menuForm.mealId ? parseInt(menuForm.mealId) : null,
        isActive: menuForm.isActive
      };

      if (modalMode === 'add') {
        await api.createMenuItem(payload);
        alert('Menu item added successfully');
      } else {
        await api.updateMenuItem(menuForm.id, payload);
        alert('Menu item updated successfully');
      }
      setShowModal(false);
      await loadMenuItems();
    } catch (err) {
      setError(err.message || `Failed to ${modalMode} menu item`);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleMenuItem = async (item) => {
    const action = item.isActive ? 'disable' : 'enable';
    if (!window.confirm(`Are you sure you want to ${action} "${item.itemName}"?`)) {
      return;
    }

    setLoading(true);
    try {
      await api.toggleMenuItem(item.id);
      await loadMenuItems();
      alert(`Menu item ${action}d successfully`);
    } catch (err) {
      setError(err.message || `Failed to ${action} menu item`);
    } finally {
      setLoading(false);
    }
  };

  const getMealName = (mealId) => {
    const meal = meals.find(m => m.id === mealId);
    return meal ? meal.type : 'General';
  };

  return (
    <div className="meal-management">
      <div className="meal-header">
        <h1>🍽️ Meal Management</h1>
        <button className="back-button" onClick={onBack}>← Back to Admin</button>
      </div>

      <div className="meal-tabs">
        <button
          className={activeTab === 'meals' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('meals')}
        >
          Meal Times
        </button>
        <button
          className={activeTab === 'menu' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('menu')}
        >
          Menu Items
        </button>
      </div>

      {error && (
        <div className="error-message">
          ❌ {error}
        </div>
      )}

      {/* Meal Times Tab */}
      {activeTab === 'meals' && (
        <div className="content-section">
          <div className="section-header">
            <h2>Meal Times Configuration</h2>
            <button className="add-button" onClick={handleAddMeal}>
              ➕ Add Meal Time
            </button>
          </div>

          <div className="meals-grid">
            {meals.map((meal) => (
              <div key={meal.id} className="meal-card">
                <div className="meal-card-header">
                  <h3>{meal.type}</h3>
                  <button
                    className="edit-btn-small"
                    onClick={() => handleEditMeal(meal)}
                    title="Edit Meal Time"
                  >
                    ✏️
                  </button>
                </div>
                <div className="meal-card-body">
                  <div className="time-info">
                    <span className="time-label">Start:</span>
                    <span className="time-value">{meal.startTime}</span>
                  </div>
                  <div className="time-info">
                    <span className="time-label">End:</span>
                    <span className="time-value">{meal.endTime}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Menu Items Tab */}
      {activeTab === 'menu' && (
        <div className="content-section">
          <div className="section-header">
            <h2>Menu Items Management</h2>
            <button className="add-button" onClick={handleAddMenuItem}>
              ➕ Add Menu Item
            </button>
          </div>

          <div className="menu-table-container">
            <table className="menu-table">
              <thead>
                <tr>
                  <th>Item Name</th>
                  <th>Meal Type</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {menuItems.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="no-data">No menu items found</td>
                  </tr>
                ) : (
                  menuItems.map((item) => (
                    <tr key={item.id} className={!item.isActive ? 'disabled-row' : ''}>
                      <td>{item.itemName}</td>
                      <td>{item.meal ? item.meal.type : 'General'}</td>
                      <td>₹{item.price.toFixed(2)}</td>
                      <td>
                        <span className={`status-badge ${item.isActive ? 'active' : 'inactive'}`}>
                          {item.isActive ? '✓ Active' : '✗ Disabled'}
                        </span>
                      </td>
                      <td className="actions">
                        <button
                          className="edit-btn"
                          onClick={() => handleEditMenuItem(item)}
                          title="Edit Item"
                        >
                          ✏️
                        </button>
                        <button
                          className={item.isActive ? 'disable-btn' : 'enable-btn'}
                          onClick={() => handleToggleMenuItem(item)}
                          title={item.isActive ? 'Disable Item' : 'Enable Item'}
                        >
                          {item.isActive ? '🚫' : '✅'}
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal for Add/Edit */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>
                {activeTab === 'meals'
                  ? (modalMode === 'add' ? '➕ Add Meal Time' : '✏️ Edit Meal Time')
                  : (modalMode === 'add' ? '➕ Add Menu Item' : '✏️ Edit Menu Item')}
              </h2>
              <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            {activeTab === 'meals' ? (
              <form onSubmit={handleSaveMeal} className="modal-form">
                <div className="form-group">
                  <label>Meal Type *</label>
                  <select
                    value={mealForm.type}
                    onChange={(e) => setMealForm({ ...mealForm, type: e.target.value })}
                    required
                    disabled={modalMode === 'edit'}
                  >
                    <option value="BREAKFAST">BREAKFAST</option>
                    <option value="LUNCH">LUNCH</option>
                    <option value="DINNER">DINNER</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Start Time *</label>
                  <input
                    type="time"
                    value={mealForm.startTime}
                    onChange={(e) => setMealForm({ ...mealForm, startTime: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>End Time *</label>
                  <input
                    type="time"
                    value={mealForm.endTime}
                    onChange={(e) => setMealForm({ ...mealForm, endTime: e.target.value })}
                    required
                  />
                </div>

                <div className="modal-actions">
                  <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="submit-btn" disabled={loading}>
                    {loading ? 'Saving...' : (modalMode === 'add' ? 'Add Meal' : 'Update Meal')}
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleSaveMenuItem} className="modal-form">
                <div className="form-group">
                  <label>Item Name *</label>
                  <input
                    type="text"
                    value={menuForm.itemName}
                    onChange={(e) => setMenuForm({ ...menuForm, itemName: e.target.value })}
                    required
                    placeholder="e.g., Dal Rice"
                  />
                </div>

                <div className="form-group">
                  <label>Price (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={menuForm.price}
                    onChange={(e) => setMenuForm({ ...menuForm, price: e.target.value })}
                    required
                    placeholder="e.g., 40.00"
                  />
                </div>

                <div className="form-group">
                  <label>Meal Type</label>
                  <select
                    value={menuForm.mealId}
                    onChange={(e) => setMenuForm({ ...menuForm, mealId: e.target.value })}
                  >
                    <option value="">General (No specific meal)</option>
                    {meals.map((meal) => (
                      <option key={meal.id} value={meal.id}>
                        {meal.type}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={menuForm.isActive}
                      onChange={(e) => setMenuForm({ ...menuForm, isActive: e.target.checked })}
                    />
                    <span>Active (visible to customers)</span>
                  </label>
                </div>

                <div className="modal-actions">
                  <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="submit-btn" disabled={loading}>
                    {loading ? 'Saving...' : (modalMode === 'add' ? 'Add Item' : 'Update Item')}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default MealManagement;