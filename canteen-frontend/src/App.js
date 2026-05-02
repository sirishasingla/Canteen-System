import React, { useState } from 'react';
import './App.css';
import WelcomeScreen from './components/WelcomeScreen';
import MenuScreen from './components/MenuScreen';
import OrderSummary from './components/OrderSummary';
import SuccessScreen from './components/SuccessScreen';
import AdminPanel from './components/AdminPanel';
import EmployeeManagement from './components/EmployeeManagement';

function App() {
  const [screen, setScreen] = useState('welcome'); // welcome, menu, summary, success, admin, employees
  const [customerType, setCustomerType] = useState(null);
  const [customerData, setCustomerData] = useState({});
  const [cart, setCart] = useState([]);
  const [currentMeal, setCurrentMeal] = useState(null);

  const handleCustomerTypeSelect = (type, data) => {
    setCustomerType(type);
    setCustomerData(data);
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
        />
      )}
      {screen === 'employees' && (
        <EmployeeManagement onBack={handleCloseEmployeeManagement} />
      )}
    </div>
  );
}

export default App;
