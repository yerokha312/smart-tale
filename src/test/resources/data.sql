
INSERT INTO roles (role_id, authority)
values (1, 'USER'),
       (2, 'ADMIN');

INSERT INTO users (user_id, email, first_name, last_name, father_name, is_enabled, phone_number) VALUES ( 100000, 'existing@example.com', 'Existing', 'Profile', 'Example', true, '+7999999999' );
INSERT INTO users (user_id, email, first_name, last_name, father_name, is_enabled, phone_number) VALUES ( 100001, 'existing2@example.com', 'Second', 'Existing', 'Profile', true, '+77771234567' );


insert into user_details (details_id) values (100000);
insert into user_details (details_id) values (100001);

INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100000 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Product ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

insert into products (advertisement_id)
SELECT
    100000 + n,
FROM
    generate_series(0, 9) AS t(n);

INSERT INTO abstract_advertisements (advertisement_id, published_at, published_by, title, description)
SELECT
    100010 + n,
    DATEADD('SECOND', RAND() * 31536000, '2000-01-01 00:00:00'),
    100001,
    CONCAT('Order ', 1 + n),
    'Example description'
FROM
    generate_series(0, 9) AS t(n);

insert into orders (advertisement_id)
select
    100010 + n,
from
    generate_series(0, 9) as t(n);

