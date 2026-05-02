-- Clear existing menu items and reload with new items
-- Run this SQL script in your PostgreSQL database

-- Delete all existing menu items
DELETE FROM order_items;
DELETE FROM menu;

-- The application will automatically reload the new menu items on next restart
-- Just restart your Spring Boot application after running this script