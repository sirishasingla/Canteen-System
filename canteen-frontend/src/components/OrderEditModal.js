import React, { useState, useEffect, useMemo } from 'react';
import './OrderEditModal.css';
import { api } from '../services/api';

const toDatetimeLocal = (iso) => {
  if (!iso) return '';
  // Convert ISO string to yyyy-MM-ddTHH:mm (local time) for datetime-local input
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// UI-only "type" tabs. STAFF and WORKER both save as customerType=EMPLOYEE server-side.
const UI_TYPES = ['STAFF', 'WORKER', 'OUTSIDER', 'GUEST'];

const uiTypeToCustomerType = (t) => (t === 'STAFF' || t === 'WORKER') ? 'EMPLOYEE' : t;

// Audience drives menu filtering + shown price:
//   STAFF    → STAFF audience (needs staff_price to be visible on restricted items)
//   WORKER   → WORKER audience
//   OUTSIDER → OUTSIDER audience (uses outsider_price)
//   GUEST    → null (universal items only, at base price)
const audienceOf = (uiType) => {
  if (uiType === 'STAFF' || uiType === 'WORKER' || uiType === 'OUTSIDER') return uiType;
  return null;
};

const effectivePrice = (m, audience) => {
  if (audience === 'STAFF' && m.staffPrice != null) return m.staffPrice;
  if (audience === 'WORKER' && m.workerPrice != null) return m.workerPrice;
  if (audience === 'OUTSIDER' && m.outsiderPrice != null) return m.outsiderPrice;
  return m.price;
};

const isVisibleTo = (m, audience) => {
  const restricted = m.staffPrice != null || m.workerPrice != null || m.outsiderPrice != null;
  if (!restricted) return true;
  if (audience === 'STAFF') return m.staffPrice != null;
  if (audience === 'WORKER') return m.workerPrice != null;
  if (audience === 'OUTSIDER') return m.outsiderPrice != null;
  return false;
};

const OrderEditModal = ({ order, onClose, onSaved }) => {
  const isEdit = !!order;
  // Initial UI type: for existing EMPLOYEE orders, guess STAFF as a safe default;
  // once we've fetched the employee we correct it to their real role.
  const initialUiType = (() => {
    if (!order) return 'STAFF';
    if (order.customerType === 'EMPLOYEE') return 'STAFF';
    return order.customerType;
  })();
  const [uiType, setUiType] = useState(initialUiType);
  const [empId, setEmpId] = useState(order?.employeeId || '');
  const [outsiderName, setOutsiderName] = useState(order?.outsiderName || '');
  const [hostEmpId, setHostEmpId] = useState(order?.hostEmployeeId || '');
  const [purpose, setPurpose] = useState(order?.purpose || '');
  const [guestCount, setGuestCount] = useState(order?.guestCount || '');
  const [companyEmployeeCount, setCompanyEmployeeCount] = useState(order?.companyEmployeeCount || '');
  const [orderTime, setOrderTime] = useState(toDatetimeLocal(order?.orderTime));
  const [menuItems, setMenuItems] = useState([]);
  // Employee's real role — the backend always charges at this rate, regardless of which
  // STAFF/WORKER tab the admin picked (filter-only, charge-real-role).
  const [employeeRole, setEmployeeRole] = useState(null);
  const [items, setItems] = useState(
    (order?.items || []).map(i => ({ menuId: i.itemId ?? i.menuId, quantity: i.quantity, itemName: i.itemName, price: i.price }))
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isEmployeeType = uiType === 'STAFF' || uiType === 'WORKER';

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

  // Look up the employee's real role for accurate billing display.
  // Also — when opening an existing EMPLOYEE order — snap the UI tab to their actual role.
  useEffect(() => {
    if (!isEmployeeType || !empId.trim()) {
      setEmployeeRole(null);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const emp = await api.getEmployeeByEmpId(empId.trim());
        if (cancelled) return;
        setEmployeeRole(emp.role);
        // On first load of an existing edit, align the UI tab with the real role.
        if (isEdit && order?.customerType === 'EMPLOYEE' && (emp.role === 'STAFF' || emp.role === 'WORKER')) {
          setUiType(prev => (prev === 'STAFF' || prev === 'WORKER') ? emp.role : prev);
        }
      } catch {
        if (!cancelled) setEmployeeRole(null);
      }
    })();
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isEmployeeType, empId]);

  // Audience used to filter + price the item dropdown.
  const audience = audienceOf(uiType);
  // Audience used for saved line totals — for EMPLOYEE this is the employee's real role
  // (charge-real-role), so the UI total matches what the backend will save.
  const billedAudience = isEmployeeType ? (employeeRole || audience) : audience;

  const availableMenuItems = useMemo(
    () => menuItems
      .filter(m => isVisibleTo(m, audience))
      .map(m => ({ ...m, effectivePrice: effectivePrice(m, audience) })),
    [menuItems, audience]
  );

  // For edit mode, order.items[].itemId is OrderItem.id, not menuId — reconcile by name.
  useEffect(() => {
    if (isEdit && menuItems.length && items.length && items.some(i => !i.menuId || typeof i.menuId !== 'number' || !menuItems.find(m => m.id === i.menuId))) {
      setItems(prev => prev.map(i => {
        const found = menuItems.find(m => m.itemName === i.itemName);
        return found ? { ...i, menuId: found.id } : i;
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [menuItems]);

  const displayItems = useMemo(() => items.map(it => {
    const m = menuItems.find(x => x.id === it.menuId);
    if (!m) return it;
    return { ...it, itemName: m.itemName, price: effectivePrice(m, billedAudience) };
  }), [items, menuItems, billedAudience]);

  const addItem = () => {
    if (!availableMenuItems.length) return;
    const first = availableMenuItems[0];
    setItems([...items, { menuId: first.id, itemName: first.itemName, quantity: 1 }]);
  };

  const updateItem = (idx, field, value) => {
    setItems(items.map((it, i) => {
      if (i !== idx) return it;
      if (field === 'menuId') {
        const menu = menuItems.find(m => m.id === Number(value));
        return { ...it, menuId: Number(value), itemName: menu?.itemName };
      }
      if (field === 'quantity') {
        return { ...it, quantity: Math.max(1, Number(value) || 1) };
      }
      return it;
    }));
  };

  const removeItem = (idx) => setItems(items.filter((_, i) => i !== idx));

  const totalAmount = displayItems.reduce((sum, it) => sum + (Number(it.price) || 0) * (Number(it.quantity) || 0), 0);

  const validate = () => {
    if (!items.length) return 'Add at least one item';
    if (isEmployeeType && !empId.trim()) return 'Employee ID is required';
    if (uiType === 'OUTSIDER' && !outsiderName.trim()) return 'Outsider name is required';
    if (uiType === 'GUEST') {
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

    const customerType = uiTypeToCustomerType(uiType);
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
              {UI_TYPES.map(t => (
                <button
                  key={t}
                  type="button"
                  className={`oem-type-btn ${uiType === t ? 'active' : ''}`}
                  onClick={() => setUiType(t)}
                >
                  {t}
                </button>
              ))}
            </div>
            {isEmployeeType && employeeRole && employeeRole !== uiType && (
              <p className="hint">
                Note: employee's actual role is {employeeRole}. The order will be billed at the {employeeRole} price.
              </p>
            )}
          </div>

          {isEmployeeType && (
            <div className="oem-section">
              <label>Employee ID *</label>
              <input value={empId} onChange={e => setEmpId(e.target.value)} placeholder="User ID" />
              {empId.trim() && !employeeRole && (
                <p className="hint">Enter a valid employee ID to see role-priced items.</p>
              )}
            </div>
          )}

          {uiType === 'OUTSIDER' && (
            <div className="oem-section">
              <label>Outsider Name *</label>
              <input value={outsiderName} onChange={e => setOutsiderName(e.target.value)} />
            </div>
          )}

          {uiType === 'GUEST' && (
            <>
              <div className="oem-section">
                <label>Host Employee ID *</label>
                <input value={hostEmpId} onChange={e => setHostEmpId(e.target.value)} placeholder="Host User ID" />
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
              <button type="button" className="oem-add-item" onClick={addItem} disabled={!availableMenuItems.length}>➕ Add Item</button>
            </div>
            {items.length === 0 && <p className="oem-empty">No items yet — click "Add Item"</p>}
            {!availableMenuItems.length && items.length === 0 && (
              <p className="oem-empty">No items available for this customer type.</p>
            )}
            {displayItems.map((it, idx) => {
              const currentInAudience = availableMenuItems.some(m => m.id === it.menuId);
              // Ensure the currently-selected menuId is present as an option even if it's not
              // in the current audience (e.g. editing an older order after the customer type changed).
              const options = currentInAudience
                ? availableMenuItems
                : (() => {
                    const fallback = menuItems.find(m => m.id === it.menuId);
                    return fallback
                      ? [{ ...fallback, effectivePrice: it.price }, ...availableMenuItems]
                      : availableMenuItems;
                  })();
              return (
                <div key={idx} className="oem-item-row">
                  <select value={it.menuId || ''} onChange={e => updateItem(idx, 'menuId', e.target.value)}>
                    {options.map(m => (
                      <option key={m.id} value={m.id}>
                        {m.itemName} — ₹{Number(m.effectivePrice).toFixed(2)}
                      </option>
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
              );
            })}
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
