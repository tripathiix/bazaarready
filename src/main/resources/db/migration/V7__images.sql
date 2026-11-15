-- Pictures on generated sites.

-- What to search stock photos for: one query for the site's banner, one per offering.
alter table business add column hero_query text;
alter table product add column image_query text;

-- Stock photos already looked up, so each search is made once and shared between sites.
create table stock_photo (
    query            text primary key,
    found            boolean     not null,
    image_url        text,
    thumb_url        text,
    photographer     text,
    photographer_url text,
    page_url         text,
    fetched_at       timestamptz not null default now()
);

-- Photos an owner uploads. Keyed by offering name (empty for the banner), so they survive the
-- agent rewriting the profile.
create table item_photo (
    id           uuid primary key,
    business_id  uuid        not null references business (id) on delete cascade,
    item_name    text        not null,
    content_type text        not null,
    data         bytea       not null,
    created_at   timestamptz not null default now(),
    unique (business_id, item_name)
);
