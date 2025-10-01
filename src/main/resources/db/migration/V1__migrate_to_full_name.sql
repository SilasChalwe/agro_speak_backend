-- Migration to change from firstName/lastName to fullName
-- V1__migrate_to_full_name.sql

-- Step 1: Add the new full_name column
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);

-- Step 2: Migrate existing data by combining firstName and lastName
UPDATE users 
SET full_name = CONCAT(
    IFNULL(first_name, ''), 
    CASE 
        WHEN first_name IS NOT NULL AND last_name IS NOT NULL THEN ' ' 
        ELSE '' 
    END, 
    IFNULL(last_name, '')
)
WHERE full_name IS NULL;

-- Step 3: Clean up empty full_name values (set to NULL if empty)
UPDATE users SET full_name = NULL WHERE full_name = '';

-- Step 4: Drop the old columns (uncomment these lines after verifying the migration works)
-- ALTER TABLE users DROP COLUMN first_name;
-- ALTER TABLE users DROP COLUMN last_name;