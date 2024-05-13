
INSERT INTO roles (role_id, authority)
VALUES (1, 'USER'),
       (2, 'EMPLOYEE'),
       (3, 'ADMIN');

INSERT INTO users (user_id, email, is_enabled) VALUES ( 100000, 'existing@example.com', true );
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100001, 'existing2@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100002, 'existing3@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100003, 'existing4@example.com', true);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number) VALUES (100003, 'Fourth', 'Existing', 'Profile', 'existing4@example.com', '+777712345600');
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100004, 'existing5@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100005, 'existing6@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100006, 'existing7@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100007, 'existing8@example.com', true);
INSERT INTO users (user_id, email, is_enabled) VALUES ( 100008, 'invited1@example.com', true);

INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100003 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100004 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100005 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100006 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100007 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 2, 100008 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100003 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100004 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100005 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100006 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100007 );
INSERT INTO user_role_junction (role_id, user_id) VALUES ( 1, 100008 );

INSERT INTO organizations(organization_id, name, registered_at, owner_id) VALUES ( 100000, 'First Organization', '2024-01-01', 100003 );
INSERT INTO organizations(organization_id, name, registered_at) VALUES ( 100001, 'Second Organization', '2024-01-01' );

INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100000, 'Position 1', 100000, 1 + 2 + 8 + 16 + 32 + 64 + 128 + 256 + 512, 0 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100001, 'Position 2', 100000, 64, 1 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100002, 'Position 3', 100000, 6, 1 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100003, 'Position 4', 100000, 32, 2 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100004, 'Position 5', 100001, 1023, 0 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100005, 'Position 6', 100001, 8, 1 );
INSERT INTO positions (position_id, title, organization_id, authorities, hierarchy) VALUES ( 100006, 'Position 7', 100000, 8, 1 );

UPDATE user_details SET organization_id = 100000, position_id = 100000 WHERE details_id = 100003;

INSERT INTO user_details (details_id, last_name, first_name, email, phone_number) VALUES (100000, 'Existing', 'Profile', 'existing@example.com', '+7999999999');
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, is_subscribed) VALUES (100001, 'Second', 'Existing', 'Profile', 'existing2@example.com', '+77771234567', true);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number) VALUES (100002, 'Third', 'Existing', 'Profile', 'existing3@example.com', '+777712345690');
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100004, 'Fifth', 'Existing', 'Profile', 'existing5@example.com', '+777712345100', 100000, 100001, 4);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100005, 'Sixth', 'Existing', 'Profile', 'existing6@example.com', '+777712345200', 100000, 100002, 4);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100006, 'Seventh', 'Existing', 'Profile', 'existing7@example.com', '+777712345300', 100000, 100003, 5);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100007, 'ZEighth', 'Existing', 'Profile', 'existing8@example.com', '+777712345400', 100001, 100003, 4);
INSERT INTO user_details (details_id, last_name, first_name, middle_name, email, phone_number, organization_id, position_id, active_orders_count) VALUES (100008, 'Invited', 'Person', 'Profile', 'invited1@example.com', '+777712341100', null, null, 0);

INSERT INTO invitations (invited_at, invitation_id, invitee_id, inviter_id, organization_id, position_id) VALUES ('2023-01-01', 100000, 100008, 100003, 100000, 100003);

-- create 10 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100000 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       CONCAT('Product ', 1 + n),
       'Example description',
       2
FROM generate_series(0, 9) AS t(n);

-- create 10 products --
INSERT INTO products (advertisement_id)
SELECT 100000 + n
FROM generate_series(0, 9) AS t(n);

-- imitate purchases for 5 products --
INSERT INTO purchases (purchase_id, purchased_by, product_id, purchased_at)
SELECT 100000 + n,
       100002,
       100000 + n,
       current_timestamp
FROM generate_series(0, 4) AS t(n);

-- create additional 10 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100010 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       concat('Order ', 1 + n),
       'Example description',
       2
FROM generate_series(0, 9) AS t(n);

-- create 10 orders and make them accepted by First Organization with id 100000 and assign to Third User with id 100002--
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status, task_key)
SELECT 100010 + n,
       100000,
       current_date - INTERVAL '1' DAY * n,
       '1',
       concat('T-1-', n + 1)
FROM generate_series(0, 9) as t(n);

INSERT INTO task_employee_junction (task_id, user_id)
SELECT 100010 + n,
       100002
FROM generate_series(0, 9) AS t(n);

-- set DONE for 4 of 10 orders that accepted by First Organization with id 100000 --
UPDATE orders
SET completed_at = current_timestamp,
    status       = '6'
WHERE advertisement_id IN (SELECT advertisement_id FROM orders LIMIT 4);

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100020 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       concat('Order ', 11 + n),
       'Example description',
       2
FROM generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by First Organization, assign to Fifth User with id 100004 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status, task_key)
VALUES (100020, 100000, current_date - INTERVAL '6' DAY, '1', 'T-1-11'),
       (100021, 100000, current_date - INTERVAL '7' DAY, '2', 'T-1-12'),
       (100022, 100000, current_date - INTERVAL '8' DAY, '3', 'T-1-13'),
       (100023, 100000, current_date - INTERVAL '9' DAY, '5', 'T-1-14'),
       (100024, 100000, current_date - INTERVAL '10' DAY, '6', 'T-1-15');

UPDATE orders SET completed_at = current_date - INTERVAL '8' DAY WHERE advertisement_id = 100024;

INSERT INTO task_employee_junction(task_id, user_id)
SELECT 100020 + n,
       100004
FROM generate_series(0, 4) AS t(n);

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100025 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       CONCAT('Order ', 16 + n),
       'Example description',
       2
FROM generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by First Organization, assign to Sixth User with id 100005 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status, task_key)
VALUES (100025, 100000, current_date - INTERVAL '11' DAY, '1', 'T-1-16'),
       (100026, 100000, current_date - INTERVAL '12' DAY, '1', 'T-1-17'),
       (100027, 100000, current_date - INTERVAL '13' DAY, '2', 'T-1-18'),
       (100028, 100000, current_date - INTERVAL '14' DAY, '3', 'T-1-19'),
       (100029, 100000, current_date - INTERVAL '15' DAY, '6', 'T-1-20');

UPDATE orders SET completed_at = current_date - INTERVAL '13' DAY WHERE advertisement_id = 100029;

INSERT INTO task_employee_junction(task_id, user_id)
SELECT 100025 + n,
       100005
FROM generate_series(0, 4) AS t(n);

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100030 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       CONCAT('Order ', 21 + n),
       'Example description',
       2
FROM generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by First Organization, assign to Seventh User with id 100006 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status, task_key)
VALUES (100030, 100000, current_date - INTERVAL '11' DAY, '1', 'T-1-21'),
       (100031, 100000, current_date - INTERVAL '21' DAY, '1', 'T-1-22'),
       (100032, 100000, current_date - INTERVAL '31' DAY, '2', 'T-1-23'),
       (100033, 100000, current_date - INTERVAL '41' DAY, '3', 'T-1-24'),
       (100034, 100000, current_date - INTERVAL '51' DAY, '3', 'T-1-25');

INSERT INTO task_employee_junction(task_id, user_id)
SELECT 100030 + n,
       100006
FROM generate_series(0, 4) AS t(n);

-- create additional 5 abstract ads --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
SELECT 100035 + n,
       current_timestamp - INTERVAL '1' SECOND * (FLOOR(RANDOM() * 31536000)),
       100001,
       CONCAT('Order ', 26 + n),
       'Example description',
       2
FROM generate_series(0, 4) AS t(n);

-- create additional 5 orders and make accepted by Second Organization assign to ZEighth User with id 100007 --
INSERT INTO orders (advertisement_id, accepted_by, accepted_at, status, task_key)
VALUES (100035, 100001, current_date - INTERVAL '1' DAY, '1', 'T-1-26'),
       (100036, 100001, current_date - INTERVAL '2' DAY, '1', 'T-1-27'),
       (100037, 100001, current_date - INTERVAL '3' DAY, '2', 'T-1-28'),
       (100038, 100001, current_date - INTERVAL '4' DAY, '3', 'T-1-29'),
       (100039, 100001, current_date - INTERVAL '5' DAY, '4', 'T-1-30');

INSERT INTO task_employee_junction(task_id, user_id)
SELECT 100035 + n,
       100007
FROM generate_series(0, 4) AS t(n);

-- create 1 order --
INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description,
                                     contact_information)
VALUES (100040, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 100001, 'Order 31', 'Example description', 2);

INSERT INTO orders (advertisement_id)
VALUES (100040);
