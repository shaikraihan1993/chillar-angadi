-- Reference lists for the admin product form's Brand and Manufacturer dropdowns.
-- products.brand / products.manufacturer stay plain text columns; these tables hold the choices.
CREATE TABLE IF NOT EXISTS brands (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_brands_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS manufacturers (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_manufacturers_name UNIQUE (name)
);

INSERT IGNORE INTO brands (name) VALUES
    ('Angadi Farm'), ('Angadi Fresh'), ('Angadi Essentials'),
    ('Aashirvaad'), ('Amul'), ('Britannia'), ('Dabur'), ('Dodla'), ('Fortune'), ('Lux'), ('MDH'),
    ('Nandini'), ('Organic India'), ('Adani Organics'), ('Patanjali'), ('Tata Sampann'), ('Tata Tea'),
    ('Vim'), ('Wildcraft');

INSERT IGNORE INTO manufacturers (name) VALUES
    ('Chillar Angadi'), ('Local farmers'),
    ('Adani Wilmar Ltd'), ('Britannia Industries Ltd'), ('Dabur India Ltd'), ('Dodla Dairy Ltd'),
    ('Gujarat Cooperative Milk Marketing Federation (Amul)'), ('Hindustan Unilever Ltd'), ('ITC Ltd'),
    ('Karnataka Milk Federation (Nandini)'), ('Mahashian Di Hatti Pvt Ltd (MDH)'), ('Organic India Pvt Ltd'),
    ('Patanjali Ayurved Ltd'), ('Tata Consumer Products Ltd'), ('Wildcraft India Ltd');

-- Anything already typed on products becomes a choice too
INSERT IGNORE INTO brands (name)
    SELECT DISTINCT TRIM(brand) FROM products WHERE brand IS NOT NULL AND TRIM(brand) <> '';
INSERT IGNORE INTO manufacturers (name)
    SELECT DISTINCT TRIM(manufacturer) FROM products WHERE manufacturer IS NOT NULL AND TRIM(manufacturer) <> '';
