-- Clear existing employees and load complete employee master list
-- Total: 486 employees from Employee Master.xlsx

-- First, disable foreign key constraints temporarily
SET session_replication_role = 'replica';

-- Clear existing employees
TRUNCATE TABLE employee CASCADE;

-- Re-enable foreign key constraints
SET session_replication_role = 'origin';

-- Insert all 486 employees (split into multiple INSERT statements for reliability)
