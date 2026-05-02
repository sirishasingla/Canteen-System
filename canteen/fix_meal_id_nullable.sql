-- Fix meal_id column to allow NULL values
-- This allows orders to be created without a specific meal (for general items)

ALTER TABLE orders ALTER COLUMN meal_id DROP NOT NULL;

-- Verify the change
SELECT column_name, is_nullable, data_type 
FROM information_schema.columns 
WHERE table_name = 'orders' AND column_name = 'meal_id';