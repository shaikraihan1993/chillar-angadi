-- Expand sub_category column to accommodate longer enum values
ALTER TABLE products MODIFY COLUMN sub_category VARCHAR(50);
