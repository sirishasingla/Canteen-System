import React, { useState, useEffect } from 'react';
import './OrderEditModal.css';
import { api } from '../services/api';

const toDatetimeLocal = (iso) => {
  if (!iso) return '';
  // Convert ISO string to yyyy-MM-ddTHH:mm (local time) for datetime-local input
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const OrderEditModal = ({ order, onClose, onSaved }) => {
  const isEdit = !!order;
  const [customerType, setCustomerType] = useState(order?.customerType || 'EMPLOYEE');
  const [empId, setEmpId] = useState(order?.employeeId || '');
  const [outsiderName, setOutsiderName] = useState(order?.outsiderName || '');
  const [hostEmpId, setHostEmpId] = useState(order?.hostEmployeeId || '');
  const [purpose, setPurpose] = useState(order?.purpose || '');
  const [guestCount, setGuestCount] = useState(order?.guestCount || '');
  const [companyEmployeeCount, setCompanyEmployeeCount] = useState(order?.companyEmployeeCount || '');
  const [orderTime, setOrderTime] = useState(toDatetimeLocal(order?.orderTime));
  const [menuItems, setMenuItems] = useState([]);
  const [items, setItems] = useState(
    (order?.items || []).map(i => ({ menuId: i.itemId ?? i.menuId, quantity: i.quantity, itemName: i.itemName, price: i.price }))
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const data = await api.getAllMenuItemsForManagement();
        setMenuItems(data.filter(m => m.isActive));
      } catch (err) {
        setError('Failed to load menu: ' + err.message);
      }
    })();
  }, []);

  // For edit mode, the order's item.itemId is the OrderItem.id, not menuId.
  // We need to map by item name to get the actual menuId. Once the menu loads,
  // reconcile items so save works correctly.
  useEffect(() => {
    if (isEdit && menuItems.length && items.length && items.some(i => !i.menuId || typeof i.menuId !== 'number' || !menuItems.find(m => m.id === i.menuId))) {
      setItems(prev => prev.map(i => {
        const found = menuItems.find(m => m.itemName === i.itemName);
        return found ? { ...i, menuId: found.id, price: found.price } : i;
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [menuItems]);

  const addItem = () => {
    if (!menuItems.length) return;
    const first = menuItems[0];
    setItems([...items, { menuId: first.id, itemName: first.itemName, price: first.price, quantity: 1 }]);
  };

  const updateItem = (idx, field, value) => {
    setItems(items.map((it, i) => {
      if (i !== idx) return it;
      if (field === 'menuId') {
        const menu = menuItems.find(m => m.id === Number(value));
        return { ...it, menuId: Number(value), itemName: menu?.itemName, price: menu?.price };
      }
      if (field === 'quantity') {
        return { ...it, quantity: Math.max(1, Number(value) || 1) };
      }
      return it;
    }));
  };

  const removeItem = (idx) => setItems(items.filter((_, i) => i !== idx));

  const totalAmount = items.reduce((sum, it) => sum + (Number(it.price) || 0) * (Number(it.quantity) || 0), 0);

  const validate = () => {
    if (!items.length) return 'Add at least one item';
    if (customerType === 'EMPLOYEE' && !empId.trim()) return 'Employee ID is required';
    if (customerType === 'OUTSIDER' && !outsiderName.trim()) return 'Outsider name is required';
    if (customerType === 'GUEST') {
      if (!hostEmpId.trim()) return 'Host Employee ID is required';
      if (!purpose.trim()) return 'Purpose is required';
      if (!guestCount) return 'Number of guests is required';
      if (!companyEmployeeCount) return 'Number of company employees is required';
    }
    return null;
  };

  const handleSave = async () => {
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    setLoading(true);
    setError('');

    const payload = {
      customerType,
      items: items.map(i => ({ menuId: i.menuId, quantity: Number(i.quantity) })),
      orderTime: orderTime ? orderTime + ':00' : null,
    };
    if (customerType === 'EMPLOYEE') payload.empId = empId.trim();
    if (customerType === 'OUTSIDER') payload.outsiderName = outsiderName.trim();
    if (customerType === 'GUEST') {
      payload.hostEmpId = hostEmpId.trim();
      payload.purpose = purpose.trim();
      payload.guestCount = Number(guestCount);
      payload.companyEmployeeCount = Number(companyEmployeeCount);
    }

    try {
      if (isEdit) {
        await api.updateOrder(order.orderId, payload);
      } else {
        await api.createOrderAsAdmin(payload);
      }
      onSaved();
    } catch (err) {
      setError(err.message || 'Failed to save');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="oem-backdrop" onClick={onClose}>
      <div className="oem-modal" onClick={e => e.stopPropagation()}>
        <div className="oem-header">
          <h2>{isEdit ? `✏️ Edit Order #${order.orderId}` : '➕ Add Manual Order'}</h2>
          <button className="oem-close" onClick={onClose}>✕</button>
        </div>

        <div className="oem-body">
          <div className="oem-section">
            <label>Customer Type</label>
            <div className="oem-type-picker">
              {['EMPLOYEE', 'OUTSIDER', 'GUEST'].map(t => (
                <button
                  key={t}
                  type="button"
                  className={`oem-type-btn ${customerType === t ? 'active' : ''}`}
                  onClick={() => setCustomerType(t)}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>

          {customerType === 'EMPLOYEE' && (
            <div className="oem-section">
              <label>Employee ID *</label>
              <input value={empId} onChange={e => setEmpId(e.target.value)} placeholder="Full 8-digit ID or last 5 digits" />
            </div>
          )}

          {customerType === 'OUTSIDER' && (
            <div className="oem-section">
              <label>Outsider Name *</label>
              <input value={outsiderName} onChange={e => setOutsiderName(e.target.value)} />
            </div>
          )}

          {customerType === 'GUEST' && (
            <>
              <div className="oem-section">
                <label>Host Employee ID *</label>
                <input value={hostEmpId} onChange={e => setHostEmpId(e.target.value)} placeholder="Full 8-digit ID or last 5 digits" />
              </div>
              <div className="oem-section">
                <label>Purpose *</label>
                <input value={purpose} onChange={e => setPurpose(e.target.value)} placeholder="e.g., Client meeting" />
              </div>
              <div className="oem-row">
                <div className="oem-section">
                  <label>Guests *</label>
                  <input type="number" min="1" value={guestCount} onChange={e => setGuestCount(e.target.value)} />
                </div>
                <div className="oem-section">
                  <label>Company Employees *</label>
                  <input type="number" min="1" value={companyEmployeeCount} onChange={e => setCompanyEmployeeCount(e.target.value)} />
                </div>
              </div>
            </>
          )}

          <div className="oem-section">
            <label>Order Date/Time {isEdit && <span className="hint">(leave to keep current)</span>}</label>
            <input type="datetime-local" value={orderTime} onChange={e => setOrderTime(e.target.value)} />
          </div>

          <div className="oem-section">
            <div className="oem-items-header">
              <label>Items *</label>
              <button type="button" className="oem-add-item" onClick={addItem}>➕ Add Item</button>
            </div>
            {items.length === 0 && <p className="oem-empty">No items yet — click "Add Item"</p>}
            {items.map((it, idx) => (
              <div key={idx} className="oem-item-row">
                <select value={it.menuId || ''} onChange={e => updateItem(idx, 'menuId', e.target.value)}>
                  {menuItems.map(m => (
                    <option key={m.id} value={m.id}>{m.itemName} — ₹{m.price}</option>
                  ))}
                </select>
                <input
                  type="number"
                  min="1"
                  value={it.quantity}
                  onChange={e => updateItem(idx, 'quantity', e.target.value)}
                  className="oem-qty"
                />
                <span className="oem-line-total">₹{((it.price || 0) * (it.quantity || 0)).toFixed(2)}</span>
                <button type="button" className="oem-remove-item" onClick={() => removeItem(idx)}>✕</button>
              </div>
            ))}
            <div className="oem-total">Total: ₹{totalAmount.toFixed(2)}</div>
          </div>

          {error && <div className="oem-error">❌ {error}</div>}
        </div>

        <div className="oem-footer">
          <button className="oem-btn-cancel" onClick={onClose} disabled={loading}>Cancel</button>
          <button className="oem-btn-save" onClick={handleSave} disabled={loading}>
            {loading ? 'Saving…' : (isEdit ? '💾 Save Changes' : '➕ Create Order')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default OrderEditModal;
