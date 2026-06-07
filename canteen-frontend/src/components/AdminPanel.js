import React, { useState } from 'react';
import './AdminPanel.css';
import { api } from '../services/api';

const AdminPanel = ({ onBack, onManageEmployees, onManageMeals }) => {
  const [activeTab, setActiveTab] = useState('mealManagement');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Form states
  const [excelForm, setExcelForm] = useState({
    startDate: '',
    endDate: ''
  });

  const [uploadState, setUploadState] = useState({
    file: null,
    clearExisting: false,
    uploading: false,
    result: null
  });

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setUploadState({ ...uploadState, file, result: null });
      setError('');
    }
  };

  const handleEmployeeUpload = async () => {
    if (!uploadState.file) {
      setError('Please select an Excel file');
      return;
    }

    setUploadState({ ...uploadState, uploading: true });
    setError('');

    try {
      const result = await api.uploadEmployees(uploadState.file, uploadState.clearExisting);
      setUploadState({ ...uploadState, uploading: false, result, file: null });
      
      // Reset file input
      document.getElementById('employee-file-input').value = '';
    } catch (err) {
      setError(err.message || 'Failed to upload employees');
      setUploadState({ ...uploadState, uploading: false });
    }
  };

  const handleDownloadExcel = async (reportType) => {
    if (!excelForm.startDate || !excelForm.endDate) {
      setError('Please select both start and end dates');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await api.downloadExcelReport(reportType, excelForm.startDate, excelForm.endDate);
    } catch (err) {
      setError(err.message || 'Failed to download Excel report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h1>📊 Admin Panel</h1>
        <div className="header-buttons">
          <button className="manage-employees-btn" onClick={onManageEmployees}>
            👥 Manage Employees
          </button>
          <button className="back-button" onClick={onBack}>← Back to Kiosk</button>
        </div>
      </div>

      <div className="admin-tabs">
        <button
          className={activeTab === 'mealManagement' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('mealManagement'); setError(''); }}
        >
          🍽️ Meal Management
        </button>
        <button
          className={activeTab === 'excelReports' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('excelReports'); setError(''); }}
        >
          📥 Excel Reports
        </button>
        <button
          className={activeTab === 'employeeUpload' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('employeeUpload'); setError(''); }}
        >
          👥 Upload Employees
        </button>
      </div>

      <div className="admin-content">
        {/* Meal Management Tab */}
        {activeTab === 'mealManagement' && (
          <div className="report-section">
            <h2>Meal Management</h2>
            <p className="section-description">
              Manage meal times, menu items, and meal configurations
            </p>
            <div className="meal-management-card">
              <div className="card-icon">🍽️</div>
              <h3>Meal & Menu Management</h3>
              <p>Configure meal times, add/edit menu items, and manage pricing</p>
              <ul className="feature-list">
                <li>✨ Add/Edit Meal Times</li>
                <li>✨ Manage Menu Items</li>
                <li>✨ Configure Meal Pricing</li>
                <li>✨ Enable/Disable Menu Items</li>
              </ul>
              <button className="manage-meals-btn" onClick={onManageMeals}>
                🍽️ Open Meal Management
              </button>
            </div>
          </div>
        )}

        {/* Excel Reports Tab */}
        {activeTab === 'excelReports' && (
          <div className="report-section">
            <h2>Download Excel Reports</h2>
            <p className="section-description">
              Select a date range and download Excel reports
            </p>
            
            <div className="excel-date-selector">
              <div className="form-row">
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    value={excelForm.startDate}
                    onChange={(e) => setExcelForm({...excelForm, startDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input
                    type="date"
                    value={excelForm.endDate}
                    onChange={(e) => setExcelForm({...excelForm, endDate: e.target.value})}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="excel-reports-grid">
              <div className="excel-report-card">
                <div className="report-icon">📊</div>
                <h3>Detailed Orders Report</h3>
                <button
                  className="download-excel-button"
                  onClick={() => handleDownloadExcel('detailed-orders')}
                  disabled={loading || !excelForm.startDate || !excelForm.endDate}
                >
                  {loading ? 'Downloading...' : '📥 Download Excel'}
                </button>
              </div>

              <div className="excel-report-card">
                <div className="report-icon">👥</div>
                <h3>Employee Purchase Summary</h3>
                <button
                  className="download-excel-button"
                  onClick={() => handleDownloadExcel('employee-purchases')}
                  disabled={loading || !excelForm.startDate || !excelForm.endDate}
                >
                  {loading ? 'Downloading...' : '📥 Download Excel'}
                </button>
              </div>

              <div className="excel-report-card">
                <div className="report-icon">🍽️</div>
                <h3>Item Purchase Statistics</h3>
                <button
                  className="download-excel-button"
                  onClick={() => handleDownloadExcel('item-statistics')}
                  disabled={loading || !excelForm.startDate || !excelForm.endDate}
                >
                  {loading ? 'Downloading...' : '📥 Download Excel'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Employee Upload Tab */}
        {activeTab === 'employeeUpload' && (
          <div className="report-section">
            <h2>Upload Employee Master Data</h2>
            <p className="section-description">
              Upload an Excel file to bulk import or update employee records
            </p>

            <div className="upload-section">
              <div className="upload-instructions">
                <h3>📋 File Format Requirements:</h3>
                <ul>
                  <li><strong>File Type:</strong> Excel (.xlsx or .xls)</li>
                  <li><strong>Column 1:</strong> Employee Code (Required, Unique)</li>
                  <li><strong>Column 2:</strong> Employee Name (Required)</li>
                  <li><strong>Column 3:</strong> Department (Optional, defaults to "General")</li>
                  <li><strong>Column 4:</strong> Role (Optional: STAFF or WORKER, defaults to STAFF)</li>
                  <li><strong>First Row:</strong> Should contain column headers</li>
                </ul>
                <p className="note">💡 If an employee code already exists, the record will be updated</p>
              </div>

              <div className="upload-controls">
                <div className="file-input-wrapper">
                  <input
                    id="employee-file-input"
                    type="file"
                    accept=".xlsx,.xls"
                    onChange={handleFileSelect}
                    disabled={uploadState.uploading}
                  />
                  {uploadState.file && (
                    <p className="selected-file">
                      📄 Selected: {uploadState.file.name}
                    </p>
                  )}
                </div>

                <div className="upload-options">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={uploadState.clearExisting}
                      onChange={(e) => setUploadState({ ...uploadState, clearExisting: e.target.checked })}
                      disabled={uploadState.uploading}
                    />
                    <span>Clear all existing employees before import</span>
                  </label>
                </div>

                <button
                  className="upload-button"
                  onClick={handleEmployeeUpload}
                  disabled={!uploadState.file || uploadState.uploading}
                >
                  {uploadState.uploading ? '⏳ Uploading...' : '📤 Upload Employees'}
                </button>
              </div>

              {uploadState.result && (
                <div className="upload-result">
                  <h3>✅ Upload Complete!</h3>
                  <div className="result-stats">
                    <div className="stat-item">
                      <span className="stat-label">Total Processed:</span>
                      <span className="stat-value">{uploadState.result.totalProcessed}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">New Employees:</span>
                      <span className="stat-value success">{uploadState.result.newEmployees}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Updated:</span>
                      <span className="stat-value info">{uploadState.result.updatedEmployees}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Skipped:</span>
                      <span className="stat-value">{uploadState.result.skippedRows}</span>
                    </div>
                    {uploadState.result.errorCount > 0 && (
                      <div className="stat-item">
                        <span className="stat-label">Errors:</span>
                        <span className="stat-value error">{uploadState.result.errorCount}</span>
                      </div>
                    )}
                  </div>

                  {uploadState.result.errors && uploadState.result.errors.length > 0 && (
                    <div className="error-list">
                      <h4>⚠️ Errors:</h4>
                      <ul>
                        {uploadState.result.errors.map((error, index) => (
                          <li key={index}>{error}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="error-message">
            ❌ {error}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPanel;