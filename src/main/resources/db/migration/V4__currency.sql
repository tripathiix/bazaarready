-- Prices are no longer assumed to be in rupees. Each business has its own currency symbol.
alter table business add column currency text not null default '₹';
