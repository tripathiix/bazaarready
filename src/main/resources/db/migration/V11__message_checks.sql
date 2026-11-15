-- Fake-or-real checks on messages: contact-form enquiries, and collab emails an owner pastes in.
create table message_check (
    id          uuid primary key,
    business_id uuid        not null references business (id) on delete cascade,
    enquiry_id  uuid references enquiry (id) on delete cascade,
    source      text        not null,
    sender      text,
    body        text        not null,
    status      text        not null,
    label       text,
    reasons     text,
    questions   text,
    facts       text,
    reply       text,
    error       text,
    created_at  timestamptz not null default now()
);
create index message_check_business_idx on message_check (business_id, created_at desc);
