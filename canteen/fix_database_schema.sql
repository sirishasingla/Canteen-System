-- Fix database schema to allow null meal_id in menu table
-- Run this in PostgreSQL before restarting the application

-- First, delete existing menu items
DELETE FROM order_items;
DELETE FROM menu;

-- Alter the menu table to allow null meal_id
ALTER TABLE menu ALTER COLUMN meal_id DROP NOT NULL;

-- Verify the change
\d menu

-- Now restart your Spring Boot application and it will load the new menu items