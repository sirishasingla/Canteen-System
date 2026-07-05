import React, { useState, useEffect, useCallback } from 'react';
import './OrderManagement.css';
import { api } from '../services/api';
import OrderEditModal from './OrderEditModal';

const todayIso = () => new Date().toISOString().slice(0, 10);
const weekAgoIso = () => {
  const d = new Date();
  d.setDate(d.getDate() - 7);
  return d.toISOString().slice(0, 10);
};

const OrderManagement = ({ onBack }) => {
  const [startDate, setStartDate] = useState(weekAgoIso());
  const [endDate, setEndDate] = useState(todayIso());
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [editingOrder, setEditingOrder] = useState(null); // null | 'new' | orderObject
  const [showCancelled, setShowCancelled] = useState(true);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const startDateTime = `${startDate}T00:00:00`;
      const endDateTime = `${endDate}T23:59:59`;
      const data = await api.getAllOrdersInRange(startDateTime, endDateTime);
      data.sort((a, b) => new Date(b.orderTime) - new Date(a.orderTime));
      setOrders(data);
    } catch (err) {
      setError(err.message || 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleCancel = async (order) => {
    if (!window.confirm(`Cancel order #${order.orderId}? It will be excluded from reports but preserved in the database.`)) {
      return;
    }
    try {
      await api.cancelOrder(order.orderId);
      await fetchOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRestore = async (order) => {
    try {
      await api.restoreOrder(order.orderId);
      await fetchOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSaved = async () => {
    setEditingOrder(null);
    await fetchOrders();
  };

  const customerLabel = (order) => {
    if (order.customerType === 'EMPLOYEE') {
      return `👤 ${order.employeeName || 'Unknown'}${order.employeeId ? ` (${order.employeeId})` : ''}`;
    }
    if (order.customerType === 'OUTSIDER') {
      return `🚶 ${order.outsiderName || 'Outsider'}`;
    }
    if (order.customerType === 'GUEST') {
      return `👥 Guest of ${order.hostEmployeeName || order.hostEmployeeId || 'Unknown'}`;
    }
    return order.customerType;
  };

  const visibleOrders = showCancelled ? orders : orders.filter(o => !o.isCancelled);

  return (
    <div className="order-management">
      <div className="om-header">
        <h1>📦 Order Management</h1>
        <div className="om-header-actions">
          <button className="new-order-btn" onClick={() => setEditingOrder('new')}>➕ Add Manual Order</button>
          <button className="back-button" onClick={onBack}>← Back to Admin</button>
        </div>
      </div>

      <div className="om-filters">
        <div className="filter-group">
          <label>From</label>
          <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} />
        </div>
        <div className="filter-group">
          <label>To</label>
          <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} />
        </div>
        <label className="show-cancelled-toggle">
          <input type="checkbox" checked={showCancelled} onChange={e => setShowCancelled(e.target.checked)} />
          Show cancelled
        </label>
        <button className="refresh-btn" onClick={fetchOrders} disabled={loading}>
          {loading ? '⏳' : '🔄'} Refresh
        </button>
      </div>

      {error && <div className="om-error">❌ {error}</div>}

      <div className="om-table-wrap">
        {loading ? (
          <p className="om-empty">Loading…</p>
        ) : visibleOrders.length === 0 ? (
          <p className="om-empty">No orders in this range.</p>
        ) : (
          <table className="om-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Date/Time</th>
                <th>Customer</th>
                <th>Type</th>
                <th>Items</th>
                <th>Total</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {visibleOrders.map(order => (
                <tr key={order.orderId} className={order.isCancelled ? 'row-cancelled' : ''}>
                  <td>#{order.orderId}</td>
                  <td>{new Date(order.orderTime).toLocaleString()}</td>
                  <td>{customerLabel(order)}</td>
                  <td>{order.customerType}</td>
                  <td>{order.items?.length ?? 0}</td>
                  <td>₹{Number(order.totalAmount || 0).toFixed(2)}</td>
                  <td>{order.isCancelled ? <span className="badge cancelled">Cancelled</span> : <span className="badge active">Active</span>}</td>
                  <td className="row-actions">
                    <button className="btn-edit" onClick={() => setEditingOrder(order)}>Edit</button>
                    {order.isCancelled ? (
                      <button className="btn-restore" onClick={() => handleRestore(order)}>Restore</button>
                    ) : (
                      <button className="btn-cancel" onClick={() => handleCancel(order)}>Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {editingOrder && (
        <OrderEditModal
          order={editingOrder === 'new' ? null : editingOrder}
          onClose={() => setEditingOrder(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
};

export default OrderManagement;
