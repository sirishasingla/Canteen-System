import React, { useState } from 'react';
import './AdminPanel.css';
import { api } from '../services/api';

const AdminPanel = ({ onBack, onManageEmployees }) => {
  const [activeTab, setActiveTab] = useState('sales');
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Form states
  const [salesForm, setSalesForm] = useState({
    startTime: '',
    endTime: ''
  });

  const [employeeCostForm, setEmployeeCostForm] = useState({
    startDate: '',
    endDate: ''
  });

  const [employeeHistoryForm, setEmployeeHistoryForm] = useState({
    empId: '',
    startDate: '',
    endDate: ''
  });

  const [mealCountForm, setMealCountForm] = useState({
    startDate: '',
    endDate: '',
    groupBy: 'both'
  });

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

  const handleSalesReport = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await api.getSalesReport(salesForm.startTime, salesForm.endTime);
      setReportData(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch sales report');
    } finally {
      setLoading(false);
    }
  };

  const handleEmployeeCostReport = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await api.getEmployeeCostReport(employeeCostForm.startDate, employeeCostForm.endDate);
      setReportData(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch employee cost report');
    } finally {
      setLoading(false);
    }
  };

  const handleEmployeeHistoryReport = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await api.getEmployeeOrderHistory(
        employeeHistoryForm.empId,
        employeeHistoryForm.startDate,
        employeeHistoryForm.endDate
      );
      setReportData(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch employee history');
    } finally {
      setLoading(false);
    }
  };

  const handleMealCountReport = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await api.getMealCountReport(
        mealCountForm.startDate,
        mealCountForm.endDate,
        mealCountForm.groupBy
      );
      setReportData(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch meal count report');
    } finally {
      setLoading(false);
    }
  };

  const exportToCSV = () => {
    if (reportData.length === 0) return;

    const headers = Object.keys(reportData[0]).join(',');
    const rows = reportData.map(row => 
      Object.values(row).map(val => 
        typeof val === 'string' && val.includes(',') ? `"${val}"` : val
      ).join(',')
    );
    
    const csv = [headers, ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${activeTab}_report_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
  };

  const renderSalesReportTable = () => (
    <div className="report-table-container">
      <table className="report-table">
        <thead>
          <tr>
            <th>Order ID</th>
            <th>Date & Time</th>
            <th>Customer Type</th>
            <th>Employee ID</th>
            <th>Name</th>
            <th>Meal Type</th>
            <th>Items</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          {reportData.map((row, index) => (
            <tr key={index}>
              <td>{row.orderId}</td>
              <td>{new Date(row.orderTime).toLocaleString()}</td>
              <td>{row.customerType}</td>
              <td>{row.employeeId || '-'}</td>
              <td>{row.employeeName || '-'}</td>
              <td>{row.mealType}</td>
              <td>{row.itemCount}</td>
              <td>₹{row.totalAmount.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan="7"><strong>Total</strong></td>
            <td><strong>₹{reportData.reduce((sum, row) => sum + row.totalAmount, 0).toFixed(2)}</strong></td>
          </tr>
        </tfoot>
      </table>
    </div>
  );

  const renderEmployeeCostTable = () => (
    <div className="report-table-container">
      <table className="report-table">
        <thead>
          <tr>
            <th>Employee ID</th>
            <th>Name</th>
            <th>Department</th>
            <th>Order Count</th>
            <th>Total Cost</th>
          </tr>
        </thead>
        <tbody>
          {reportData.map((row, index) => (
            <tr key={index}>
              <td>{row.employeeId}</td>
              <td>{row.employeeName}</td>
              <td>{row.department}</td>
              <td>{row.orderCount}</td>
              <td>₹{row.totalCost.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan="4"><strong>Total</strong></td>
            <td><strong>₹{reportData.reduce((sum, row) => sum + row.totalCost, 0).toFixed(2)}</strong></td>
          </tr>
        </tfoot>
      </table>
    </div>
  );

  const renderMealCountTable = () => (
    <div className="report-table-container">
      <table className="report-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Meal Type</th>
            <th>Order Count</th>
            <th>Total Revenue</th>
          </tr>
        </thead>
        <tbody>
          {reportData.map((row, index) => (
            <tr key={index}>
              <td>{row.date || 'All Dates'}</td>
              <td>{row.mealType}</td>
              <td>{row.orderCount}</td>
              <td>₹{row.totalRevenue.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan="2"><strong>Total</strong></td>
            <td><strong>{reportData.reduce((sum, row) => sum + row.orderCount, 0)}</strong></td>
            <td><strong>₹{reportData.reduce((sum, row) => sum + row.totalRevenue, 0).toFixed(2)}</strong></td>
          </tr>
        </tfoot>
      </table>
    </div>
  );

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
          className={activeTab === 'sales' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('sales'); setReportData([]); setError(''); }}
        >
          Sales Report
        </button>
        <button 
          className={activeTab === 'employeeCost' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('employeeCost'); setReportData([]); setError(''); }}
        >
          Employee Cost
        </button>
        <button 
          className={activeTab === 'employeeHistory' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('employeeHistory'); setReportData([]); setError(''); }}
        >
          Employee History
        </button>
        <button
          className={activeTab === 'mealCount' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('mealCount'); setReportData([]); setError(''); }}
        >
          Meal Statistics
        </button>
        <button
          className={activeTab === 'excelReports' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('excelReports'); setReportData([]); setError(''); }}
        >
          📥 Excel Reports
        </button>
        <button
          className={activeTab === 'employeeUpload' ? 'tab active' : 'tab'}
          onClick={() => { setActiveTab('employeeUpload'); setReportData([]); setError(''); }}
        >
          👥 Upload Employees
        </button>
      </div>

      <div className="admin-content">
        {/* Sales Report Form */}
        {activeTab === 'sales' && (
          <div className="report-section">
            <h2>Sales Report</h2>
            <form onSubmit={handleSalesReport} className="report-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Start Date & Time</label>
                  <input
                    type="datetime-local"
                    value={salesForm.startTime}
                    onChange={(e) => setSalesForm({...salesForm, startTime: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>End Date & Time</label>
                  <input
                    type="datetime-local"
                    value={salesForm.endTime}
                    onChange={(e) => setSalesForm({...salesForm, endTime: e.target.value})}
                    required
                  />
                </div>
              </div>
              <button type="submit" className="generate-button" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Report'}
              </button>
            </form>
          </div>
        )}

        {/* Employee Cost Form */}
        {activeTab === 'employeeCost' && (
          <div className="report-section">
            <h2>Employee Cost Report</h2>
            <form onSubmit={handleEmployeeCostReport} className="report-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    value={employeeCostForm.startDate}
                    onChange={(e) => setEmployeeCostForm({...employeeCostForm, startDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input
                    type="date"
                    value={employeeCostForm.endDate}
                    onChange={(e) => setEmployeeCostForm({...employeeCostForm, endDate: e.target.value})}
                    required
                  />
                </div>
              </div>
              <button type="submit" className="generate-button" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Report'}
              </button>
            </form>
          </div>
        )}

        {/* Employee History Form */}
        {activeTab === 'employeeHistory' && (
          <div className="report-section">
            <h2>Employee Order History</h2>
            <form onSubmit={handleEmployeeHistoryReport} className="report-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Employee ID</label>
                  <input
                    type="text"
                    placeholder="e.g., EMP001"
                    value={employeeHistoryForm.empId}
                    onChange={(e) => setEmployeeHistoryForm({...employeeHistoryForm, empId: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    value={employeeHistoryForm.startDate}
                    onChange={(e) => setEmployeeHistoryForm({...employeeHistoryForm, startDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input
                    type="date"
                    value={employeeHistoryForm.endDate}
                    onChange={(e) => setEmployeeHistoryForm({...employeeHistoryForm, endDate: e.target.value})}
                    required
                  />
                </div>
              </div>
              <button type="submit" className="generate-button" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Report'}
              </button>
            </form>
          </div>
        )}

        {/* Meal Count Form */}
        {activeTab === 'mealCount' && (
          <div className="report-section">
            <h2>Meal Count Statistics</h2>
            <form onSubmit={handleMealCountReport} className="report-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    value={mealCountForm.startDate}
                    onChange={(e) => setMealCountForm({...mealCountForm, startDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input
                    type="date"
                    value={mealCountForm.endDate}
                    onChange={(e) => setMealCountForm({...mealCountForm, endDate: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Group By</label>
                  <select
                    value={mealCountForm.groupBy}
                    onChange={(e) => setMealCountForm({...mealCountForm, groupBy: e.target.value})}
                  >
                    <option value="day">By Day</option>
                    <option value="meal">By Meal Type</option>
                    <option value="both">By Day & Meal</option>
                  </select>
                </div>
              </div>
              <button type="submit" className="generate-button" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Report'}
              </button>
            </form>
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
                  <li><strong>Column 3:</strong> Department (Optional)</li>
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

        {/* Report Results */}
        {reportData.length > 0 && (
          <div className="report-results">
            <div className="results-header">
              <h3>Report Results ({reportData.length} records)</h3>
              <button className="export-button" onClick={exportToCSV}>
                📥 Export to CSV
              </button>
            </div>
            
            {activeTab === 'sales' && renderSalesReportTable()}
            {activeTab === 'employeeCost' && renderEmployeeCostTable()}
            {activeTab === 'employeeHistory' && renderSalesReportTable()}
            {activeTab === 'mealCount' && renderMealCountTable()}
          </div>
        )}

        {!loading && reportData.length === 0 && !error && (
          <div className="no-data">
            <p>📋 Fill in the form above and click "Generate Report" to view data</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPanel;