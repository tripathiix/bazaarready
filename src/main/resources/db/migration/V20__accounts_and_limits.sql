-- Owner accounts. Passwords are stored only as BCrypt hashes.
create table account (
    id            uuid primary key,
    email         text not null unique,
    name          text not null,
    password_hash text not null,
    created_at    timestamptz not null default now()
);

-- Each site and each agent conversation belongs to the account that made it.
-- Older sites have no account yet; the first signed-in person who opens their private owner link claims them.
alter table business add column account_id uuid references account (id);
alter table agent_session add column account_id uuid references account (id);
create index business_account_idx on business (account_id);

-- Calls to Claude per day, per account ('' for work with no signed-in person, such as photo checks),
-- so a daily spending cap can protect the API credits.
create table claude_usage (
    day     date not null,
    account text not null,
    calls   integer not null default 0,
    primary key (day, account)
);
