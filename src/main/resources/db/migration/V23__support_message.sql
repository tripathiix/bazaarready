-- Messages sent through BazaarReady's own contact page (/contact). Each one is saved first and then
-- emailed to the support mailbox, so nothing is lost if the mail server is down.
create table support_message (
    id          uuid primary key,
    name        varchar(100) not null,
    email       varchar(200) not null,
    topic       varchar(40)  not null,
    message     text         not null,
    status      varchar(10)  not null default 'PENDING',  -- PENDING, SENT or FAILED (gave up after 5 tries)
    attempts    int          not null default 0,
    last_error  varchar(500),
    created_at  timestamptz  not null default now(),
    sent_at     timestamptz
);

create index support_message_unsent on support_message (created_at) where status = 'PENDING';
