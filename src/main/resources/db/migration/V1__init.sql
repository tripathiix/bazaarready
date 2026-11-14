-- Day one schema: the business profile and the agent's activity log.
-- Money is stored in paise as integers.

create table business (
    id              uuid primary key,
    name            text        not null,
    slug            text        not null unique,
    tagline         text,
    owner_language  text        not null default 'hi',
    whatsapp_number text,
    delivery_notes  text,
    notice_days     integer,
    reply_style     text,
    source          text        not null,
    status          text        not null default 'DRAFT',
    published_at    timestamptz,
    created_at      timestamptz not null default now()
);

create table product (
    id          uuid primary key,
    business_id uuid    not null references business (id) on delete cascade,
    name        text    not null,
    price_paise bigint  not null,
    unit        text,
    note        text,
    sort_order  integer not null default 0
);
create index product_business_idx on product (business_id);

create table faq (
    id          uuid primary key,
    business_id uuid    not null references business (id) on delete cascade,
    question    text    not null,
    answer      text    not null,
    times_asked integer not null default 0,
    source      text    not null default 'OWNER',
    sort_order  integer not null default 0
);
create index faq_business_idx on faq (business_id);

create table agent_action (
    id             uuid primary key,
    business_id    uuid        not null references business (id) on delete cascade,
    run_id         uuid        not null,
    step_no        integer     not null,
    tool           text        not null,
    input          jsonb,
    result_summary text,
    status         text        not null,
    created_at     timestamptz not null default now()
);
create index agent_action_business_idx on agent_action (business_id, created_at);
