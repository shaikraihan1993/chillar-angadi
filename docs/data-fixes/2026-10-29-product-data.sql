-- 2026-10-29: correct category / sub-category for every product, and fill brand + manufacturer where empty
-- (brands/manufacturers already entered by hand are kept).
-- Matches products by id AND name so it never touches the wrong row on another database.
-- Undo: 2026-10-29-product-data-UNDO.sql (values before this script ran).
START TRANSACTION;

-- Fresh produce
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_FRUITS',         brand=COALESCE(NULLIF(TRIM(brand),''), 'Organic India'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=3  AND name='Organic Apples';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='HERBS_AND_SEASONINGS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=5  AND name='Coriander';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Adani Organics'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=8  AND name='Onion';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=10 AND name='Raddish';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=11 AND name='Capsicum';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=14 AND name='Carrot';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=16 AND name='Garlic';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_VEGETABLES',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=21 AND name='Potato';
UPDATE products SET category='FRUITS_AND_VEGETABLES', sub_category='FRESH_FRUITS',         brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Fresh'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Local farmers') WHERE id=37 AND name='Lemons';

-- Dairy & eggs
UPDATE products SET category='DAIRY_AND_EGGS', sub_category='YOGURT',               brand=COALESCE(NULLIF(TRIM(brand),''), 'Dodla'),       manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Dodla Dairy Ltd') WHERE id=17 AND name='Dodla Curd';
UPDATE products SET category='DAIRY_AND_EGGS', sub_category='MILK_AND_CREAM',       brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')  WHERE id=22 AND name='Milk';
UPDATE products SET category='DAIRY_AND_EGGS', sub_category='BUTTER_AND_MARGARINE', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')  WHERE id=40 AND name='A2 Desi Cow Ghee (Bilona)';
UPDATE products SET category='DAIRY_AND_EGGS', sub_category='EGGS',                 brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')  WHERE id=54 AND name='Free-Range Country Eggs';
UPDATE products SET category='DAIRY_AND_EGGS', sub_category='MILK_AND_CREAM',       brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')  WHERE id=56 AND name='Spiced Buttermilk (Majjige)';

-- Beverages
UPDATE products SET category='BEVERAGES', sub_category='COFFEE_AND_TEA', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=20 AND name='Tea';

-- Pantry
UPDATE products SET category='PANTRY', sub_category='FLOURS_AND_ATTA', brand=COALESCE(NULLIF(TRIM(brand),''), 'Aashirvaad'),  manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'ITC Ltd')        WHERE id=26 AND name='Aashirvaad Atta';
UPDATE products SET category='PANTRY', sub_category='RICE_AND_GRAINS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=39 AND name='Unpolished Sona Masoori Rice';
UPDATE products SET category='PANTRY', sub_category='COOKING_OIL',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=41 AND name='Cold-Pressed Groundnut Oil';
UPDATE products SET category='PANTRY', sub_category='COOKING_OIL',     brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=42 AND name='Cold-Pressed Coconut Oil';
UPDATE products SET category='PANTRY', sub_category='PULSES_AND_DALS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=43 AND name='Unpolished Toor Dal';
UPDATE products SET category='PANTRY', sub_category='PULSES_AND_DALS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=44 AND name='Dal Variety Pack';
UPDATE products SET category='PANTRY', sub_category='FLOURS_AND_ATTA', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=45 AND name='Chana Besan (Gram Flour)';
UPDATE products SET category='PANTRY', sub_category='FLOURS_AND_ATTA', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=46 AND name='Ragi & Rajgira Flour Combo';
UPDATE products SET category='PANTRY', sub_category='RICE_AND_GRAINS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=47 AND name='Millet Mix (Foxtail, Little, Kodo)';
UPDATE products SET category='PANTRY', sub_category='SPICES_AND_SALT', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=48 AND name='Pink Rock Salt Crystals';
UPDATE products SET category='PANTRY', sub_category='SPICES_AND_SALT', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=49 AND name='Lakadong Turmeric Powder';
UPDATE products SET category='PANTRY', sub_category='SWEETENERS',      brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=50 AND name='Raw Wildflower Honey';
UPDATE products SET category='PANTRY', sub_category='SWEETENERS',      brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=52 AND name='Chemical-Free Jaggery Cubes';

-- Snacks
UPDATE products SET category='SNACKS', sub_category='INDIAN_SWEETS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=51 AND name='Peanut Jaggery Chikki';
UPDATE products SET category='SNACKS', sub_category='NUTS_AND_SEEDS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=53 AND name='Whole Cashews (W240)';

-- Frozen / ready to cook
UPDATE products SET category='FROZEN_FOODS', sub_category='FROZEN_MEALS', brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Farm'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi') WHERE id=55 AND name='Paneer Butter Masala Kit';

-- Household
UPDATE products SET category='HOUSEHOLD', sub_category='CLEANING_SUPPLIES',    brand=COALESCE(NULLIF(TRIM(brand),''), 'Vim'),               manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Hindustan Unilever Ltd') WHERE id=15 AND name='Vim Bar';
UPDATE products SET category='HOUSEHOLD', sub_category='STATIONERY',           brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Essentials'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')         WHERE id=12 AND name='Pen';
UPDATE products SET category='HOUSEHOLD', sub_category='STATIONERY',           brand=COALESCE(NULLIF(TRIM(brand),''), 'Angadi Essentials'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Chillar Angadi')         WHERE id=13 AND name='Pencil';
UPDATE products SET category='HOUSEHOLD', sub_category='BAGS_AND_ACCESSORIES', brand=COALESCE(NULLIF(TRIM(brand),''), 'Wildcraft'),         manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Wildcraft India Ltd')    WHERE id=38 AND name='Bag';

-- Personal care
UPDATE products SET category='PERSONAL_CARE', sub_category='BATH_AND_BODY', brand=COALESCE(NULLIF(TRIM(brand),''), 'Lux'), manufacturer=COALESCE(NULLIF(TRIM(manufacturer),''), 'Hindustan Unilever Ltd') WHERE id=2 AND name='Lux';

COMMIT;
