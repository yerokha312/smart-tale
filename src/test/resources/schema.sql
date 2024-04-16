create table image
(
    image_id   identity,
    hash       varchar(255),
    image_name varchar(255),
    image_url  varchar(255),
    primary key (image_id)
);

create index image_hash_idx
    on image (hash);

create table organizations
(
    is_deleted         boolean,
    founded_at      timestamp(6),
    image_id        bigint,
    organization_id identity,
    owner_id        bigint,
    registered_at   timestamp(6),
    description     varchar(255),
    name            varchar(255) not null,
    primary key (organization_id),
    unique (image_id),
    unique (owner_id),
    unique (name),
    constraint fklhp1fun3rd75x1yv43puat7c1
        foreign key (image_id) references image
);

create table roles
(
    role_id   identity,
    authority varchar(255),
    primary key (role_id)
);

INSERT INTO roles (role_id, authority)
values (1, 'USER'),
       (2, 'ADMIN');

create table users
(
    is_deleted   boolean default false,
    is_enabled   boolean default false,
    user_id      identity,
    email        varchar(255) not null,
    father_name  varchar(255),
    first_name   varchar(255) not null,
    last_name    varchar(255) not null,
    phone_number varchar(255),
    primary key (user_id),
    unique (email),
    unique (phone_number)
);

INSERT INTO users (user_id, email, first_name, last_name, father_name) VALUES ( 100000, 'existing@example.com', 'Existing', 'User', 'Example' );

create table refresh_token
(
    is_revoked boolean,
    expires_at timestamp(6) with time zone,
    issued_at  timestamp(6) with time zone,
    token_id   identity,
    user_id    bigint,
    token      varchar(1000),
    primary key (token_id),
    unique (token),
    constraint fkjtx87i0jvq2svedphegvdwcuy
        foreign key (user_id) references users
);

create table user_details
(
    is_subscribed           boolean,
    subscription_end_date   date,
    subscription_start_date date,
    details_id              bigint not null,
    image_id                bigint,
    last_seen_at            timestamp(6),
    organization_id         bigint,
    registered_at           timestamp(6),
    primary key (details_id),
    constraint fkkctijsa16thqtpecv5e7njug4
        foreign key (image_id) references image,
    constraint fkeijluvxgeb1mqhskvwflne7fu
        foreign key (organization_id) references organizations,
    constraint fkee49wu3twsclnm2pbvd3pq6n8
        foreign key (details_id) references users
);

create table equipments
(
    is_deleted       boolean,
    is_hidden        boolean,
    price            numeric(38, 2),
    advertisement_id identity,
    published_at     timestamp(6),
    purchased_at     timestamp(6),
    purchased_by     bigint,
    user_id          bigint,
    views            bigint,
    title            varchar(250)  not null,
    description      varchar(1000) not null,
    primary key (advertisement_id),
    constraint fko6s9hlrfen0hq6xbcxyvbfn1p
        foreign key (user_id) references user_details,
    constraint fk4j3g4ue90oyy0f7gruud3xx53
        foreign key (purchased_by) references user_details
);

create table orders
(
    is_deleted       boolean,
    is_hidden        boolean,
    price            numeric(38, 2),
    status           smallint,
    accepted_at      timestamp(6),
    advertisement_id identity,
    deadline_at      timestamp(6),
    published_at     timestamp(6),
    user_id          bigint,
    views            bigint,
    title            varchar(250)  not null,
    description      varchar(1000) not null,
    size             varchar(255),
    primary key (advertisement_id),
    constraint fkfjwlaf81pfw2i5btnbk0636b7
        foreign key (user_id) references user_details,
    constraint orders_status_check
        check ((status >= 0) AND (status <= 5))
);

create table advertisement_image_junction
(
    advertisement_id bigint not null,
    image_id         bigint not null,
    constraint fkldye6v9oxi1swj3fs4og92xni
        foreign key (image_id) references image,
    constraint fkde2j95onpa5pnf4hora5qcb9q
        foreign key (advertisement_id) references orders
);

alter table organizations
    add constraint fkk2l1oiw4hjj0vt19sexvru55x
        foreign key (owner_id) references user_details;

create table user_role_junction
(
    role_id bigint not null,
    user_id bigint not null,
    primary key (role_id, user_id),
    constraint fkhybpcwvq8snjhbxawo25hxous
        foreign key (role_id) references roles,
    constraint fk5aqfsa7i8mxrr51gtbpcvp0v1
        foreign key (user_id) references users
);

