-- Four 3D designs join the seven page layouts: globe, poppy, gradient and neon. Each gets a sample site
-- for the Templates gallery, rendered by the real site engine like the others.

insert into business (id, name, slug, tagline, owner_language, source, status, published_at, sample,
                      kind, theme, layout, currency, about, location, hours, offerings_title, hero_query, sections, delivery_notes)
values
('5a3e0000-0000-4000-8000-000000000008', 'Atlas Spice Exports', 'atlas-spice-exports',
 'Kerala spices, shipped to kitchens in 40 countries', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'shop', 'ocean', 'globe', '$',
 'Atlas is a family spice house in Kochi. We buy straight from growers in the Western Ghats, clean and pack every lot by hand, and ship by air to home cooks and restaurants around the world.',
 'Fort Kochi, Kerala, India', 'Orders ship Mon to Fri within 48 hours', 'Our spices', 'spice market',
 '[{"title":"Worldwide shipping","body":"Air shipping to 40 countries.\nTracked parcels, 5 to 9 days.\nFree shipping on orders over $60.","style":"list"}]',
 'Tracked air shipping worldwide'),

('5a3e0000-0000-4000-8000-000000000009', 'Bloom & Bake', 'bloom-and-bake',
 'Loud, happy cupcakes baked fresh every morning', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'food', 'marigold', 'poppy', '₹',
 'Bloom & Bake is a tiny bakery in Indiranagar run by two sisters. We bake cupcakes, brownies and party cakes in bright colours, with eggless options for everything.',
 'Indiranagar, Bengaluru, India', 'Daily, 10 am to 9 pm', 'Fresh today', 'cupcakes',
 '[{"title":"Party orders","body":"Boxes of 12 or more need one day notice. Custom cakes need two days.","style":"highlight"}]',
 'Delivery across east Bengaluru, ₹49'),

('5a3e0000-0000-4000-8000-000000000010', 'Serene Skin Studio', 'serene-skin-studio',
 'Calm, science-led skin care in the heart of the city', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'services', 'rose', 'gradient', 'S$',
 'Serene is a small skin studio in Singapore. Our therapists combine gentle facials, LED light and simple home routines to help your skin feel like yours again.',
 'Tanjong Pagar, Singapore', 'Tue to Sun, 11 am to 8 pm', 'Treatments', 'spa treatment',
 '[{"title":"Your first visit","body":"Every first visit starts with a free 15-minute skin consultation.","style":"highlight"}]',
 null),

('5a3e0000-0000-4000-8000-000000000011', 'Vinyl Vault', 'vinyl-vault-berlin',
 'Records, turntables and late-night listening sessions', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'shop', 'indigo', 'neon', '€',
 'Vinyl Vault is a record shop in Kreuzberg. We sell new and used vinyl, restored turntables and headphones, and host listening nights every Friday.',
 'Kreuzberg, Berlin, Germany', 'Tue to Sat, 12:00 to 22:00', 'In the crates', 'vinyl records',
 '[{"title":"Friday listening nights","body":"Free entry from 20:00. Bring a record you love and we play it on the big system.","style":"highlight"}]',
 'Shipping across the EU, €6');

insert into product (id, business_id, name, price_paise, unit, price_note, category, description, image_query, sort_order) values
-- Atlas Spice Exports (globe)
('5a3e0001-0000-4000-8000-000000000801', '5a3e0000-0000-4000-8000-000000000008', 'Green cardamom', 1800, '100 g', null, 'Whole spices', 'Bold 8 mm pods from Idukki, full of oil and aroma.', 'cardamom', 1),
('5a3e0001-0000-4000-8000-000000000802', '5a3e0000-0000-4000-8000-000000000008', 'Tellicherry black pepper', 1200, '200 g', null, 'Whole spices', 'Large, sun-dried peppercorns with a deep, warm heat.', 'black pepper', 2),
('5a3e0001-0000-4000-8000-000000000803', '5a3e0000-0000-4000-8000-000000000008', 'True cinnamon', 1400, '100 g', null, 'Whole spices', 'Thin, sweet quills, hand-rolled.', 'cinnamon sticks', 3),
('5a3e0001-0000-4000-8000-000000000804', '5a3e0000-0000-4000-8000-000000000008', 'Turmeric powder', 900, '250 g', null, 'Ground spices', 'High-curcumin Alleppey turmeric, stone-ground.', 'turmeric', 4),
('5a3e0001-0000-4000-8000-000000000805', '5a3e0000-0000-4000-8000-000000000008', 'Whole cloves', 1100, '100 g', null, 'Whole spices', 'Plump, hand-picked cloves.', 'cloves spice', 5),
('5a3e0001-0000-4000-8000-000000000806', '5a3e0000-0000-4000-8000-000000000008', 'Spice discovery box', 4900, 'box', null, 'Gift boxes', 'Eight spices in glass jars with a recipe card for each.', 'spices jars', 6),
-- Bloom & Bake (poppy)
('5a3e0001-0000-4000-8000-000000000901', '5a3e0000-0000-4000-8000-000000000009', 'Rainbow cupcakes', 54000, 'box of 6', null, 'Cupcakes', 'Vanilla sponge with swirls of six-colour buttercream.', 'colorful cupcakes', 1),
('5a3e0001-0000-4000-8000-000000000902', '5a3e0000-0000-4000-8000-000000000009', 'Red velvet cupcakes', 60000, 'box of 6', null, 'Cupcakes', 'Cocoa red velvet with cream cheese frosting.', 'red velvet cupcake', 2),
('5a3e0001-0000-4000-8000-000000000903', '5a3e0000-0000-4000-8000-000000000009', 'Fudge brownies', 45000, 'box of 6', null, 'Brownies', 'Dense, gooey and very chocolatey.', 'brownies', 3),
('5a3e0001-0000-4000-8000-000000000904', '5a3e0000-0000-4000-8000-000000000009', 'Party cake', 120000, 'kg', null, 'Cakes', 'Choose any flavour and colour, with your message on top.', 'birthday cake', 4),
('5a3e0001-0000-4000-8000-000000000905', '5a3e0000-0000-4000-8000-000000000009', 'Glazed donuts', 39000, 'box of 6', null, 'Treats', 'Soft yeast donuts with pink, chocolate and lemon glaze.', 'donuts', 5),
('5a3e0001-0000-4000-8000-000000000906', '5a3e0000-0000-4000-8000-000000000009', 'Sprinkle cookies', 25000, 'box of 8', null, 'Treats', 'Butter cookies rolled in rainbow sprinkles.', 'sugar cookies', 6),
-- Serene Skin Studio (gradient)
('5a3e0001-0000-4000-8000-000000001001', '5a3e0000-0000-4000-8000-000000000010', 'Signature glow facial', 16800, '75 min', null, 'Facials', 'Deep cleanse, gentle peel, massage and a hydrating mask.', 'facial treatment', 1),
('5a3e0001-0000-4000-8000-000000001002', '5a3e0000-0000-4000-8000-000000000010', 'LED light therapy', 9800, '40 min', null, 'Facials', 'Red and blue light to calm redness and breakouts.', 'led light therapy', 2),
('5a3e0001-0000-4000-8000-000000001003', '5a3e0000-0000-4000-8000-000000000010', 'Hydra boost', 13800, '60 min', null, 'Facials', 'Layers of moisture for dry, tired skin.', 'skincare serum', 3),
('5a3e0001-0000-4000-8000-000000001004', '5a3e0000-0000-4000-8000-000000000010', 'Back and shoulder massage', 8800, '45 min', null, 'Body', 'Warm oil massage to release tension.', 'massage', 4),
('5a3e0001-0000-4000-8000-000000001005', '5a3e0000-0000-4000-8000-000000000010', 'Skin consultation', null, null, 'Free', 'Consultations', 'Fifteen minutes to understand your skin and goals.', 'skin care', 5),
-- Vinyl Vault (neon)
('5a3e0001-0000-4000-8000-000000001101', '5a3e0000-0000-4000-8000-000000000011', 'New release LPs', 2800, 'record', 'from', 'Vinyl', 'This week''s new pressings in electronic, jazz and soul.', 'vinyl record', 1),
('5a3e0001-0000-4000-8000-000000001102', '5a3e0000-0000-4000-8000-000000000011', 'Used classics', 1200, 'record', 'from', 'Vinyl', 'Graded, cleaned and sleeved second-hand records.', 'record store', 2),
('5a3e0001-0000-4000-8000-000000001103', '5a3e0000-0000-4000-8000-000000000011', 'Restored turntable', 24000, null, 'from', 'Gear', 'Vintage decks, serviced in our workshop with a one-year guarantee.', 'turntable', 3),
('5a3e0001-0000-4000-8000-000000001104', '5a3e0000-0000-4000-8000-000000000011', 'Studio headphones', 9900, null, null, 'Gear', 'Closed-back headphones for long listening.', 'headphones', 4),
('5a3e0001-0000-4000-8000-000000001105', '5a3e0000-0000-4000-8000-000000000011', 'Gift card', 2500, null, 'from', 'Gifts', 'For the record lover who has everything.', 'gift card', 5),
('5a3e0001-0000-4000-8000-000000001106', '5a3e0000-0000-4000-8000-000000000011', 'Record cleaning kit', 1900, 'kit', null, 'Gear', 'Brush, fluid and anti-static sleeves.', 'vinyl cleaning', 6);

insert into faq (id, business_id, question, answer, sort_order) values
('5a3e0002-0000-4000-8000-000000000801', '5a3e0000-0000-4000-8000-000000000008', 'Do you ship to my country?', 'We ship to 40 countries, including the US, UK, EU, UAE, Singapore and Australia.', 1),
('5a3e0002-0000-4000-8000-000000000802', '5a3e0000-0000-4000-8000-000000000008', 'Do you sell to restaurants?', 'Yes. Ask us for bulk prices from 5 kg.', 2),
('5a3e0002-0000-4000-8000-000000000901', '5a3e0000-0000-4000-8000-000000000009', 'Is everything eggless?', 'Everything can be made eggless. Just choose it when you order.', 1),
('5a3e0002-0000-4000-8000-000000000902', '5a3e0000-0000-4000-8000-000000000009', 'Can I pick up?', 'Yes, from our Indiranagar shop any time we are open.', 2),
('5a3e0002-0000-4000-8000-000000001001', '5a3e0000-0000-4000-8000-000000000010', 'Is it suitable for sensitive skin?', 'Yes. We patch-test and use fragrance-free products for sensitive skin.', 1),
('5a3e0002-0000-4000-8000-000000001002', '5a3e0000-0000-4000-8000-000000000010', 'Can I reschedule?', 'Of course, up to 24 hours before your visit.', 2),
('5a3e0002-0000-4000-8000-000000001101', '5a3e0000-0000-4000-8000-000000000011', 'Do you buy records?', 'Yes. Bring them in on Tuesdays and we make an offer on the spot.', 1),
('5a3e0002-0000-4000-8000-000000001102', '5a3e0000-0000-4000-8000-000000000011', 'Do you ship turntables?', 'Within Germany only, fully insured.', 2);

-- The shop samples can be bargained with, like the other shop samples.
insert into site_extras (business_id, haggle_on, haggle_max_off, haggle_min_qty)
select id, true, 12, 1 from business where id in ('5a3e0000-0000-4000-8000-000000000008', '5a3e0000-0000-4000-8000-000000000009', '5a3e0000-0000-4000-8000-000000000011')
on conflict (business_id) do nothing;
