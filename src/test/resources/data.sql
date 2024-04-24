
INSERT INTO roles (role_id, authority)
VALUES (1, 'USER'),
       (2, 'ADMIN');

INSERT INTO users (user_id, email, is_enabled) VALUES ( 100000, 'existing@example.com', true );
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100001, 'existing2@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100002, 'existing3@example.com', true);

INSERT INTO user_details (details_id, first_name, last_name, middle_name, email, phone_number) VALUES (100000, 'Existing', 'Profile', 'Example', 'existing@example.com', '+7999999999');
INSERT INTO user_details (details_id, first_name, last_name, middle_name, email, phone_number) VALUES (100001, 'Second', 'Existing', 'Profile', 'existing2@example.com', '+77771234567');
INSERT INTO user_details (details_id, first_name, last_name, middle_name, email, phone_number) VALUES (100002, 'Third', 'Existing', 'Profile', 'existing3@example.com', '+777712345690');

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

INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status)
SELECT
    100010 + n,
    100002,
    DATEADD('DAY', -n, CURRENT_TIMESTAMP),
    '1'
FROM
    generate_series(0, 9) as t(n);


UPDATE orders
SET completed_at = CURRENT_TIMESTAMP,
    status = '5'
WHERE advertisement_id IN (SELECT advertisement_id FROM orders LIMIT 4);


