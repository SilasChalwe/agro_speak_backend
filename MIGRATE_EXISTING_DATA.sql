-- Migration script to update existing data and clean up old columns
-- This script handles the case where full_name column already exists

-- 1. First, let's see what data we have
SELECT id, email, first_name, last_name, full_name FROM users;

-- 2. Update full_name column for existing users who have first_name and/or last_name but no full_name
UPDATE users 
SET full_name = TRIM(CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, '')))
WHERE (first_name IS NOT NULL OR last_name IS NOT NULL) 
  AND (full_name IS NULL OR full_name = '');

-- 3. Clean up any double spaces or leading/trailing spaces
UPDATE users 
SET full_name = TRIM(REPLACE(full_name, '  ', ' '))
WHERE full_name LIKE '%  %';

-- 4. Show the updated data
SELECT id, email, first_name, last_name, full_name FROM users;

-- 5. After confirming data is correct, drop the old columns (commented out for safety)
-- ALTER TABLE users DROP COLUMN first_name;
-- ALTER TABLE users DROP COLUMN last_name;