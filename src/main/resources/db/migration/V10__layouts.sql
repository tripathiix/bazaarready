-- Sites can use one of four page layouts. Existing sites get one that suits their kind of business.
alter table business add column layout text not null default 'classic';

update business set layout = case
    when kind = 'food' then 'boutique'
    when kind in ('services', 'classes', 'transport') then 'split'
    when kind in ('portfolio', 'events') then 'magazine'
    else 'classic'
end;
