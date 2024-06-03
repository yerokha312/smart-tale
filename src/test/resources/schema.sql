create table image
(
    image_id   bigserial,
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
    founded_at      date,
    is_deleted      boolean default false,
    registered_at   date,
    image_id        bigint,
    organization_id bigserial,
    owner_id        bigint,
    description     varchar(255),
    name            varchar(255) not null,
    primary key (organization_id),
    unique (image_id),
    unique (owner_id),
    unique (name),
    constraint fklhp1fun3rd75x1yv43puat7c1
        foreign key (image_id) references image
);

create table positions
(
    authorities     integer default 0,
    organization_id bigint,
    hierarchy       integer default 0,
    position_id     bigserial,
    title           varchar(255),
    primary key (position_id),
    constraint fkcayns9gtx5dfrugp6jyw0y8kx
        foreign key (organization_id) references organizations
);

create table roles
(
    role_id   bigserial,
    authority varchar(255),
    primary key (role_id)
);

create table users
(
    is_deleted boolean default false,
    is_enabled boolean default false,
    is_invited boolean default false,
    user_id    bigserial,
    email      varchar(255) not null,
    primary key (user_id),
    unique (email)
);

create table refresh_token
(
    is_revoked boolean,
    expires_at timestamp(6) with time zone,
    issued_at  timestamp(6) with time zone,
    token_id   bigserial,
    user_id    bigint,
    token      varchar(1000),
    primary key (token_id),
    unique (token),
    constraint fkjtx87i0jvq2svedphegvdwcuy
        foreign key (user_id) references users
);

create table user_details
(
    active_orders_count     integer default 0,
    is_subscribed           boolean default false,
    subscription_end_date   date,
    subscription_start_date date,
    details_id              bigint       not null,
    image_id                bigint,
    last_seen_at            timestamp(6),
    organization_id         bigint,
    position_id             bigint,
    registered_at           timestamp(6),
    email                   varchar(255) not null,
    first_name              varchar(255),
    last_name               varchar(255),
    middle_name             varchar(255),
    phone_number            varchar(255),
    primary key (details_id),
    unique (email),
    unique (phone_number),
    constraint fkkctijsa16thqtpecv5e7njug4
        foreign key (image_id) references image,
    constraint fkeijluvxgeb1mqhskvwflne7fu
        foreign key (organization_id) references organizations,
    constraint fk8x5k73xp6tncp2nr9pfvgi1wg
        foreign key (position_id) references positions,
    constraint fkee49wu3twsclnm2pbvd3pq6n8
        foreign key (details_id) references users
);

create table abstract_advertisements
(
    contact_information smallint,
    is_closed           boolean default false,
    is_deleted          boolean default false,
    advertisement_id    bigserial,
    published_at        timestamp(6),
    published_by        bigint,
    views               bigint  default 0,
    title               varchar(250)  not null,
    description         varchar(1000) not null,
    primary key (advertisement_id),
    constraint fk5pdy89af9tqcyu4f6iklwxg4m
        foreign key (published_by) references user_details,
    constraint abstract_advertisements_contact_information_check
        check ((contact_information >= 0) AND (contact_information <= 2))
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

create table invitations
(
    invited_at      timestamp(6),
    invitation_id   bigserial,
    invitee_id      bigint,
    inviter_id      bigint,
    organization_id bigint,
    position_id     bigint,
    primary key (invitation_id),
    constraint fkfu00oeldfg13nmla8p48uya43
        foreign key (invitee_id) references user_details,
    constraint fk51wagt9i90tnmgx7bcm9lseuc
        foreign key (inviter_id) references user_details,
    constraint fkq0jssk151g1kt9cx4vnomojc9
        foreign key (organization_id) references organizations,
    constraint fk6nmsyb8t1b7g9ec2cibtwajw5
        foreign key (position_id) references positions
);

create table orders
(
    accepted_at      date,
    completed_at     date,
    deadline_at      date,
    price               numeric(38, 2),
    status           smallint,
    accepted_by      bigint,
    advertisement_id bigint not null,
    comment          varchar(255),
    size             varchar(255),
    task_key         varchar(255),
    primary key (advertisement_id),
    constraint fko7oqmrhjyu78foflci8xhw9u5
        foreign key (accepted_by) references organizations,
    constraint fkfc4g9w56mvotr4du2iitbvc17
        foreign key (advertisement_id) references abstract_advertisements,
    constraint orders_status_check
        check ((status >= 0) AND (status <= 7))
);

create table acceptance_requests
(
    requested_at    date,
    acceptance_id   bigserial,
    order_id        bigint,
    organization_id bigint,
    primary key (acceptance_id),
    constraint fkjvm2vwu8wep26unujk966supx
        foreign key (order_id) references orders,
    constraint fk80ke2kc0c60mi4bvw4yjx472c
        foreign key (organization_id) references organizations
);

alter table organizations
    add constraint fkk2l1oiw4hjj0vt19sexvru55x
        foreign key (owner_id) references user_details;

create table products
(
    price               numeric(38, 2),
    advertisement_id bigint not null,
    primary key (advertisement_id),
    constraint fkrlasy6vsu39rymr339s3esa6p
        foreign key (advertisement_id) references abstract_advertisements
);

create table purchases
(
    product_id   bigint,
    purchase_id  bigserial,
    purchased_at timestamp(6),
    purchased_by bigint,
    primary key (purchase_id),
    constraint fkcacbvw28fu31rv1vrhnkcbe28
        foreign key (product_id) references products,
    constraint fkfnlesww8h07n1mt79mb90vrvf
        foreign key (purchased_by) references user_details
);

create table task_employee_junction
(
    task_id bigint not null,
    user_id bigint not null,
    constraint fkdy3oyqgyom36c2uxqmuh6ygut
        foreign key (task_id) references orders,
    constraint fk3gwkdfroec44jsbpvtrmvxejk
        foreign key (user_id) references user_details
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

create table notifications
(
    notification_id bigint                      not null
        primary key,
    data            json                        not null,
    is_read         boolean                     not null,
    is_sent         boolean                     not null,
    recipient_id    bigint                      not null,
    recipient_type  varchar(255)                not null
        CONSTRAINT notifications_recipient_type_check CHECK (recipient_type IN ('ORGANIZATION', 'USER')),
    timestamp       timestamp(6) with time zone not null
);
create sequence notifications_notification_id_seq
    increment by 5;
create index recipient_idx
    on notifications (recipient_id);

create table jobs
(
    application_deadline date,
    job_type             varchar(255)
        constraint jobs_job_type_check
            check (job_type IN
                   ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN', 'TEMPORARY')),
    location             varchar(255),
    salary               numeric(38, 2),
    advertisement_id     bigint not null
        primary key
        constraint fk6d8msioynvly32v7ooxycbkxp
            references abstract_advertisements,
    organization_id      bigint
        constraint fkrj84ptwt9tksbcnduv0fo8t0r
            references organizations
);

create table applications
(
    application_id   bigint generated by default as identity
        constraint pk_applications
            primary key,
    job_id           bigint not null
        constraint fk_applications_on_job
            references jobs,
    user_id          bigint not null
        constraint fk_applications_on_user
            references user_details,
    application_date timestamp,
    status           varchar(255)
);






