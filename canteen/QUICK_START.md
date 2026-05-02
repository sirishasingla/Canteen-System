# Quick Start Guide - Canteen Management System

## Prerequisites Checklist
- ✅ Java 17 installed
- ✅ PostgreSQL installed and running
- ✅ Maven installed (or use included mvnw)

## Step-by-Step Setup

### 1. Database Setup (5 minutes)

Open PostgreSQL terminal or pgAdmin and run:

```sql
-- Create database
CREATE DATABASE canteen_db;

-- Verify database is created
\l
```

### 2. Configure Application (2 minutes)

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

**Default values:**
- Username: `postgres`
- Password: `postgres`
- Database: `canteen_db`
- Port: `5432`

### 3. Build & Run (3 minutes)

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

**Expected Output:**
```
Started CanteenApplication in X.XXX seconds
Sample employees initialized
Meal times initialized
Menu items initialized
```

### 4. Verify Installation (2 minutes)

Open browser or Postman and test:

```
http://localhost:8080/api/meals
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "type": "BREAKFAST",
    "startTime": "08:00:00",
    "endTime": "09:00:00"
  },
  ...
]
```

## Sample Data Included

### Employees
- **EMP001** - John Doe (IT, Worker)
- **EMP002** - Jane Smith (HR, Staff)
- **EMP003** - Bob Johnson (Finance, Worker)

### Meals
- **Breakfast**: 8:00 AM - 9:00 AM
- **Lunch**: 12:00 PM - 2:00 PM
- **Dinner**: 8:00 PM - 9:00 PM

### Menu Items

**Breakfast:**
- Parantha (₹10)
- Namkeen (₹5)
- Dahi (₹5)
- Tea (₹5)
- Toast (₹10)

**Lunch:**
- Dal Rice (₹40)
- Roti Sabzi (₹35)
- Biryani (₹60)
- Salad (₹15)
- Curd (₹10)

**Dinner:**
- Dal Rice (₹40)
- Roti Sabzi (₹35)
- Paneer Curry (₹50)
- Salad (₹15)

## Quick API Tests

### Test 1: Get Current Menu
```bash
curl http://localhost:8080/api/menu/current
```

### Test 2: Create Employee Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "EMPLOYEE",
    "empId": "EMP001",
    "items": [
      {"menuId": 1, "quantity": 2},
      {"menuId": 2, "quantity": 1}
    ]
  }'
```

### Test 3: Get Employee Orders
```bash
curl http://localhost:8080/api/orders/employee/EMP001
```

### Test 4: Get Monthly Total
```bash
curl "http://localhost:8080/api/orders/employee/EMP001/total?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"
```

## Import Postman Collection

1. Open Postman
2. Click **Import**
3. Select `Canteen-API-Collection.postman_collection.json`
4. All API endpoints will be ready to test!

## Common Issues & Solutions

### Issue 1: Port 8080 already in use
**Solution:** Change port in `application.properties`:
```properties
server.port=8081
```

### Issue 2: Database connection failed
**Solution:** 
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in `application.properties`
- Ensure database `canteen_db` exists

### Issue 3: "No meal is currently being served"
**Solution:** This is expected if you test outside meal times. Either:
- Wait for meal time (8-9 AM, 12-2 PM, or 8-9 PM)
- Or modify meal times in database
- Or use specific meal ID endpoint: `/api/menu/meal/1`

### Issue 4: Compilation errors
**Solution:**
```bash
./mvnw clean install -U
```

## Next Steps

1. ✅ **Test all APIs** using Postman collection
2. ✅ **Create sample orders** for different customer types
3. ✅ **Generate reports** using date range queries
4. ✅ **Explore the code** to understand the architecture

## Development Tips

### Hot Reload
Use Spring Boot DevTools for automatic restart:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### View SQL Queries
Already enabled in `application.properties`:
```properties
spring.jpa.show-sql=true
```

### Database GUI Tools
- **pgAdmin** - Full-featured PostgreSQL GUI
- **DBeaver** - Universal database tool
- **DataGrip** - JetBrains database IDE

## Support

For issues or questions:
1. Check the main [README.md](README.md)
2. Review API documentation
3. Check application logs in console

## Success Checklist

- [ ] Database created and connected
- [ ] Application starts without errors
- [ ] Sample data loaded successfully
- [ ] Can fetch meals via API
- [ ] Can fetch menu via API
- [ ] Can create orders via API
- [ ] Can retrieve orders via API

**Congratulations! Your Canteen Management System is ready to use! 🎉**