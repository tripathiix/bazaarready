-- Stock photos can now come from Wikimedia Commons as well as Pexels; Commons photos carry a licence to show.
alter table stock_photo add column source text not null default 'Pexels';
alter table stock_photo add column licence text;
alter table stock_photo add column licence_url text;
