-- Manual Database Migration Script
-- Run this script in your MySQL database to migrate from firstName/lastName to fullName

-- Step 1: Connect to your MySQL database
-- mysql -u testuser -p agro_speak_db

-- Step 2: Check current table structure
DESCRIBE users;

-- Step 3: Add the new full_name column
ALTER TABLE users ADD COLUMN full_name VARCHAR(255) AFTER id;

-- Step 4: Migrate existing data by combining first_name and last_name
UPDATE users 
SET full_name = CASE 
    WHEN first_name IS NOT NULL AND last_name IS NOT NULL THEN CONCAT(first_name, ' ', last_name)
    WHEN first_name IS NOT NULL THEN first_name
    WHEN last_name IS NOT NULL THEN last_name
    ELSE NULL
END
WHERE full_name IS NULL;

-- Step 5: Verify the migration worked
SELECT id, first_name, last_name, full_name, email FROM users;

-- Step 6: After verifying everything looks correct, drop the old columns
-- IMPORTANT: Only run these after confirming the above worked correctly!
-- ALTER TABLE users DROP COLUMN first_name;
-- ALTER TABLE users DROP COLUMN last_name;

-- Step 7: Check final table structure
-- DESCRIBE users;