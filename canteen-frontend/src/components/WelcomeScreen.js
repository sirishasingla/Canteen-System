import React, { useState } from 'react';
import './WelcomeScreen.css';

function WelcomeScreen({ onCustomerTypeSelect, onOpenAdmin }) {
  const [selectedType, setSelectedType] = useState(null);
  const [empId, setEmpId] = useState('');
  const [outsiderName, setOutsiderName] = useState('');
  const [hostEmpId, setHostEmpId] = useState('');
  const [purpose, setPurpose] = useState('');
  const [guestCount, setGuestCount] = useState('');
  const [companyEmployeeCount, setCompanyEmployeeCount] = useState('');
  const [error, setError] = useState('');

  const handleTypeSelect = (type) => {
    setSelectedType(type);
    setError('');
  };

  const handleSubmit = () => {
    setError('');

    if (selectedType === 'EMPLOYEE') {
      if (!empId.trim()) {
        setError('Please enter your Employee ID');
        return;
      }
      onCustomerTypeSelect('EMPLOYEE', { empId: empId.trim() });
    } else if (selectedType === 'OUTSIDER') {
      if (!outsiderName.trim()) {
        setError('Please enter your name');
        return;
      }
      onCustomerTypeSelect('OUTSIDER', { outsiderName: outsiderName.trim() });
    } else if (selectedType === 'GUEST') {
      if (!hostEmpId.trim() || !purpose.trim() || !guestCount || !companyEmployeeCount) {
        setError('Please fill in Host Employee ID, Purpose, Number of Guests, and Number of Company Employees');
        return;
      }
      if (isNaN(guestCount) || guestCount < 1) {
        setError('Please enter a valid guest count');
        return;
      }
      if (isNaN(companyEmployeeCount) || companyEmployeeCount < 1) {
        setError('Please enter a valid number of company employees');
        return;
      }
      onCustomerTypeSelect('GUEST', {
        hostEmpId: hostEmpId.trim(),
        purpose: purpose.trim(),
        guestCount: parseInt(guestCount),
        companyEmployeeCount: parseInt(companyEmployeeCount)
      });
    }
  };

  return (
    <div className="welcome-screen">
      <button className="admin-button" onClick={onOpenAdmin} title="Admin Panel">
        ⚙️ Admin
      </button>
      
      <div className="welcome-container">
        <h1 className="welcome-title">🍽️ Welcome to Canteen</h1>
        <p className="welcome-subtitle">Please select your customer type</p>

        <div className="customer-type-buttons">
          <button
            className={`type-button ${selectedType === 'EMPLOYEE' ? 'active' : ''}`}
            onClick={() => handleTypeSelect('EMPLOYEE')}
          >
            <span className="type-icon">👤</span>
            <span className="type-label">Employee</span>
          </button>

          <button
            className={`type-button ${selectedType === 'OUTSIDER' ? 'active' : ''}`}
            onClick={() => handleTypeSelect('OUTSIDER')}
          >
            <span className="type-icon">🚶</span>
            <span className="type-label">Outsider</span>
          </button>

          <button
            className={`type-button ${selectedType === 'GUEST' ? 'active' : ''}`}
            onClick={() => handleTypeSelect('GUEST')}
          >
            <span className="type-icon">👥</span>
            <span className="type-label">Guest</span>
          </button>
        </div>

        {selectedType && (
          <div className="input-section">
            {selectedType === 'EMPLOYEE' && (
              <div className="input-group">
                <label>Employee ID</label>
                <input
                  type="text"
                  placeholder="User ID"
                  value={empId}
                  onChange={(e) => setEmpId(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSubmit()}
                  autoFocus
                />
              </div>
            )}

            {selectedType === 'OUTSIDER' && (
              <div className="input-group">
                <label>Your Name</label>
                <input
                  type="text"
                  placeholder="Enter your name"
                  value={outsiderName}
                  onChange={(e) => setOutsiderName(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSubmit()}
                  autoFocus
                />
              </div>
            )}

            {selectedType === 'GUEST' && (
              <>
                <div className="input-group">
                  <label>Host Employee ID</label>
                  <input
                    type="text"
                    placeholder="User ID"
                    value={hostEmpId}
                    onChange={(e) => setHostEmpId(e.target.value)}
                    autoFocus
                  />
                </div>
                <div className="input-group">
                  <label>Purpose *</label>
                  <input
                    type="text"
                    placeholder="e.g., Client meeting, interview panel"
                    value={purpose}
                    onChange={(e) => setPurpose(e.target.value)}
                  />
                </div>
                <div className="input-group">
                  <label>Number of Guests</label>
                  <input
                    type="number"
                    placeholder="Enter number of guests"
                    value={guestCount}
                    onChange={(e) => setGuestCount(e.target.value)}
                    min="1"
                  />
                </div>
                <div className="input-group">
                  <label>Number of Company Employees</label>
                  <input
                    type="number"
                    placeholder="Employees hosting the guests"
                    value={companyEmployeeCount}
                    onChange={(e) => setCompanyEmployeeCount(e.target.value)}
                    min="1"
                    onKeyPress={(e) => e.key === 'Enter' && handleSubmit()}
                  />
                </div>
              </>
            )}

            {error && <div className="error-message">{error}</div>}

            <button className="continue-button" onClick={handleSubmit}>
              Continue to Menu →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default WelcomeScreen;