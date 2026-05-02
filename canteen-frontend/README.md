# Canteen Kiosk Frontend

A modern, user-friendly kiosk interface for the Canteen Management System built with React.

## Features

- **Multi-Customer Support**: Handles employees, outsiders, and guests
- **Real-time Menu**: Displays menu items based on meal time
- **Shopping Cart**: Add/remove items with quantity controls
- **Order Summary**: Review order before confirmation
- **Success Feedback**: Clear confirmation after order placement
- **Auto-redirect**: Returns to welcome screen after order completion

## Prerequisites

- Node.js 14+ and npm
- Backend API running on http://localhost:8080

## Installation

```bash
cd canteen-frontend
npm install
```

## Running the Application

```bash
npm start
```

The application will open at http://localhost:3000

## Project Structure

```
src/
├── components/
│   ├── WelcomeScreen.js      # Customer type selection
│   ├── MenuScreen.js          # Menu display and cart
│   ├── OrderSummary.js        # Order review
│   └── SuccessScreen.js       # Order confirmation
├── services/
│   └── api.js                 # API service layer
├── App.js                     # Main app component
└── App.css                    # Global styles
```

## User Flow

1. **Welcome Screen**: Select customer type (Employee/Outsider/Guest)
2. **Enter Details**: Provide required information based on customer type
3. **Menu Screen**: Browse menu items and add to cart
4. **Order Summary**: Review order details
5. **Confirmation**: Order placed successfully
6. **Auto-redirect**: Returns to welcome screen

## Customer Types

### Employee
- Enter Employee ID
- Amount deducted from salary

### Outsider
- Enter name
- Pay at counter

### Guest
- Enter host employee ID
- Specify team name and guest count
- Charged to host employee

## API Integration

The frontend communicates with the backend API:

- `GET /api/meals` - Get all meals
- `GET /api/menu/meal/{id}` - Get menu by meal ID
- `POST /api/orders` - Create new order

## Styling

- Modern gradient design
- Responsive layout
- Smooth animations
- Touch-friendly buttons for kiosk use

## Build for Production

```bash
npm run build
```

Creates optimized production build in `build/` directory.

## Deployment

The built files can be:
- Served by Spring Boot (place in `src/main/resources/static`)
- Deployed to any static hosting service
- Served by Nginx/Apache

## Browser Support

- Chrome (recommended for kiosk)
- Firefox
- Safari
- Edge

## Troubleshooting

### CORS Issues
The app uses proxy configuration in package.json. Ensure backend is running on port 8080.

### API Connection Failed
1. Verify backend is running: `curl http://localhost:8080/api/meals`
2. Check browser console for errors
3. Ensure no firewall blocking

### Menu Not Loading
- Check if you're within meal time
- Use specific meal endpoint if testing outside meal hours
- Verify sample data is loaded in database

## Future Enhancements

- [ ] Admin dashboard
- [ ] Order history view
- [ ] Payment integration
- [ ] Multi-language support
- [ ] Accessibility improvements
- [ ] Offline mode
