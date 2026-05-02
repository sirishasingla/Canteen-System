import React, { useState, useEffect } from 'react';
import './EmployeeManagement.css';
import { api } from '../services/api';

const EmployeeManagement = ({ onBack }) => {
  const [employees, setEmployees] = useState([]);
  const [filteredEmployees, setFilteredEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
  const [currentEmployee, setCurrentEmployee] = useState({
    empId: '',
    name: '',
    department: '',
    role: 'STAFF'
  });

  useEffect(() => {
    loadEmployees();
  }, []);

  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredEmployees(employees);
    } else {
      const query = searchQuery.toLowerCase();
      setFilteredEmployees(
        employees.filter(emp =>
          emp.empId.toLowerCase().includes(query) ||
          emp.name.toLowerCase().includes(query) ||
          (emp.department && emp.department.toLowerCase().includes(query))
        )
      );
    }
  }, [searchQuery, employees]);

  const loadEmployees = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await api.getAllEmployees();
      setEmployees(data);
      setFilteredEmployees(data);
    } catch (err) {
      setError(err.message || 'Failed to load employees');
    } finally {
      setLoading(false);
    }
  };

  const handleAddEmployee = () => {
    setModalMode('add');
    setCurrentEmployee({
      empId: '',
      name: '',
      department: '',
      role: 'STAFF'
    });
    setShowModal(true);
    setError('');
  };

  const handleEditEmployee = (employee) => {
    setModalMode('edit');
    setCurrentEmployee({
      empId: employee.empId,
      name: employee.name,
      department: employee.department || '',
      role: employee.role
    });
    setShowModal(true);
    setError('');
  };

  const handleToggleStatus = async (employee) => {
    const action = employee.isActive ? 'disable' : 'enable';
    if (!window.confirm(`Are you sure you want to ${action} employee ${employee.name} (${employee.empId})?`)) {
      return;
    }

    setLoading(true);
    setError('');
    try {
      await api.toggleEmployeeStatus(employee.empId);
      await loadEmployees();
      alert(`Employee ${action}d successfully`);
    } catch (err) {
      setError(err.message || `Failed to ${action} employee`);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (modalMode === 'add') {
        await api.createEmployee(currentEmployee);
        alert('Employee added successfully');
      } else {
        await api.updateEmployee(currentEmployee.empId, currentEmployee);
        alert('Employee updated successfully');
      }
      setShowModal(false);
      await loadEmployees();
    } catch (err) {
      setError(err.message || `Failed to ${modalMode} employee`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="employee-management">
      <div className="emp-header">
        <h1>👥 Employee Management</h1>
        <button className="back-button" onClick={onBack}>← Back to Admin</button>
      </div>

      <div className="emp-controls">
        <div className="search-bar">
          <input
            type="text"
            placeholder="🔍 Search by Employee ID, Name, or Department..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <button className="add-employee-btn" onClick={handleAddEmployee}>
          ➕ Add Employee
        </button>
      </div>

      {error && (
        <div className="error-message">
          ❌ {error}
        </div>
      )}

      <div className="emp-stats">
        <div className="stat-card">
          <span className="stat-number">{filteredEmployees.length}</span>
          <span className="stat-label">
            {searchQuery ? 'Filtered' : 'Total'} Employees
          </span>
        </div>
      </div>

      {loading && !showModal ? (
        <div className="loading">Loading employees...</div>
      ) : (
        <div className="employee-table-container">
          <table className="employee-table">
            <thead>
              <tr>
                <th>Employee Code</th>
                <th>Name</th>
                <th>Department</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredEmployees.length === 0 ? (
                <tr>
                  <td colSpan="6" className="no-data">
                    {searchQuery ? 'No employees found matching your search' : 'No employees found'}
                  </td>
                </tr>
              ) : (
                filteredEmployees.map((emp) => (
                  <tr key={emp.id}>
                    <td className="emp-code">{emp.empId}</td>
                    <td>{emp.name}</td>
                    <td>{emp.department || '-'}</td>
                    <td>
                      <span className={`role-badge ${emp.role.toLowerCase()}`}>
                        {emp.role}
                      </span>
                    </td>
                    <td>
                      <span className={`status-badge ${emp.isActive ? 'active' : 'inactive'}`}>
                        {emp.isActive ? '✓ Active' : '✗ Inactive'}
                      </span>
                    </td>
                    <td className="actions">
                      <button
                        className="edit-btn"
                        onClick={() => handleEditEmployee(emp)}
                        title="Edit Employee"
                      >
                        ✏️
                      </button>
                      <button
                        className={emp.isActive ? 'disable-btn' : 'enable-btn'}
                        onClick={() => handleToggleStatus(emp)}
                        title={emp.isActive ? 'Disable Employee' : 'Enable Employee'}
                      >
                        {emp.isActive ? '🚫' : '✅'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Add/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{modalMode === 'add' ? '➕ Add New Employee' : '✏️ Edit Employee'}</h2>
              <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            <form onSubmit={handleSubmit} className="employee-form">
              <div className="form-group">
                <label>Employee Code *</label>
                <input
                  type="text"
                  value={currentEmployee.empId}
                  onChange={(e) => setCurrentEmployee({...currentEmployee, empId: e.target.value})}
                  disabled={modalMode === 'edit'}
                  required
                  placeholder="e.g., 70000011"
                />
              </div>

              <div className="form-group">
                <label>Employee Name *</label>
                <input
                  type="text"
                  value={currentEmployee.name}
                  onChange={(e) => setCurrentEmployee({...currentEmployee, name: e.target.value})}
                  required
                  placeholder="e.g., John Doe"
                />
              </div>

              <div className="form-group">
                <label>Department</label>
                <input
                  type="text"
                  value={currentEmployee.department}
                  onChange={(e) => setCurrentEmployee({...currentEmployee, department: e.target.value})}
                  placeholder="e.g., Engineering"
                />
              </div>

              <div className="form-group">
                <label>Role *</label>
                <select
                  value={currentEmployee.role}
                  onChange={(e) => setCurrentEmployee({...currentEmployee, role: e.target.value})}
                  required
                >
                  <option value="STAFF">STAFF</option>
                  <option value="WORKER">WORKER</option>
                </select>
              </div>

              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn" disabled={loading}>
                  {loading ? 'Saving...' : (modalMode === 'add' ? 'Add Employee' : 'Update Employee')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmployeeManagement;