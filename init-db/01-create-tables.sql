-- Canteen Management System - Database Initialization Script
-- This script creates all necessary tables and types for the canteen system

-- Create ENUM types
CREATE TYPE customer_type AS ENUM ('EMPLOYEE', 'OUTSIDER', 'GUEST');
CREATE TYPE employee_role AS ENUM ('WORKER', 'STAFF');
CREATE TYPE meal_type AS ENUM ('BREAKFAST', 'SNACKS', 'LUNCH', 'DINNER');

-- Create Admin User table (for authenticated admin panel access)
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_admin_user_username ON admin_user(username);

-- Create Employee table
CREATE TABLE IF NOT EXISTS employee (
    id BIGSERIAL PRIMARY KEY,
    emp_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    role employee_role NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Meal table
CREATE TABLE IF NOT EXISTS meal (
    id BIGSERIAL PRIMARY KEY,
    type meal_type UNIQUE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Menu table
CREATE TABLE IF NOT EXISTS menu (
    id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    staff_price DECIMAL(10, 2) CHECK (staff_price IS NULL OR staff_price >= 0),
    worker_price DECIMAL(10, 2) CHECK (worker_price IS NULL OR worker_price >= 0),
    outsider_price DECIMAL(10, 2) CHECK (outsider_price IS NULL OR outsider_price >= 0),
    is_active BOOLEAN DEFAULT true,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_menu_item UNIQUE (item_name)
);

-- Join table: which meals a menu item is served during (empty = always available)
CREATE TABLE IF NOT EXISTS menu_meal (
    menu_id BIGINT NOT NULL REFERENCES menu(id) ON DELETE CASCADE,
    meal_id BIGINT NOT NULL REFERENCES meal(id) ON DELETE CASCADE,
    PRIMARY KEY (menu_id, meal_id)
);

-- Create Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employee(id) ON DELETE SET NULL,
    meal_id BIGINT REFERENCES meal(id) ON DELETE SET NULL,
    order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    customer_type customer_type NOT NULL,
    outsider_name VARCHAR(100),
    host_employee_id BIGINT REFERENCES employee(id) ON DELETE SET NULL,
    purpose VARCHAR(255),
    guest_count INTEGER CHECK (guest_count > 0),
    company_employee_count INTEGER CHECK (company_employee_count > 0),
    is_cancelled BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Order_Items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menu(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_order_menu UNIQUE (order_id, menu_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_employee_emp_id ON employee(emp_id);
CREATE INDEX idx_employee_is_active ON employee(is_active);
CREATE INDEX idx_menu_is_active ON menu(is_active);
CREATE INDEX idx_menu_meal_menu ON menu_meal(menu_id);
CREATE INDEX idx_menu_meal_meal ON menu_meal(meal_id);
CREATE INDEX idx_orders_employee_id ON orders(employee_id);
CREATE INDEX idx_orders_meal_id ON orders(meal_id);
CREATE INDEX idx_orders_order_time ON orders(order_time);
CREATE INDEX idx_orders_customer_type ON orders(customer_type);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_id ON order_items(menu_id);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_employee_updated_at BEFORE UPDATE ON employee
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meal_updated_at BEFORE UPDATE ON meal
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_updated_at BEFORE UPDATE ON menu
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;