-- Update lunch time to 3-5 PM
UPDATE meal SET start_time = '15:00:00', end_time = '17:00:00' WHERE type = 'LUNCH';

-- Verify the update
SELECT id, type, start_time, end_time FROM meal;