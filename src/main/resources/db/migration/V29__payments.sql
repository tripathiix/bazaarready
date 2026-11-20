-- How a shop takes payment, chosen by the owner: UPI (straight to the owner's UPI ID), cash on delivery,
-- or ordering on WhatsApp. Card payments are not offered: they need each owner's own gateway account.
alter table site_extras add column pay_upi boolean not null default false;
alter table site_extras add column upi_id text;
alter table site_extras add column pay_cod boolean not null default true;
alter table site_extras add column pay_whatsapp boolean not null default false;

-- What the customer chose at checkout, and the UPI reference they gave, if any.
alter table shop_order add column payment_method text;
alter table shop_order add column payment_ref text;

-- Sample shops show cash on delivery and ordering on WhatsApp, never UPI (no real UPI ID to pay).
update site_extras set pay_whatsapp = true where business_id in (select id from business where sample);
