import React, { useState } from 'react';
import './App.css';
import WelcomeScreen from './components/WelcomeScreen';
import MenuScreen from './components/MenuScreen';
import OrderSummary from './components/OrderSummary';
import SuccessScreen from './components/SuccessScreen';
import AdminPanel from './components/AdminPanel';
import EmployeeManagement from './components/EmployeeManagement';
import MealManagement from './components/MealManagement';
import { api } from './services/api';

function App() {
  const [screen, setScreen] = useState('welcome'); // welcome, menu, summary, success, admin, employees, meals
  const [customerType, setCustomerType] = useState(null);
  const [customerData, setCustomerData] = useState({});
  const [cart, setCart] = useState([]);
  const [currentMeal, setCurrentMeal] = useState(null);

  const handleCustomerTypeSelect = async (type, data) => {
    setCustomerType(type);
    
    // Fetch employee details if employee or guest
    let enrichedData = { ...data };
    
    if (type === 'EMPLOYEE' && data.empId) {
      try {
        const employee = await api.getEmployeeByEmpId(data.empId);
        
        // Validate employee is active
        if (!employee.isActive) {
          alert('This employee account is inactive. Please contact HR.');
          return; // Don't proceed with login
        }
        
        enrichedData = {
          ...data,
          employeeName: employee.name,
          department: employee.department,
          role: employee.role
        };
      } catch (error) {
        console.error('Failed to fetch employee details:', error);
        alert('Invalid Employee ID. Please check and try again.');
        return; // Don't proceed with login
      }
    } else if (type === 'GUEST' && data.hostEmpId) {
      try {
        const hostEmployee = await api.getEmployeeByEmpId(data.hostEmpId);
        
        // Validate host employee is active
        if (!hostEmployee.isActive) {
          alert('The host employee account is inactive. Please use a different host employee ID.');
          return; // Don't proceed with login
        }
        
        enrichedData = {
          ...data,
          hostEmployeeName: hostEmployee.name,
          hostDepartment: hostEmployee.department
        };
      } catch (error) {
        console.error('Failed to fetch host employee details:', error);
        alert('Invalid Host Employee ID. Please check and try again.');
        return; // Don't proceed with login
      }
    }
    
    setCustomerData(enrichedData);
    setScreen('menu');
  };

  const handleAddToCart = (item) => {
    const existingItem = cart.find(cartItem => cartItem.id === item.id);
    if (existingItem) {
      setCart(cart.map(cartItem =>
        cartItem.id === item.id
          ? { ...cartItem, quantity: cartItem.quantity + 1 }
          : cartItem
      ));
    } else {
      setCart([...cart, { ...item, quantity: 1 }]);
    }
  };

  const handleRemoveFromCart = (itemId) => {
    const existingItem = cart.find(cartItem => cartItem.id === itemId);
    if (existingItem.quantity > 1) {
      setCart(cart.map(cartItem =>
        cartItem.id === itemId
          ? { ...cartItem, quantity: cartItem.quantity - 1 }
          : cartItem
      ));
    } else {
      setCart(cart.filter(cartItem => cartItem.id !== itemId));
    }
  };

  const handleProceedToSummary = (meal) => {
    setCurrentMeal(meal);
    setScreen('summary');
  };

  const handleConfirmOrder = () => {
    setScreen('success');
  };

  const handleStartOver = () => {
    setScreen('welcome');
    setCustomerType(null);
    setCustomerData({});
    setCart([]);
    setCurrentMeal(null);
  };

  const handleOpenAdmin = () => {
    setScreen('admin');
  };

  const handleCloseAdmin = () => {
    setScreen('welcome');
  };

  const handleOpenEmployeeManagement = () => {
    setScreen('employees');
  };

  const handleCloseEmployeeManagement = () => {
    setScreen('admin');
  };

  const handleOpenMealManagement = () => {
    setScreen('meals');
  };

  const handleCloseMealManagement = () => {
    setScreen('admin');
  };

  return (
    <div className="App">
      {screen === 'welcome' && (
        <WelcomeScreen
          onCustomerTypeSelect={handleCustomerTypeSelect}
          onOpenAdmin={handleOpenAdmin}
        />
      )}
      {screen === 'menu' && (
        <MenuScreen
          customerType={customerType}
          customerData={customerData}
          cart={cart}
          onAddToCart={handleAddToCart}
          onRemoveFromCart={handleRemoveFromCart}
          onProceedToSummary={handleProceedToSummary}
          onBack={handleStartOver}
        />
      )}
      {screen === 'summary' && (
        <OrderSummary
          customerType={customerType}
          customerData={customerData}
          cart={cart}
          currentMeal={currentMeal}
          onConfirm={handleConfirmOrder}
          onBack={() => setScreen('menu')}
        />
      )}
      {screen === 'success' && (
        <SuccessScreen onStartOver={handleStartOver} />
      )}
      {screen === 'admin' && (
        <AdminPanel
          onBack={handleCloseAdmin}
          onManageEmployees={handleOpenEmployeeManagement}
          onManageMeals={handleOpenMealManagement}
        />
      )}
      {screen === 'employees' && (
        <EmployeeManagement onBack={handleCloseEmployeeManagement} />
      )}
      {screen === 'meals' && (
        <MealManagement onBack={handleCloseMealManagement} />
      )}
    </div>
  );
}

export default App;
