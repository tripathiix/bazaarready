-- A deep test sends every AI customer that fits the site, not just ten. It is limited per day, so the
-- run is marked.
alter table test_run add column deep boolean not null default false;
