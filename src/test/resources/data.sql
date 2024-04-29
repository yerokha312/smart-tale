
INSERT INTO roles (role_id, authority)
VALUES (1, 'USER'),
       (2, 'ADMIN');

INSERT INTO users (user_id, email, is_enabled) VALUES ( 100000, 'existing@example.com', true );
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100001, 'existing2@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100002, 'existing3@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100003, 'existing4@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100004, 'existing5@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100005, 'existing6@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100006, 'existing7@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100007, 'existing8@example.com', true);

INSERT INTO organizations(organization_id, name) VALUES ( 100000, 'Test Org' );
INSERT INTO organizations(organization_id, name) VALUES ( 100001, 'Second Test Org' );

INSERT INTO positions (position_id, title, organization_id) VALUES ( 100000, 'PositionEntity 1', 100000 );
INSERT INTO positions (position_id, title, organization_id) VALUES ( 100001, 'PositionEntity 2', 100000 );
INSERT INTO positions (position_id, title, organization_id) VALUES ( 100002, 'PositionEntity 3', 100000 );
INSERT INTO positions (position_id, title, organization_id) VALUES ( 100003, 'PositionEntity 4', 100000 );
INSERT INTO positions (position_id, title, organization_id) VALUES ( 100004, 'PositionEntity 5', 100001 );
INSERT INTO positions (position_id, title, organization_id) VALUES ( 100005, 'PositionEntity 6', 100001 );

INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number) VALUES (100000, 'Existing', 'Profile', 'Example', 'existing@example.com', '+7999999999');
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number) VALUES (100001, 'Second', 'Existing', 'Profile', 'existing2@example.com', '+77771234567');
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number) VALUES (100002, 'Third', 'Existing', 'Profile', 'existing3@example.com', '+777712345690');
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id) VALUES (100003, 'Fourth', 'Existing', 'Profile', 'existing4@example.com', '+777712345600', 100000, 100000);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100004, 'Fifth', 'Existing', 'Profile', 'existing5@example.com', '+777712345100', 100000, 100001, 3);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100005, 'Sixth', 'Existing', 'Profile', 'existing6@example.com', '+777712345200', 100000, 100002, 4);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100006, 'Seventh', 'Existing', 'Profile', 'existing7@example.com', '+777712345300', 100000, 100003, 5);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100007, 'ZEighth', 'Existing', 'Profile', 'existing8@example.com', '+777712345400', 100001, 100003, 4);




-- create 10 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100000 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Product ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

-- create 10 products --
INSERT INTO products (advertisement_id)
SELECT
    100000 + n,
FROM
    generate_series(0, 9) AS t(n);

-- imitate purchases for 5 products --
UPDATE abstract_advertisements
SET    purchased_at = CURRENT_TIMESTAMP
WHERE advertisement_id IN (SELECT advertisement_id FROM abstract_advertisements LIMIT 5);

UPDATE products
SET purchased_by = 100002
WHERE advertisement_id IN (SELECT advertisement_id FROM products LIMIT 5);

-- create additional 10 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100010 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

-- create 10 orders and make them accepted by Third User with id 100002 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status)
SELECT
    100010 + n,
    100002,
    DATEADD('DAY', -n, CURRENT_TIMESTAMP),
    '1'
FROM
    generate_series(0, 9) as t(n);

-- set completed for 4 of 10 orders that accepted by Third User with id 100002 --
UPDATE orders
SET completed_at = CURRENT_TIMESTAMP,
    status = '5'
WHERE advertisement_id IN (SELECT advertisement_id FROM orders LIMIT 4);

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100020 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 11 + n),
    'Example description'
FROM
    generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by Fifth User with id 100004 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status) VALUES
    ( 100020, 100004, DATEADD('DAY', -1, CURRENT_TIMESTAMP), '0'),
    ( 100021, 100004, DATEADD('DAY', -2, CURRENT_TIMESTAMP), '1'),
    ( 100022, 100004, DATEADD('DAY', -3, CURRENT_TIMESTAMP), '2'),
    ( 100023, 100004, DATEADD('DAY', -4, CURRENT_TIMESTAMP), '4'),
    ( 100024, 100004, DATEADD('DAY', -5, CURRENT_TIMESTAMP), '5');

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100025 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 15 + n),
    'Example description'
FROM
    generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by Sixth User with id 100005 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status) VALUES
    ( 100025, 100005, DATEADD('DAY', -1, CURRENT_TIMESTAMP), '0'),
    ( 100026, 100005, DATEADD('DAY', -2, CURRENT_TIMESTAMP), '1'),
    ( 100027, 100005, DATEADD('DAY', -3, CURRENT_TIMESTAMP), '2'),
    ( 100028, 100005, DATEADD('DAY', -4, CURRENT_TIMESTAMP), '3'),
    ( 100029, 100005, DATEADD('DAY', -5, CURRENT_TIMESTAMP), '5');

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100030 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 20 + n),
    'Example description'
FROM
    generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by Seventh User with id 100006 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status) VALUES
    ( 100030, 100006, DATEADD('DAY', -1, CURRENT_TIMESTAMP), '0'),
    ( 100031, 100006, DATEADD('DAY', -2, CURRENT_TIMESTAMP), '1'),
    ( 100032, 100006, DATEADD('DAY', -3, CURRENT_TIMESTAMP), '2'),
    ( 100033, 100006, DATEADD('DAY', -4, CURRENT_TIMESTAMP), '3'),
    ( 100034, 100006, DATEADD('DAY', -5, CURRENT_TIMESTAMP), '3');

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100035 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 25 + n),
    'Example description'
FROM
    generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by Eighth User with id 100007 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status) VALUES
    ( 100035, 100007, DATEADD('DAY', -1, CURRENT_TIMESTAMP), '0'),
    ( 100036, 100007, DATEADD('DAY', -2, CURRENT_TIMESTAMP), '1'),
    ( 100037, 100007, DATEADD('DAY', -3, CURRENT_TIMESTAMP), '2'),
    ( 100038, 100007, DATEADD('DAY', -4, CURRENT_TIMESTAMP), '3'),
    ( 100039, 100007, DATEADD('DAY', -5, CURRENT_TIMESTAMP), '4');

