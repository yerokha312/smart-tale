
INSERT INTO roles (role_id, authority)
VALUES (1, 'USER'),
       (2, 'ADMIN');

INSERT INTO users (user_id, email, first_name, last_name, father_name, is_enabled, phone_number) VALUES ( 100000, 'existing@example.com', 'Existing', 'Profile', 'Example', true, '+7999999999' );
INSERT INTO users (user_id, email, first_name, last_name, father_name, is_enabled, phone_number) VALUES ( 100001, 'existing2@example.com', 'Second', 'Existing', 'Profile', true, '+77771234567' );
INSERT INTO users (user_id, email, first_name, last_name, father_name, is_enabled, phone_number) VALUES ( 100002, 'existing3@example.com', 'Third', 'Existing', 'Profile', true, '+777712345690' );

INSERT INTO user_details (details_id) VALUES (100000);
INSERT INTO user_details (details_id) VALUES (100001);
INSERT INTO user_details (details_id) VALUES (100002);

INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100000 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Product ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

INSERT INTO products (advertisement_id)
SELECT
    100000 + n,
FROM
    generate_series(0, 9) AS t(n);

UPDATE products
SET purchased_by = 100002,
    purchased_at = CURRENT_TIMESTAMP
WHERE advertisement_id IN (SELECT advertisement_id FROM products LIMIT 5);

INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100010 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

INSERT INTO orders (advertisement_id)
SELECT
    100010 + n,
FROM
    generate_series(0, 9) as t(n);

