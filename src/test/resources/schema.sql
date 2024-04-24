create table image
(
    image_id   identity,
    hash       varchar(255),
    image_name varchar(255),
    image_url  varchar(255),
    primary key (image_id),
    unique (hash)
);

create index image_hash_idx
    on image (hash);

create table organizations
(
    is_deleted      boolean,
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

create table users
(
    is_deleted boolean default false,
    is_enabled boolean default true,
    user_id    identity,
    email      varchar(255) not null,
    primary key (user_id),
    unique (email)
);

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

create table positions
(
    position_id identity,
    title       varchar(255),
    authorities int
);

create table user_details
(
    email                   varchar(255) not null,
    middle_name             varchar(255) not null,
    first_name              varchar(255) not null,
    last_name               varchar(255) not null,
    phone_number            varchar(255),
    is_subscribed           boolean default true,
    subscription_end_date   date,
    subscription_start_date date,
    details_id              bigint       not null,
    image_id                bigint,
    last_seen_at            timestamp(6),
    organization_id         bigint,
    position_id             bigint,
    registered_at           timestamp(6),
    primary key (details_id),
    unique (phone_number),
    unique (email),
    constraint fkkctijsa16thqtpecv5e7njug4
        foreign key (image_id) references image,
    constraint fkeijluvxgeb1mqhskvwflne7fu
        foreign key (organization_id) references organizations,
    constraint fkee49wu3twsclnm2pbvd3pq6n8
        foreign key (details_id) references users,
    constraint fkee49wu3twsclnm2pb3498q6n0
        foreign key (position_id) references positions
);

create table abstract_advertisements
(
    is_deleted       boolean default false,
    is_closed        boolean default false,
    price            numeric(38, 2),
    advertisement_id identity,
    published_at     timestamp(6),
    published_by     bigint,
    views            bigint  default 0,
    title            varchar(250)  not null,
    description      varchar(1000) not null,
    primary key (advertisement_id),
    constraint fk5pdy89af9tqcyu4f6iklwxg4m
        foreign key (published_by) references user_details
);

create table advertisement_image_junction
(
    advertisement_id bigint not null,
    image_id         bigint not null,
    constraint fkldye6v9oxi1swj3fs4og92xni
        foreign key (image_id) references image,
    constraint fkd9jrdlwb5q5t0ugxpgva4d9r0
        foreign key (advertisement_id) references abstract_advertisements
);

create table orders
(
    status           smallint,
    accepted_at      date,
    accepted_by      bigint,
    advertisement_id bigint not null,
    completed_at     date,
    deadline_at      date,
    size             varchar(255),
    primary key (advertisement_id),
    constraint fkfc4g9w56mvotr4du2iitbvc17
        foreign key (advertisement_id) references abstract_advertisements,
    constraint fk43920hf23408gh20h2v20fj92
        foreign key (accepted_by) references user_details,
    constraint orders_status_check
        check ((status >= 0) AND (status <= 5))
);

alter table organizations
    add constraint fkk2l1oiw4hjj0vt19sexvru55x
        foreign key (owner_id) references user_details;

create table products
(
    advertisement_id bigint not null,
    purchased_at     timestamp(6),
    purchased_by     bigint,
    primary key (advertisement_id),
    constraint fk5g5vs5tt1b74vcp32t3e8hfx4
        foreign key (purchased_by) references user_details,
    constraint fkrlasy6vsu39rymr339s3esa6p
        foreign key (advertisement_id) references abstract_advertisements
);

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

