-- Five additions to every generated site: the living shutter, haggle mode, two-language chat,
-- a creator's media kit, and the coming-soon waitlist with a price test (plus the AI focus group).

-- One row per site with the owner's settings for these features. Created on first change.
create table site_extras (
    business_id     uuid primary key references business (id) on delete cascade,
    -- Living shutter
    shop_open       boolean      not null default true,
    closed_note     varchar(200),
    auto_hours      boolean      not null default false,
    open_at         time,
    close_at        time,
    time_zone       varchar(40)  not null default 'Asia/Kolkata',
    -- Haggle mode: the lowest price is list price minus max_off percent, enforced in code
    haggle_on       boolean      not null default false,
    haggle_max_off  int          not null default 10,
    haggle_min_qty  int          not null default 1,
    -- Creator media kit as JSON: {"platforms":[{name,handle,followers,views}], "audience": "..."}
    media_kit       text,
    -- Coming soon: a waitlist instead of orders, and up to three prices shown to different visitors
    coming_soon     boolean      not null default false,
    price_options   varchar(100),
    updated_at      timestamptz  not null default now()
);

-- A price a shopper bargained for. Checkout uses it only while it is unexpired and unused.
create table haggle_deal (
    id               uuid primary key,
    business_id      uuid        not null references business (id) on delete cascade,
    product_id       uuid        not null,
    quantity         int         not null,
    unit_price_minor bigint      not null,
    list_price_minor bigint      not null,
    used             boolean     not null default false,
    created_at       timestamptz not null default now(),
    expires_at       timestamptz not null
);

-- Two-language chat: each shopper gets a private thread, found again by its secret token.
create table chat_thread (
    id                uuid primary key,
    business_id       uuid         not null references business (id) on delete cascade,
    token             uuid         not null unique,
    shopper_name      varchar(80),
    shopper_language  varchar(20)  not null,
    owner_unread      int          not null default 0,
    created_at        timestamptz  not null default now(),
    last_at           timestamptz  not null default now()
);
create index chat_thread_business on chat_thread (business_id, last_at desc);

create table chat_message (
    id          bigserial primary key,
    thread_id   uuid         not null references chat_thread (id) on delete cascade,
    sender      varchar(10)  not null,  -- SHOPPER or OWNER
    original    text         not null,
    translated  text,                   -- in the other side's language; null when both share one
    created_at  timestamptz  not null default now()
);
create index chat_message_thread on chat_message (thread_id, id);

-- Coming-soon sign-ups, with the price that visitor was shown.
create table waitlist_signup (
    id           bigserial primary key,
    business_id  uuid         not null references business (id) on delete cascade,
    name         varchar(80)  not null,
    contact      varchar(120) not null,
    price_seen   varchar(20),
    created_at   timestamptz  not null default now()
);
create index waitlist_business on waitlist_signup (business_id, created_at desc);

-- AI focus group results: who the personas were and what they said, as JSON.
create table focus_group (
    id           uuid primary key,
    business_id  uuid        not null references business (id) on delete cascade,
    result       text        not null,
    created_at   timestamptz not null default now()
);

-- Shop samples in the Templates gallery can be bargained with, so visitors can try haggle mode there.
insert into site_extras (business_id, haggle_on, haggle_max_off, haggle_min_qty)
select id, true, 12, 1 from business where sample and kind in ('shop', 'food')
on conflict (business_id) do nothing;
