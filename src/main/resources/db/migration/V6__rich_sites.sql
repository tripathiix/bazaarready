-- Multi-page sites with orders, bookings and enquiries.

-- A private link for the owner to see orders and messages. Hard to guess; never shown on the public site.
alter table business add column owner_key uuid not null default gen_random_uuid();
create unique index business_owner_key_idx on business (owner_key);

-- Offerings can be grouped, and a transport route has a start, an end and a schedule.
alter table product add column category text;
alter table product add column origin text;
alter table product add column destination text;
alter table product add column schedule text;

-- The fixed words on a site (buttons, headings), translated once per language and reused.
create table site_labels (
    language   text primary key,
    labels     text        not null,
    created_at timestamptz not null default now()
);

-- An order from a shop's cart, or a booking request for a service.
create table shop_order (
    id             uuid primary key,
    business_id    uuid        not null references business (id) on delete cascade,
    kind           text        not null,
    customer_name  text        not null,
    customer_phone text        not null,
    address        text,
    note           text,
    booking_date   date,
    booking_time   text,
    items          text        not null,
    total_paise    bigint      not null default 0,
    status         text        not null,
    created_at     timestamptz not null default now()
);
create index shop_order_business_idx on shop_order (business_id, created_at desc);

-- A message sent through a site's contact form.
create table enquiry (
    id          uuid primary key,
    business_id uuid        not null references business (id) on delete cascade,
    name        text        not null,
    contact     text        not null,
    message     text        not null,
    created_at  timestamptz not null default now()
);
create index enquiry_business_idx on enquiry (business_id, created_at desc);
