-- Migration: rename team_name → purpose, add company_employee_count
-- Idempotent — safe to re-run.
-- Fresh Docker DBs will run this on first boot after 01-create-tables.sql.
-- For an already-running local DB, run this once manually.

ALTER TABLE orders ADD COLUMN IF NOT EXISTS purpose VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_employee_count INTEGER;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS is_cancelled BOOLEAN DEFAULT false NOT NULL;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'orders' AND column_name = 'team_name'
  ) THEN
    UPDATE orders SET purpose = team_name WHERE team_name IS NOT NULL AND purpose IS NULL;
    ALTER TABLE orders DROP COLUMN team_name;
  END IF;
END $$;
