// Use relative URL so requests go through nginx proxy
const API_BASE_URL = '/api';

export const api = {
  // Get all meals
  getMeals: async () => {
    const response = await fetch(`${API_BASE_URL}/meals`);
    if (!response.ok) throw new Error('Failed to fetch meals');
    return response.json();
  },

  // Get current meal
  getCurrentMeal: async () => {
    const response = await fetch(`${API_BASE_URL}/meals/current`);
    if (!response.ok) throw new Error('No meal is currently being served');
    return response.json();
  },

  // Get menu by meal ID
  getMenuByMealId: async (mealId) => {
    const response = await fetch(`${API_BASE_URL}/menu/meal/${mealId}`);
    if (!response.ok) throw new Error('Failed to fetch menu');
    return response.json();
  },

  // Get current meal menu
  getCurrentMenu: async () => {
    const response = await fetch(`${API_BASE_URL}/menu/current`);
    if (!response.ok) throw new Error('No menu available for current meal');
    return response.json();
  },

  // Get all active menu items (no time restriction)
  getAllMenuItems: async () => {
    const response = await fetch(`${API_BASE_URL}/menu/items`);
    if (!response.ok) throw new Error('Failed to fetch menu items');
    return response.json();
  },

  // Get ALL menu items including inactive (for management)
  getAllMenuItemsForManagement: async () => {
    const response = await fetch(`${API_BASE_URL}/menu/all`);
    if (!response.ok) throw new Error('Failed to fetch menu items');
    return response.json();
  },

  // Create order
  createOrder: async (orderData) => {
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(orderData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create order');
    }
    return response.json();
  },

  // Get orders by employee
  getOrdersByEmployee: async (empId) => {
    const response = await fetch(`${API_BASE_URL}/orders/employee/${empId}`);
    if (!response.ok) throw new Error('Failed to fetch orders');
    return response.json();
  },

  // Admin Reports APIs
  
  // Get sales report
  getSalesReport: async (startTime, endTime) => {
    const response = await fetch(
      `${API_BASE_URL}/reports/sales?startTime=${startTime}&endTime=${endTime}`
    );
    if (!response.ok) throw new Error('Failed to fetch sales report');
    return response.json();
  },

  // Get employee cost report
  getEmployeeCostReport: async (startDate, endDate) => {
    const response = await fetch(
      `${API_BASE_URL}/reports/employee-cost?startDate=${startDate}&endDate=${endDate}`
    );
    if (!response.ok) throw new Error('Failed to fetch employee cost report');
    return response.json();
  },

  // Get employee order history
  getEmployeeOrderHistory: async (empId, startDate, endDate) => {
    const response = await fetch(
      `${API_BASE_URL}/reports/employee-history?empId=${empId}&startDate=${startDate}&endDate=${endDate}`
    );
    if (!response.ok) throw new Error('Failed to fetch employee order history');
    return response.json();
  },

  // Get meal count report
  getMealCountReport: async (startDate, endDate, groupBy) => {
    const response = await fetch(
      `${API_BASE_URL}/reports/meal-count?startDate=${startDate}&endDate=${endDate}&groupBy=${groupBy}`
    );
    if (!response.ok) throw new Error('Failed to fetch meal count report');
    return response.json();
  },

  // Download Excel reports
  downloadExcelReport: async (reportType, startDate, endDate) => {
    const response = await fetch(
      `${API_BASE_URL}/reports/excel/${reportType}?startDate=${startDate}&endDate=${endDate}`
    );
    if (!response.ok) throw new Error('Failed to download Excel report');
    
    // Get the blob from response with correct MIME type
    const blob = await response.blob();
    
    // Extract filename from Content-Disposition header or create default
    const contentDisposition = response.headers.get('Content-Disposition');
    let filename = `${reportType}_${startDate}_to_${endDate}.xlsx`;
    if (contentDisposition) {
      // Match filename with or without quotes, and remove quotes if present
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '');
      }
    }
    
    // Create download link and trigger download
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  },

  // Upload employees from Excel file
  uploadEmployees: async (file, clearExisting = false) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('clearExisting', clearExisting);

    const response = await fetch(
      `${API_BASE_URL}/employees/upload`,
      {
        method: 'POST',
        body: formData,
      }
    );

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to upload employees');
    }

    return response.json();
  },

  // Get employee count
  getEmployeeCount: async () => {
    const response = await fetch(`${API_BASE_URL}/employees/count`);
    if (!response.ok) throw new Error('Failed to get employee count');
    return response.json();
  },

  // Get all employees with optional search
  getAllEmployees: async (search = '') => {
    const url = search
      ? `${API_BASE_URL}/employees?search=${encodeURIComponent(search)}`
      : `${API_BASE_URL}/employees`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Failed to fetch employees');
    return response.json();
  },

  // Get employee by empId
  getEmployeeByEmpId: async (empId) => {
    const response = await fetch(`${API_BASE_URL}/employees/${empId}`);
    if (!response.ok) throw new Error('Employee not found');
    return response.json();
  },

  // Create new employee
  createEmployee: async (employeeData) => {
    const response = await fetch(`${API_BASE_URL}/employees/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(employeeData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create employee');
    }
    return response.json();
  },

  // Update employee
  updateEmployee: async (empId, employeeData) => {
    const response = await fetch(`${API_BASE_URL}/employees/${empId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(employeeData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update employee');
    }
    return response.json();
  },

  // Delete employee
  deleteEmployee: async (empId) => {
    const response = await fetch(`${API_BASE_URL}/employees/${empId}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to delete employee');
    }
    return response.json();
  },

  // Toggle employee active status
  toggleEmployeeStatus: async (empId) => {
    const response = await fetch(`${API_BASE_URL}/employees/${empId}/toggle-status`, {
      method: 'POST',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to toggle employee status');
    }
    return response.json();
  },

  // Meal Management APIs
  
  // Create new meal
  createMeal: async (mealData) => {
    const response = await fetch(`${API_BASE_URL}/meals`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(mealData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create meal');
    }
    return response.json();
  },

  // Update meal
  updateMeal: async (mealId, mealData) => {
    const response = await fetch(`${API_BASE_URL}/meals/${mealId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(mealData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update meal');
    }
    return response.json();
  },

  // Menu Item Management APIs
  
  // Create new menu item
  createMenuItem: async (menuData) => {
    const response = await fetch(`${API_BASE_URL}/menu`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(menuData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create menu item');
    }
    return response.json();
  },

  // Update menu item
  updateMenuItem: async (menuId, menuData) => {
    const response = await fetch(`${API_BASE_URL}/menu/${menuId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(menuData),
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update menu item');
    }
    return response.json();
  },

  // Toggle menu item active status
  toggleMenuItem: async (menuId) => {
    const response = await fetch(`${API_BASE_URL}/menu/${menuId}/toggle-status`, {
      method: 'POST',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to toggle menu item status');
    }
    return response.json();
  },
};