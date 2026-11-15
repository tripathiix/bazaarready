-- AI test customers: a run, one case per customer, and the problems they found.

create table test_run (
    id          uuid primary key,
    business_id uuid        not null references business (id) on delete cascade,
    status      text        not null,
    started_at  timestamptz not null default now(),
    finished_at timestamptz
);
create index test_run_business_idx on test_run (business_id, started_at desc);

create table test_case (
    id         uuid primary key,
    run_id     uuid    not null references test_run (id) on delete cascade,
    position   integer not null,
    persona    text    not null,
    goal       text    not null,
    status     text    not null,
    summary    text,
    steps      text    not null default ''
);

create table test_finding (
    id           uuid primary key,
    run_id       uuid not null references test_run (id) on delete cascade,
    persona      text not null,
    problem      text not null,
    evidence     text,
    suggestion   text,
    faq_question text,
    faq_answer   text,
    fix_status   text not null
);
create index test_finding_run_idx on test_finding (run_id);
