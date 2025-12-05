-- Make user_id column nullable in orders table
ALTER TABLE orders MODIFY COLUMN user_id BIGINT NULL;
