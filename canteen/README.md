# Canteen Management System

A complete Spring Boot application for managing canteen operations in a company, supporting employees, outsiders, and guests with automated meal tracking and billing.

## 📋 Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Setup Instructions](#setup-instructions)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Project Structure](#project-structure)

## ✨ Features

- **Multi-Customer Support**: Handle employees, outsiders, and guests
- **Automated Meal Detection**: Automatically detects current meal based on time
- **Real-time Menu**: Dynamic menu based on meal time
- **Order Tracking**: Complete order history with detailed items
- **Reporting**: Generate reports for salary deduction and analytics
- **RESTful APIs**: Clean REST APIs for all operations

## 🛠 Technology Stack

- **Backend**: Java 17, Spring Boot 4.0.5
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Additional**: Lombok for boilerplate reduction

## 🏗 Architecture

The application follows a **layered architecture**:

```
┌─────────────────────────────────────┐
│     Controller Layer (REST APIs)    │
├─────────────────────────────────────┤
│     Service Layer (Business Logic)  │
├─────────────────────────────────────┤
│     Repository Layer (Data Access)  │
├─────────────────────────────────────┤
│     Entity Layer (Database Models)  │
└─────────────────────────────────────┘
```

### Flow Diagram

```
[User at Kiosk]
        ↓
[Enter Employee ID OR Select Outsider/Guest]
        ↓
[Backend Validates Input]
        ↓
   ┌───────────────┬───────────────┬───────────────┐
   │               │               │
[Employee]     [Outsider]       [Guest]
   │               │               │
employee_id   outsider_name   host_employee_id
                                + guest_count
                                + team_name
   └───────────────┴───────────────┴───────────────┘
                        ↓
                [Detect Meal Time]
                        ↓
                [Fetch Menu Items]
                        ↓
                [User Selects Items]
                        ↓
                [Calculate Total]
                        ↓
                [CREATE ORDER]
                        ↓
        Insert into Orders Table
                        ↓
        Insert into Order_Items Table
                        ↓
                    [DONE]
```

## 💾 Database Schema

### Tables

#### Employee
```sql
- id (PK)
- emp_id (UNIQUE)
- name
- department
- role (WORKER / STAFF)
```

#### Meal
```sql
- id (PK)
- type (BREAKFAST / LUNCH / DINNER)
- start_time
- end_time
```

#### Menu
```sql
- id (PK)
- meal_id (FK)
- item_name
- price
- is_active
```

#### Orders
```sql
- id (PK)
- employee_id (FK nullable)
- meal_id (FK)
- order_time
- total_amount
- customer_type (EMPLOYEE / OUTSIDER / GUEST)
- outsider_name (nullable)
- host_employee_id (nullable)
- team_name (nullable)
- guest_count (nullable)
```

#### Order_Items
```sql
- id (PK)
- order_id (FK)
- menu_id (FK)
- quantity
- price
```

## 🚀 Setup Instructions

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

### Database Setup

1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE canteen_db;
```

2. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/canteen_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application

1. Clone the repository
2. Navigate to project directory
3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## 📡 API Endpoints

### Meal Endpoints

#### Get Current Meal
```http
GET /api/meals/current
```
Returns the current meal based on system time.

#### Get All Meals
```http
GET /api/meals
```
Returns all configured meals.

### Menu Endpoints

#### Get Current Meal Menu
```http
GET /api/menu/current
```
Returns menu items for the current meal.

#### Get Menu by Meal ID
```http
GET /api/menu/meal/{mealId}
```
Returns menu items for a specific meal.

### Order Endpoints

#### Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "customerType": "EMPLOYEE",
  "empId": "EMP001",
  "items": [
    {
      "menuId": 1,
      "quantity": 2
    },
    {
      "menuId": 2,
      "quantity": 1
    }
  ]
}
```

**For Outsider:**
```json
{
  "customerType": "OUTSIDER",
  "outsiderName": "John External",
  "items": [
    {
      "menuId": 1,
      "quantity": 1
    }
  ]
}
```

**For Guest:**
```json
{
  "customerType": "GUEST",
  "hostEmpId": "EMP001",
  "teamName": "Marketing",
  "guestCount": 3,
  "items": [
    {
      "menuId": 1,
      "quantity": 3
    }
  ]
}
```

#### Get Order by ID
```http
GET /api/orders/{id}
```

#### Get Orders by Employee
```http
GET /api/orders/employee/{empId}
```

#### Get Orders by Date Range
```http
GET /api/orders/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

#### Get Total Amount by Employee
```http
GET /api/orders/employee/{empId}/total?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

## 🧪 Testing

### Sample Data
The application automatically initializes with sample data:
- 3 sample employees (EMP001, EMP002, EMP003)
- 3 meals (Breakfast, Lunch, Dinner)
- Menu items for each meal

### Testing with Postman

1. **Test Current Meal**
```
GET http://localhost:8080/api/meals/current
```

2. **Test Current Menu**
```
GET http://localhost:8080/api/menu/current
```

3. **Create an Order**
```
POST http://localhost:8080/api/orders
Body (JSON):
{
  "customerType": "EMPLOYEE",
  "empId": "EMP001",
  "items": [
    {"menuId": 1, "quantity": 2},
    {"menuId": 2, "quantity": 1}
  ]
}
```

4. **Get Employee Orders**
```
GET http://localhost:8080/api/orders/employee/EMP001
```

## 📁 Project Structure

```
canteen/
├── src/main/java/com/cafeteria/canteen/
│   ├── config/
│   │   └── DataInitializer.java
│   ├── controller/
│   │   ├── MealController.java
│   │   ├── MenuController.java
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── MenuResponse.java
│   │   ├── OrderItemRequest.java
│   │   ├── OrderItemResponse.java
│   │   ├── OrderRequest.java
│   │   └── OrderResponse.java
│   ├── entity/
│   │   ├── Employee.java
│   │   ├── Meal.java
│   │   ├── Menu.java
│   │   ├── OrderItems.java
│   │   └── Orders.java
│   ├── enums/
│   │   ├── CustomerType.java
│   │   ├── EmployeeRole.java
│   │   └── MealType.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── repository/
│   │   ├── EmployeeRepository.java
│   │   ├── MealRepository.java
│   │   ├── MenuRepository.java
│   │   ├── OrderItemsRepository.java
│   │   └── OrderRepository.java
│   ├── service/
│   │   ├── MealService.java
│   │   ├── MenuService.java
│   │   └── OrderService.java
│   └── CanteenApplication.java
└── src/main/resources/
    └── application.properties
```

## 🎯 Use Cases

### 1. Employee Orders Food
1. Employee enters their ID at kiosk
2. System detects current meal time
3. Displays available menu items
4. Employee selects items
5. Order is created and saved
6. Amount is tracked for salary deduction

### 2. Outsider Orders Food
1. Outsider selects "No Employee ID"
2. Enters their name
3. Selects items from menu
4. Makes payment
5. Order is recorded

### 3. Guest Orders Food
1. Host employee enters their ID
2. Specifies number of guests and team
3. Selects items for guests
4. Order is recorded under host's account

### 4. Monthly Reporting
1. Accounts department queries orders by date range
2. Gets total amount per employee
3. Deducts from salary accordingly

## 🔒 Security Considerations (Future Enhancements)

- Add Spring Security for authentication
- Implement JWT tokens for API security
- Add role-based access control
- Encrypt sensitive data

## 📈 Future Enhancements

- [ ] Add payment gateway integration
- [ ] Implement slip/coupon management system
- [ ] Add overtime tracking and free meal allocation
- [ ] Create admin dashboard
- [ ] Add email notifications
- [ ] Implement inventory management
- [ ] Add analytics and reporting dashboard
- [ ] Mobile app integration

## 👥 Contributing

This is a demonstration project. Feel free to fork and enhance!

## 📄 License

This project is open source and available under the MIT License.