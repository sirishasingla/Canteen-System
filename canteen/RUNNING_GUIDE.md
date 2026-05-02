# 🚀 How to Run the Canteen Management System

This guide will help you set up and run the complete Canteen Management System with both backend and frontend.

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

### Required Software
1. **Java 17 or higher**
   ```bash
   java -version
   ```

2. **PostgreSQL Database**
   - Version 12 or higher
   - Running on localhost:5432

3. **Node.js and npm**
   ```bash
   node -v  # Should be v14 or higher
   npm -v   # Should be v6 or higher
   ```

4. **Maven** (included via Maven Wrapper)
   - The project includes `mvnw` (Maven Wrapper), so no separate Maven installation needed

## 🗄️ Database Setup

### Step 1: Create Database

Open PostgreSQL and create the database:

```sql
-- Using psql command line
psql -U postgres

-- Create database
CREATE DATABASE canteen_db;

-- Exit psql
\q
```

Or using pgAdmin or any PostgreSQL GUI tool, create a database named `canteen_db`.

### Step 2: Configure Database Credentials

The application is configured with these default credentials in [`application.properties`](src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/canteen_db
spring.datasource.username=postgres
spring.datasource.password=admin
```

**If your PostgreSQL has different credentials**, update the file:
1. Open `src/main/resources/application.properties`
2. Change `spring.datasource.username` and `spring.datasource.password`

### Step 3: Database Tables (Auto-Created)

The application will automatically:
- ✅ Create all required tables on first run
- ✅ Load sample data (employees, meals, menu items)
- ✅ No manual SQL scripts needed!

## 🔧 Running the Backend

### Option 1: Using Maven Wrapper (Recommended)

From the project root directory (`/Users/sirishasingla/Personal_Space/Canteen-System/canteen`):

```bash
# Make the wrapper executable (first time only, macOS/Linux)
chmod +x mvnw

# Run the application
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

### Option 2: Using JAR File

```bash
# Build the JAR
./mvnw clean package

# Run the JAR
java -jar target/canteen-0.0.1-SNAPSHOT.jar
```

### Verify Backend is Running

You should see output like:
```
Started CanteenApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

**Test the backend:**
- Open browser: http://localhost:8080/api/meals
- You should see JSON response with meal data

## 🎨 Running the Frontend

### Step 1: Navigate to Frontend Directory

```bash
cd ../canteen-frontend
```

### Step 2: Install Dependencies (First Time Only)

```bash
npm install
```

This will install all required React dependencies.

### Step 3: Start the Development Server

```bash
npm start
```

The frontend will automatically:
- ✅ Start on http://localhost:3000
- ✅ Open in your default browser
- ✅ Hot-reload on code changes

### Verify Frontend is Running

You should see:
```
Compiled successfully!

You can now view canteen-frontend in the browser.

  Local:            http://localhost:3000
  On Your Network:  http://192.168.x.x:3000
```

## 🎯 Quick Start (Both Services)

### Terminal 1 - Backend
```bash
cd /Users/sirishasingla/Personal_Space/Canteen-System/canteen
./mvnw spring-boot:run
```

### Terminal 2 - Frontend
```bash
cd /Users/sirishasingla/Personal_Space/Canteen-System/canteen-frontend
npm start
```

## 🌐 Accessing the Application

Once both services are running:

### 🍽️ Kiosk Interface
- **URL**: http://localhost:3000
- **Purpose**: Customer ordering interface
- **Features**:
  - Select customer type (Employee/Outsider/Guest)
  - Browse menu
  - Place orders
  - View order confirmation

### ⚙️ Admin Panel
- **Access**: Click "⚙️ Admin" button on the welcome screen
- **Purpose**: Reports and analytics
- **Features**:
  - Sales reports
  - Employee cost analysis
  - Order history
  - Meal statistics
  - CSV export

### 🔌 Backend API
- **Base URL**: http://localhost:8080/api
- **Swagger/API Docs**: Not configured (can be added if needed)
- **Postman Collection**: [`Canteen-API-Collection.postman_collection.json`](Canteen-API-Collection.postman_collection.json)

## 📊 Sample Data

The application comes with pre-loaded sample data:

### Employees
- **EMP001** - John Doe (IT, Worker)
- **EMP002** - Jane Smith (HR, Staff)
- **EMP003** - Bob Johnson (Finance, Worker)

### Meal Times
- **Breakfast**: 8:00 AM - 9:00 AM
- **Lunch**: 3:00 PM - 5:00 PM
- **Dinner**: 8:00 PM - 9:00 PM

### Menu Items
- Breakfast: Parantha, Namkeen, Dahi, Tea, Toast
- Lunch: Dal Rice, Roti Sabzi, Biryani, Salad, Curd
- Dinner: Dal Rice, Roti Sabzi, Paneer Curry, Salad

## 🛠️ Troubleshooting

### Backend Issues

**Problem: Port 8080 already in use**
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in application.properties
server.port=8081
```

**Problem: Database connection failed**
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in `application.properties`
- Ensure database `canteen_db` exists

**Problem: Maven build fails**
```bash
# Clean and rebuild
./mvnw clean install
```

### Frontend Issues

**Problem: Port 3000 already in use**
```bash
# Kill process on port 3000
lsof -i :3000  # macOS/Linux
# Or run on different port
PORT=3001 npm start
```

**Problem: npm install fails**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

**Problem: Cannot connect to backend**
- Verify backend is running on port 8080
- Check proxy setting in `package.json`: `"proxy": "http://localhost:8080"`

## 🔄 Stopping the Application

### Stop Backend
- Press `Ctrl + C` in the terminal running the backend

### Stop Frontend
- Press `Ctrl + C` in the terminal running the frontend

## 📝 Development Mode vs Production

### Current Setup (Development)
- Backend: Spring Boot DevTools enabled
- Frontend: React development server with hot-reload
- Database: PostgreSQL with auto-schema update

### For Production Deployment
1. Build frontend: `npm run build`
2. Build backend JAR: `./mvnw clean package`
3. Deploy JAR with built frontend
4. Use production database
5. Configure proper security and CORS

## 🎓 Next Steps

1. **Test the Kiosk**: Place a test order as an employee
2. **Try Admin Panel**: Generate reports with sample data
3. **Add More Data**: Use the kiosk to create more orders
4. **Explore APIs**: Import Postman collection and test endpoints
5. **Customize**: Modify meal times, add menu items, etc.

## 📞 Support

For issues or questions:
- Check the [`QUICK_START.md`](QUICK_START.md) guide
- Review [`README.md`](README.md) for project overview
- Check terminal logs for error messages

---

**System Status Checklist:**
- [ ] PostgreSQL running on port 5432
- [ ] Database `canteen_db` created
- [ ] Backend running on port 8080
- [ ] Frontend running on port 3000
- [ ] Can access http://localhost:3000
- [ ] Can place test order
- [ ] Admin panel accessible

**Happy Ordering! 🍽️**