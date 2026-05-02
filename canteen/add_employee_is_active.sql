-- Add isActive column to employee table
-- This allows soft-delete functionality instead of hard delete

-- Add the column with default value true
ALTER TABLE employee ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true NOT NULL;

-- Set all existing employees to active
UPDATE employee SET is_active = true WHERE is_active IS NULL;

-- Verify the change
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'employee' AND column_name = 'is_active';

-- Show count of active vs inactive employees
SELECT 
    is_active,
    COUNT(*) as count
FROM employee
GROUP BY is_active;