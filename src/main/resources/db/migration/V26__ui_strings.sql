-- Builder screens translated into languages beyond the eight hand-checked ones. Keyed by the language
-- and a hash of the English words, so a new version of the page is translated again.
create table ui_strings (
    language    varchar(10)  not null,
    hash        char(64)     not null,
    strings     text         not null,
    created_at  timestamptz  not null default now(),
    primary key (language, hash)
);
