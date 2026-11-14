-- The language the owner picked on the start screen. The agent speaks it and the site is written in it.
alter table agent_session add column language text;

-- Sites are no longer only shops. A site has a kind (shop, services, classes, portfolio and so on),
-- a colour theme, an "about" paragraph and optional timings and location.
alter table business add column kind text not null default 'shop';
alter table business add column theme text not null default 'marigold';
alter table business add column about text;
alter table business add column location text;
alter table business add column hours text;
alter table business add column offerings_title text;
alter table business add column details_title text;

-- An offering does not always have a fixed price (a portfolio piece, "price on request").
alter table product alter column price_paise drop not null;
alter table product add column price_note text;
alter table product add column description text;
