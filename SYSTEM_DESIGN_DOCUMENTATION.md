
# Canteen Management System - Complete System Design Documentation

## 📋 Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [ER Diagram](#er-diagram)
4. [Complete Flow Diagrams](#complete-flow-diagrams)
5. [Data Flow](#data-flow)
6. [API Gateway & Service Routing](#api-gateway--service-routing)
7. [Technology Stack](#technology-stack)

---

## 1. System Overview

The Canteen Management System is a **full-stack web application** built with:
- **Backend**: Spring Boot (Java 17) - RESTful API
- **Frontend**: React.js - Single Page Application
- **Database**: PostgreSQL - Relational Database
- **Architecture**: Monolithic Layered Architecture (No API Gateway - Direct REST calls)

### Key Features:
- Multi-customer support (Employees, Outsiders, Guests)
- Time-based meal detection
- Real-time menu management
- Order tracking and billing
- Reporting for salary deduction

---

## 2. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │              React Frontend (Port 3000)                        │ │
│  │  - WelcomeScreen.js (Customer Type Selection)                  │ │
│  │  - MenuScreen.js (Menu Display & Item Selection)               │ │
│  │  - OrderSummary.js (Cart & Checkout)                           │ │
│  │  - AdminPanel.js (Reports & Analytics)                         │ │
│  │  - api.js (HTTP Client Service)                                │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTP/REST API Calls
                                    │ (No API Gateway)
                                    ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER (Backend)                       │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │         Spring Boot Application (Port 8080)                    │ │
│  │                                                                 │ │
│  │  ┌──────────────────────────────────────────────────────────┐ │ │
│  │  │           CONTROLLER LAYER (REST APIs)                   │ │ │
│  │  │  - OrderController.java      (@RestController)           │ │ │
│  │  │  - MenuController.java       (@RestController)           │ │ │
│  │  │  - MealController.java       (@RestController)           │ │ │
│  │  │  - ReportController.java     (@RestController)           │ │ │
│  │  │  - AdminController.java      (@RestController)           │ │ │
│  │  │                                                           │ │ │
│  │  │  Endpoints: /api/orders, /api/menu, /api/meals, etc.    │ │ │
│  │  └──────────────────────────────────────────────────────────┘ │ │
│  │                            ↓                                   │ │
│  │  ┌──────────────────────────────────────────────────────────┐ │ │
│  │  │           SERVICE LAYER (Business Logic)                 │ │ │
│  │  │  - OrderService.java         (@Service)                  │ │ │
│  │  │  - MenuService.java          (@Service)                  │ │ │
│  │  │  - MealService.java          (@Service)                  │ │ │
│  │  │  - ReportService.java        (@Service)                  │ │ │
│  │  │                                                           │ │ │
│  │  │  Business Rules:                                         │ │ │
│  │  │  - Meal time validation                                  │ │ │
│  │  │  - Order calculation                                     │ │ │
│  │  │  - Customer type handling                                │ │ │
│  │  └──────────────────────────────────────────────────────────┘ │ │
│  │                            ↓                                   │ │
│  │  ┌──────────────────────────────────────────────────────────┐ │ │
│  │  │         REPOSITORY LAYER (Data Access)                   │ │ │
│  │  │  - OrderRepository.java      (JPA Repository)            │ │ │
│  │  │  - MenuRepository.java       (JPA Repository)            │ │ │
│  │  │  - MealRepository.java       (JPA Repository)            │ │ │
│  │  │  - EmployeeRepository.java   (JPA Repository)            │ │ │
│  │  │  - OrderItemsRepository.java (JPA Repository)            │ │ │
│  │  │                                                           │ │ │
│  │  │  Uses: Spring Data JPA + Hibernate ORM                   │ │ │
│  │  └──────────────────────────────────────────────────────────┘ │ │
│  │                            ↓                                   │ │
│  │  ┌──────────────────────────────────────────────────────────┐ │ │
│  │  │              ENTITY LAYER (Domain Models)                │ │ │
│  │  │  - Employee.java                                         │ │ │
│  │  │  - Meal.java                                             │ │ │
│  │  │  - Menu.java                                             │ │ │
│  │  │  - Orders.java                                           │ │ │
│  │  │  - OrderItems.java                                       │ │ │
│  │  │                                                           │ │ │
│  │  │  JPA Entities mapped to database tables                  │ │ │
│  │  └──────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ JDBC Connection
                                    ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                                  │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │              PostgreSQL Database (Port 5432)                   │ │
│  │                   Database: canteen_db                         │ │
│  │                                                                 │ │
│  │  Tables:                                                        │ │
│  │  - employee                                                     │ │
│  │  - meal                                                         │ │
│  │  - menu                                                         │ │
│  │  - orders                                                       │ │
│  │  - order_items                                                  │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. ER Diagram (Entity Relationship Diagram)

```
┌─────────────────────────┐
│      EMPLOYEE           │
├─────────────────────────┤
│ PK  id (Long)           │
│ UK  emp_id (String)     │
│     name (String)       │
│     department (String) │
│     role (Enum)         │
│       - WORKER          │
│       - STAFF           │
└─────────────────────────┘
         │ 1
         │
         │ *
         ↓
┌─────────────────────────┐         ┌─────────────────────────┐
│       ORDERS            │    *    │         MEAL            │
├─────────────────────────┤ ←─────→ ├─────────────────────────┤
│ PK  id (Long)           │    1    │ PK  id (Long)           │
│ FK  employee_id         │         │ UK  type (Enum)         │
│ FK  meal_id             │         │       - BREAKFAST       │
│ FK  host_employee_id    │         │       - LUNCH           │
│     order_time          │         │       - DINNER          │
│     total_amount        │         │     start_time (Time)   │
│     customer_type (Enum)│         │     end_time (Time)     │
│       - EMPLOYEE        │         └─────────────────────────┘
│       - OUTSIDER        │                  │ 1
│       - GUEST           │                  │
│     outsider_name       │                  │ *
│     team_name           │                  ↓
│     guest_count         │         ┌─────────────────────────┐
└─────────────────────────┘         │         MENU            │
         │ 1                        ├─────────────────────────┤
         │                          │ PK  id (Long)           │
         │ *                        │ FK  meal_id             │
         ↓                          │     item_name (String)  │
┌─────────────────────────┐         │     price (Double)      │
│     ORDER_ITEMS         │    *    │     is_active (Boolean) │
├─────────────────────────┤ ←─────→ └─────────────────────────┘
│ PK  id (Long)           │    1
│ FK  order_id            │
│ FK  menu_id             │
│     quantity (Integer)  │
│     price (Double)      │
└─────────────────────────┘

RELATIONSHIPS:
═══════════════
1. Employee (1) ──→ (0..*) Orders
   - One employee can have multiple orders
   - Orders can exist without employee (for outsiders)

2. Employee (1) ──→ (0..*) Orders (as host)
   - One employee can host multiple guest orders
   
3. Meal (1) ──→ (0..*) Orders
   - One meal can have multiple orders
   
4. Meal (1) ──→ (0..*) Menu
   - One meal has multiple menu items
   
5. Orders (1) ──→ (1..*) OrderItems
   - One order must have at least one order item
   - Cascade delete: deleting order deletes all items
   
6. Menu (1) ──→ (0..*) OrderItems
   - One menu item can be in multiple orders
```

---

## 4. Complete Flow Diagrams

### 4.1 User Signup/Login Flow (Employee ID Based)

```
┌──────────────────────────────────────────────────────────────────┐
│                    USER INTERACTION FLOW                          │
└──────────────────────────────────────────────────────────────────┘

[User Opens Application]
         │
         ↓
┌─────────────────────────┐
│  React Frontend Loads   │
│  (WelcomeScreen.js)     │
└─────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────────────┐
│              Select Customer Type                                │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                  │
│  │ EMPLOYEE │    │ OUTSIDER │    │  GUEST   │                  │
│  └──────────┘    └──────────┘    └──────────┘                  │
└─────────────────────────────────────────────────────────────────┘
         │                │                │
         ↓                ↓                ↓
    ┌─────────┐    ┌──────────┐    ┌──────────────┐
    │ Enter   │    │  Enter   │    │ Enter Host   │
    │ EMP ID  │    │  Name    │    │ EMP ID +     │
    │         │    │          │    │ Team + Count │
    └─────────┘    └──────────┘    └──────────────┘
         │                │                │
         └────────────────┴────────────────┘
                         │
                         ↓
              ┌──────────────────┐
              │ Click "Continue" │
              └──────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ Frontend Validation           │
         │ - Check required fields       │
         │ - Validate format             │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ API Call: GET /api/meals/     │
         │           current             │
         │                               │
         │ Request Flow:                 │
         │ Frontend → Backend            │
         │ (No API Gateway)              │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MealController.java           │
         │ @GetMapping("/current")       │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MealService.java              │
         │ getCurrentMeal()              │
         │ - Get current time            │
         │ - Find meal by time range     │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MealRepository.java           │
         │ findByCurrentTime()           │
         │ Query: SELECT * FROM meal     │
         │ WHERE NOW() BETWEEN           │
         │ start_time AND end_time       │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ PostgreSQL Database           │
         │ Execute Query                 │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ Return Meal Object            │
         │ {                             │
         │   id: 2,                      │
         │   type: "LUNCH",              │
         │   startTime: "12:00:00",      │
         │   endTime: "14:00:00"         │
         │ }                             │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ API Call: GET /api/menu/      │
         │           current             │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MenuController.java           │
         │ getCurrentMealMenu()          │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MenuService.java              │
         │ - Get current meal            │
         │ - Fetch menu items            │
         │ - Filter active items         │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ MenuRepository.java           │
         │ findByMealIdAndIsActiveTrue() │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ Return Menu Items             │
         │ [                             │
         │   {id: 6, name: "Dal Rice",   │
         │    price: 40.0},              │
         │   {id: 7, name: "Roti Sabzi", │
         │    price: 35.0}               │
         │ ]                             │
         └───────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │ Display Menu Screen           │
         │ (MenuScreen.js)               │
         │ - Show available items        │
         │ - Allow quantity selection    │
         └───────────────────────────────┘
```

### 4.2 Order Creation Flow

```
[User Selects Items & Quantities]
         │
         ↓
┌─────────────────────────┐
│ Add items to cart       │
│ - Item 1: Dal Rice x2   │
│ - Item 2: Salad x1      │
└─────────────────────────┘
         │
         ↓
┌─────────────────────────┐
│ Click "Place Order"     │
└─────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ Frontend Prepares Order Request                         │
│ {                                                        │
│   "customerType": "EMPLOYEE",                           │
│   "empId": "EMP001",                                    │
│   "items": [                                            │
│     {"menuId": 6, "quantity": 2},                       │
│     {"menuId": 9, "quantity": 1}                        │
│   ]                                                      │
│ }                                                        │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ API Call: POST /api/orders                              │
│ Content-Type: application/json                          │
│ Body: OrderRequest DTO                                  │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ OrderController.java                                    │
│ @PostMapping                                            │
│ createOrder(@RequestBody OrderRequest request)          │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ OrderService.java - createOrder()                       │
│                                                          │
│ STEP 1: Get Current Meal                                │
│   ↓                                                      │
│   MealService.getCurrentMeal()                          │
│   Returns: Meal object (LUNCH)                          │
│                                                          │
│ STEP 2: Validate Customer Type                          │
│   ↓                                                      │
│   Switch (customerType):                                │
│   - EMPLOYEE: Validate empId exists                     │
│   - OUTSIDER: Store outsider name                       │
│   - GUEST: Validate host empId + store details          │
│                                                          │
│ STEP 3: Fetch Employee (if EMPLOYEE type)               │
│   ↓                                                      │
│   EmployeeRepository.findByEmpId("EMP001")              │
│   Query: SELECT * FROM employee WHERE emp_id = ?        │
│   Returns: Employee object or throws exception          │
│                                                          │
│ STEP 4: Create Order Entity                             │
│   ↓                                                      │
│   Orders order = new Orders()                           │
│   order.setMeal(currentMeal)                            │
│   order.setEmployee(employee)                           │
│   order.setOrderTime(LocalDateTime.now())               │
│   order.setCustomerType(EMPLOYEE)                       │
│                                                          │
│ STEP 5: Process Order Items                             │
│   ↓                                                      │
│   For each item in request.items:                       │
│     - Fetch Menu item from MenuRepository               │
│     - Create OrderItems entity                          │
│     - Calculate item total (price × quantity)           │
│     - Add to order.orderItems list                      │
│     - Accumulate totalAmount                            │
│                                                          │
│ STEP 6: Set Total Amount                                │
│   ↓                                                      │
│   order.setTotalAmount(totalAmount)                     │
│   Example: 40×2 + 15×1 = ₹95                            │
│                                                          │
│ STEP 7: Save Order (with cascade)                       │
│   ↓                                                      │
│   OrderRepository.save(order)                           │
│   - Saves to orders table                               │
│   - Cascade saves to order_items table                  │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ Database Transactions                                    │
│                                                          │
│ BEGIN TRANSACTION;                                       │
│                                                          │
│ INSERT INTO orders (                                     │
│   employee_id, meal_id, order_time,                     │
│   total_amount, customer_type                           │
│ ) VALUES (1, 2, NOW(), 95.0, 'EMPLOYEE');               │
│                                                          │
│ -- Returns order_id = 123                               │
│                                                          │
│ INSERT INTO order_items (                               │
│   order_id, menu_id, quantity, price                    │
│ ) VALUES                                                 │
│   (123, 6, 2, 40.0),                                    │
│   (123, 9, 1, 15.0);                                    │
│                                                          │
│ COMMIT;                                                  │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ Convert to OrderResponse DTO                            │
│ {                                                        │
│   "orderId": 123,                                       │
│   "customerType": "EMPLOYEE",                           │
│   "employeeName": "John Doe",                           │
│   "mealType": "LUNCH",                                  │
│   "orderTime": "2024-01-15T13:30:00",                   │
│   "totalAmount": 95.0,                                  │
│   "items": [                                            │
│     {                                                    │
│       "itemName": "Dal Rice",                           │
│       "quantity": 2,                                    │
│       "price": 40.0,                                    │
│       "totalPrice": 80.0                                │
│     },                                                   │
│     {                                                    │
│       "itemName": "Salad",                              │
│       "quantity": 1,                                    │
│       "price": 15.0,                                    │
│       "totalPrice": 15.0                                │
│     }                                                    │
│   ]                                                      │
│ }                                                        │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ Return HTTP 201 Created                                 │
│ Response Body: OrderResponse                            │
└─────────────────────────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────────────────────────┐
│ Frontend Receives Response                              │
│ - Display Success Screen                                │
│ - Show order details                                    │
│ - Show total amount                                     │
│ - Option to place new order                             │
└─────────────────────────────────────────────────────────┘
```

### 4.3 Complete Application Flow (Venn Diagram Style)

```
┌────────────────────────────────────────────────────────────────────┐
│                    CANTEEN SYSTEM ECOSYSTEM                         │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    FRONTEND DOMAIN                            │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  User Interface Components                             │  │ │
│  │  │  - WelcomeScreen (Customer Selection)                  │  │ │
│  │  │  - MenuScreen (Item Selection)                         │  │ │
│  │  │  - OrderSummary (Cart & Checkout)                      │  │ │
│  │  │  - SuccessScreen (Confirmation)                        │  │ │
│  │  │  - AdminPanel (Reports)                                │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  │                           │                                   │ │
│  │                           │ HTTP REST API                     │ │
│  │                           ↓                                   │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  API Service Layer (api.js)                            │  │ │
│  │  │  - getCurrentMeal()                                    │  │ │
│  │  │  - getCurrentMenu()                                    │  │ │
│  │  │  - createOrder()                                       │  │ │
│  │  │  - getOrdersByEmployee()                               │  │ │
│  │  │  - getSalesReport()                                    │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                     │
│                           ║                                         │
│                           ║ Network Boundary                        │
│                           ║ (HTTP/JSON)                             │
│                           ║                                         │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    BACKEND DOMAIN                             │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  REST Controllers (@RestController)                    │  │ │
│  │  │  - OrderController (/api/orders)                       │  │ │
│  │  │  - MenuController (/api/menu)                          │  │ │
│  │  │  - MealController (/api/meals)                         │  │ │
│  │  │  - ReportController (/api/reports)                     │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  │                           │                                   │ │
│  │                           ↓                                   │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  Business Services (@Service)                          │  │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │  │ │
│  │  │  │OrderService  │  │MenuService   │  │MealService  │  │  │ │
│  │  │  │- Validation  │  │- Menu fetch  │  │- Time check │  │  │ │
│  │  │  │- Calculation │  │- Filtering   │  │- Detection  │  │  │ │
│  │  │  └──────────────┘  └──────────────┘  └─────────────┘  │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  │                           │                                   │ │
│  │                           ↓                                   │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  Data Repositories (JPA)                               │  │ │
│  │  │  - OrderRepository                                     │  │ │
│  │  │  - MenuRepository                                      │  │ │
│  │  │  - MealRepository                                      │  │ │
│  │  │  - EmployeeRepository                                  │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                     │
│                           ║                                         │
│                           ║ JDBC Connection                         │
│                           ║                                         │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    DATABASE DOMAIN                            │ │
│  │  ┌────────────────────────────────────────────────────────┐  │ │
│  │  │  PostgreSQL Tables                                     │  │ │
│  │  │  ┌──────────┐  ┌──────┐  ┌──────┐  ┌────────┐         │  │ │
│  │  │  │ employee │  │ meal │  │ menu │  │ orders │         │  │ │
│  │  │  └──────────┘  └──────┘  └──────┘  └────────┘         │  │ │
│  │  │       │            │         │           │             │  │ │
│  │  │       └────────────┴─────────┴───────────┘             │  │ │
│  │  │                      │                                 │  │ │
│  │  │                ┌─────────────┐                         │  │ │
│  │  │                │ order_items │                         │  │ │
│  │  │                └─────────────┘                         │  │ │
│  │  └────────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────┘
```

---

## 5. Data Flow

### 5.1 Request-Response Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATA FLOW DIAGRAM                             │
└─────────────────────────────────────────────────────────────────┘

USER INPUT                    PROCESSING                    OUTPUT
═══════════                   ══════════                    ══════

[Employee enters EMP001]
         │
         ↓
    ┌─────────┐
    │ JSON    │ 