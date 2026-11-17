-- Built-in sample sites for the Templates gallery: one fictional business per design,
-- rendered by the real site engine. Sample sites never appear in dashboards or the
-- "recently published" gallery, and their order, booking and contact forms do not submit.

alter table business add column sample boolean not null default false;

insert into business (id, name, slug, tagline, owner_language, source, status, published_at, sample,
                      kind, theme, layout, currency, about, location, hours, offerings_title, hero_query, sections, delivery_notes)
values
('5a3e0000-0000-4000-8000-000000000001', 'Green Valley Farm Store', 'green-valley-farm-store',
 'Fresh from our fields to your kitchen, every morning', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'shop', 'forest', 'classic', '₹',
 'We are a family farm on the edge of Pune. We grow vegetables without chemical sprays, keep our own cows, and pack every order the same morning it is picked.',
 'Hinjewadi, Pune, India', 'Mon to Sat, 7 am to 1 pm', 'From our farm', 'vegetable farm',
 '[{"title":"How ordering works","body":"Add what you need to the cart and send the order.\nWe confirm by phone the same day.\nDelivery the next morning across west Pune.\nPay on delivery by UPI or cash.","style":"list"},{"title":"Weekend offer","body":"Free delivery on orders over ₹500 every Saturday and Sunday.","style":"highlight"}]',
 'Next-morning delivery across west Pune'),

('5a3e0000-0000-4000-8000-000000000002', 'Maple & Crumb', 'maple-and-crumb',
 'Small-batch bakes from a home kitchen in Toronto', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'food', 'rose', 'boutique', '$',
 'Maple & Crumb is a one-person home bakery. Everything is baked to order with butter, real vanilla and local flour, for birthdays, office treats and slow Sunday mornings.',
 'Leslieville, Toronto, Canada', 'Orders Tue to Sat, pickup 10 am to 6 pm', 'Our bakes', 'bakery pastries',
 '[{"title":"Order ahead","body":"Cakes need two days notice. Cookies and pastries can be ready the next day.","style":"highlight"},{"title":"Our story","body":"I started baking for neighbours during long Toronto winters. Today Maple & Crumb bakes for families across the east end, still from the same small kitchen.","style":"text"}]',
 'Pickup in Leslieville, or local delivery for $8'),

('5a3e0000-0000-4000-8000-000000000003', 'Kirana Plus', 'kirana-plus',
 'Your neighbourhood store, now open online', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'shop', 'marigold', 'mart', '₹',
 'Kirana Plus has served HSR Layout for twelve years. Order daily groceries, staples and snacks online and we deliver within two hours.',
 'HSR Layout, Bengaluru, India', 'Every day, 7 am to 10 pm', 'Shop by aisle', 'grocery store',
 '[{"title":"Two-hour delivery","body":"Free delivery within 3 km on orders over ₹299.","style":"highlight"}]',
 'Delivery within 2 hours, free over ₹299'),

('5a3e0000-0000-4000-8000-000000000004', 'Lakshmi Maths Classes', 'lakshmi-maths-mysuru',
 'Patient maths coaching for classes 6 to 12', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'classes', 'indigo', 'split', '₹',
 'Lakshmi has taught maths for fifteen years. Small groups of six, weekly tests and doubt-clearing sessions help students build real confidence before board exams.',
 'Kuvempunagar, Mysuru, India', 'Mon to Sat, 4 pm to 8 pm', 'Classes', 'mathematics classroom',
 '[{"title":"How it works","body":"Book a free first class.\nLakshmi checks where your child is with a short test.\nJoin a batch of up to six students.\nGet a progress note every month.","style":"list"}]',
 null),

('5a3e0000-0000-4000-8000-000000000005', 'Studio Nomad', 'studio-nomad',
 'Brand and web design for small, brave businesses', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'services', 'slate', 'bento', '€',
 'Studio Nomad is a two-person design studio in Lisbon. We help cafes, makers and new founders look as good as what they make, from the first logo to a full website.',
 'Alfama, Lisbon, Portugal', 'Mon to Fri, 9:30 to 18:00 (WET)', 'What we do', 'design studio',
 '[{"title":"How we work","body":"A free 30-minute call to understand your business.\nA fixed quote within two days.\nTwo rounds of changes included in every project.","style":"list"}]',
 null),

('5a3e0000-0000-4000-8000-000000000006', 'Farah Frames', 'farah-frames-nairobi',
 'Wedding, portrait and travel photography', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'portfolio', 'slate', 'magazine', 'KSh',
 'I am Farah, a photographer based in Nairobi. I photograph weddings, families and the landscapes of East Africa, with a quiet, documentary style.',
 'Westlands, Nairobi, Kenya', 'Shoots by appointment, seven days a week', 'My work', 'safari landscape',
 '[{"title":"Booking a shoot","body":"Tell me the date, place and kind of shoot. I reply within a day with availability and a quote.","style":"highlight"}]',
 null),

('5a3e0000-0000-4000-8000-000000000007', 'Velvet Salon', 'velvet-salon',
 'Hair, skin and calm, in the heart of Dubai', 'en', 'DESCRIPTION', 'PUBLISHED', now(), true,
 'services', 'rose', 'noir', 'AED',
 'Velvet is a quiet, appointment-only salon. Our stylists specialise in colour, keratin care and bridal looks, using gentle, professional products.',
 'Jumeirah, Dubai, UAE', 'Daily, 10 am to 9 pm', 'Treatments', 'hair salon',
 '[{"title":"First visit","body":"Enjoy 15% off your first treatment when you book online.","style":"highlight"}]',
 null);

insert into product (id, business_id, name, price_paise, unit, price_note, category, description, image_query, sort_order) values
-- Green Valley Farm Store (classic)
('5a3e0001-0000-4000-8000-000000000101', '5a3e0000-0000-4000-8000-000000000001', 'Tomatoes', 6000, 'kg', null, 'Vegetables', 'Vine-ripened, picked the morning of delivery.', 'tomatoes', 1),
('5a3e0001-0000-4000-8000-000000000102', '5a3e0000-0000-4000-8000-000000000001', 'Spinach', 3000, 'bunch', null, 'Vegetables', 'Tender leaves, washed and tied.', 'spinach', 2),
('5a3e0001-0000-4000-8000-000000000103', '5a3e0000-0000-4000-8000-000000000001', 'Carrots', 5000, 'kg', null, 'Vegetables', 'Sweet orange carrots, great raw or cooked.', 'carrots', 3),
('5a3e0001-0000-4000-8000-000000000104', '5a3e0000-0000-4000-8000-000000000001', 'Fresh cow milk', 7000, 'litre', null, 'Dairy', 'From our own cows, delivered chilled in glass bottles.', 'milk bottle', 4),
('5a3e0001-0000-4000-8000-000000000105', '5a3e0000-0000-4000-8000-000000000001', 'Farm eggs', 9000, 'dozen', null, 'Dairy', 'Free-range eggs from our hens.', 'eggs', 5),
('5a3e0001-0000-4000-8000-000000000106', '5a3e0000-0000-4000-8000-000000000001', 'Honey', 45000, '500 g jar', null, 'Pantry', 'Raw honey from hives on our farm.', 'honey jar', 6),
-- Maple & Crumb (boutique)
('5a3e0001-0000-4000-8000-000000000201', '5a3e0000-0000-4000-8000-000000000002', 'Chocolate layer cake', 4800, null, 'serves 10', 'Cakes', 'Three layers of dark chocolate sponge with silky ganache.', 'chocolate cake', 1),
('5a3e0001-0000-4000-8000-000000000202', '5a3e0000-0000-4000-8000-000000000002', 'Maple pecan tart', 3200, null, 'serves 8', 'Cakes', 'Buttery crust, Ontario maple syrup and toasted pecans.', 'pecan pie', 2),
('5a3e0001-0000-4000-8000-000000000203', '5a3e0000-0000-4000-8000-000000000002', 'Butter croissants', 1800, 'box of 6', null, 'Pastries', 'Laminated over three days for crisp, flaky layers.', 'croissant', 3),
('5a3e0001-0000-4000-8000-000000000204', '5a3e0000-0000-4000-8000-000000000002', 'Cinnamon rolls', 2200, 'box of 6', null, 'Pastries', 'Soft rolls with brown sugar cinnamon and cream cheese icing.', 'cinnamon roll', 4),
('5a3e0001-0000-4000-8000-000000000205', '5a3e0000-0000-4000-8000-000000000002', 'Chocolate chip cookies', 1500, 'dozen', null, 'Cookies', 'Brown butter dough and big pools of chocolate.', 'chocolate chip cookies', 5),
('5a3e0001-0000-4000-8000-000000000206', '5a3e0000-0000-4000-8000-000000000002', 'Sourdough loaf', 900, 'loaf', null, 'Bread', 'Slow-fermented for 24 hours with a crackling crust.', 'sourdough bread', 6),
-- Kirana Plus (mart)
('5a3e0001-0000-4000-8000-000000000301', '5a3e0000-0000-4000-8000-000000000003', 'Basmati rice', 14500, '1 kg', null, 'Staples', 'Long-grain aged basmati.', 'basmati rice', 1),
('5a3e0001-0000-4000-8000-000000000302', '5a3e0000-0000-4000-8000-000000000003', 'Toor dal', 16000, '1 kg', null, 'Staples', 'Unpolished split pigeon peas.', 'lentils', 2),
('5a3e0001-0000-4000-8000-000000000303', '5a3e0000-0000-4000-8000-000000000003', 'Atta', 32000, '5 kg', null, 'Staples', 'Whole wheat flour for soft rotis.', 'wheat flour', 3),
('5a3e0001-0000-4000-8000-000000000304', '5a3e0000-0000-4000-8000-000000000003', 'Bananas', 6000, 'dozen', null, 'Fruit and vegetables', 'Ripe yelakki bananas.', 'bananas', 4),
('5a3e0001-0000-4000-8000-000000000305', '5a3e0000-0000-4000-8000-000000000003', 'Onions', 4000, 'kg', null, 'Fruit and vegetables', 'Firm red onions.', 'onions', 5),
('5a3e0001-0000-4000-8000-000000000306', '5a3e0000-0000-4000-8000-000000000003', 'Masala chai tea', 22000, '500 g', null, 'Drinks', 'Strong CTC tea for everyday chai.', 'tea leaves', 6),
('5a3e0001-0000-4000-8000-000000000307', '5a3e0000-0000-4000-8000-000000000003', 'Coffee powder', 18000, '250 g', null, 'Drinks', 'South Indian filter coffee blend.', 'coffee beans', 7),
('5a3e0001-0000-4000-8000-000000000308', '5a3e0000-0000-4000-8000-000000000003', 'Spice box', 39900, 'set', null, 'Spices', 'Turmeric, chilli, cumin, coriander, mustard, garam masala and cardamom.', 'spices', 8),
-- Lakshmi Maths Classes (split)
('5a3e0001-0000-4000-8000-000000000401', '5a3e0000-0000-4000-8000-000000000004', 'Classes 6 to 8 foundation', 150000, 'month', null, 'Group batches', 'Three classes a week, building strong basics in arithmetic and algebra.', 'classroom', 1),
('5a3e0001-0000-4000-8000-000000000402', '5a3e0000-0000-4000-8000-000000000004', 'Class 10 board preparation', 250000, 'month', null, 'Group batches', 'Full syllabus, past papers and a weekly mock test.', 'students studying', 2),
('5a3e0001-0000-4000-8000-000000000403', '5a3e0000-0000-4000-8000-000000000004', 'Class 12 calculus', 300000, 'month', null, 'Group batches', 'Limits, derivatives and integrals, with plenty of practice.', 'mathematics', 3),
('5a3e0001-0000-4000-8000-000000000404', '5a3e0000-0000-4000-8000-000000000004', 'One-to-one session', 80000, 'hour', null, 'Personal', 'Focused help on any topic, in person or online.', 'tutoring', 4),
('5a3e0001-0000-4000-8000-000000000405', '5a3e0000-0000-4000-8000-000000000004', 'Free trial class', null, null, 'Free', 'Personal', 'Meet Lakshmi and try a class before you join.', 'chalkboard', 5),
-- Studio Nomad (bento)
('5a3e0001-0000-4000-8000-000000000501', '5a3e0000-0000-4000-8000-000000000005', 'Logo and brand kit', 120000, null, 'from', 'Branding', 'Logo, colours, fonts and a one-page brand guide.', 'logo design', 1),
('5a3e0001-0000-4000-8000-000000000502', '5a3e0000-0000-4000-8000-000000000005', 'Website design', 250000, null, 'from', 'Web', 'A fast, simple website of up to five pages.', 'web design', 2),
('5a3e0001-0000-4000-8000-000000000503', '5a3e0000-0000-4000-8000-000000000005', 'Packaging design', 90000, null, 'from', 'Branding', 'Labels and boxes that stand out on the shelf.', 'packaging', 3),
('5a3e0001-0000-4000-8000-000000000504', '5a3e0000-0000-4000-8000-000000000005', 'Social media kit', 45000, null, 'from', 'Content', 'Twelve post templates matched to your brand.', 'smartphone', 4),
('5a3e0001-0000-4000-8000-000000000505', '5a3e0000-0000-4000-8000-000000000005', 'Design day', 60000, 'day', null, 'Content', 'A full day of our time for anything you need.', 'laptop desk', 5),
-- Farah Frames (magazine)
('5a3e0001-0000-4000-8000-000000000601', '5a3e0000-0000-4000-8000-000000000006', 'Wedding day', 18000000, null, 'from', 'Weddings', 'Full-day coverage with 400 edited photos.', 'wedding', 1),
('5a3e0001-0000-4000-8000-000000000602', '5a3e0000-0000-4000-8000-000000000006', 'Family portraits', 2500000, 'session', null, 'Portraits', 'One hour outdoors, 40 edited photos.', 'family portrait', 2),
('5a3e0001-0000-4000-8000-000000000603', '5a3e0000-0000-4000-8000-000000000006', 'Safari photo tour', 9000000, 'day', null, 'Travel', 'A day in Nairobi National Park, with tips on your own camera.', 'giraffe', 3),
('5a3e0001-0000-4000-8000-000000000604', '5a3e0000-0000-4000-8000-000000000006', 'Fine art prints', 650000, 'print', null, 'Prints', 'Signed landscape prints on archival paper.', 'landscape photography', 4),
('5a3e0001-0000-4000-8000-000000000605', '5a3e0000-0000-4000-8000-000000000006', 'Business headshots', 1200000, 'person', null, 'Portraits', 'Clean, natural headshots at your office.', 'portrait photography', 5),
-- Velvet Salon (noir)
('5a3e0001-0000-4000-8000-000000000701', '5a3e0000-0000-4000-8000-000000000007', 'Cut and blow-dry', 25000, null, null, 'Hair', 'Consultation, wash, precision cut and styling.', 'haircut', 1),
('5a3e0001-0000-4000-8000-000000000702', '5a3e0000-0000-4000-8000-000000000007', 'Full colour', 55000, null, 'from', 'Hair', 'Rich, even colour with a gloss finish.', 'hair dye', 2),
('5a3e0001-0000-4000-8000-000000000703', '5a3e0000-0000-4000-8000-000000000007', 'Keratin treatment', 90000, null, 'from', 'Hair', 'Smooth, frizz-free hair for up to three months.', 'hair', 3),
('5a3e0001-0000-4000-8000-000000000704', '5a3e0000-0000-4000-8000-000000000007', 'Signature facial', 40000, null, null, 'Skin', 'Seventy minutes of cleansing, massage and hydration.', 'facial', 4),
('5a3e0001-0000-4000-8000-000000000705', '5a3e0000-0000-4000-8000-000000000007', 'Manicure', 15000, null, null, 'Nails', 'Shape, cuticle care and long-wear polish.', 'manicure', 5),
('5a3e0001-0000-4000-8000-000000000706', '5a3e0000-0000-4000-8000-000000000007', 'Bridal package', 250000, null, 'from', 'Bridal', 'Trial, hair, make-up and touch-ups on the day.', 'bride', 6);

insert into faq (id, business_id, question, answer, sort_order) values
('5a3e0002-0000-4000-8000-000000000101', '5a3e0000-0000-4000-8000-000000000001', 'Do you use chemical sprays?', 'No. We use compost, crop rotation and neem-based sprays only.', 1),
('5a3e0002-0000-4000-8000-000000000102', '5a3e0000-0000-4000-8000-000000000001', 'Where do you deliver?', 'Hinjewadi, Wakad, Baner, Aundh and Pimple Saudagar.', 2),
('5a3e0002-0000-4000-8000-000000000201', '5a3e0000-0000-4000-8000-000000000002', 'Do you make eggless or vegan cakes?', 'Yes, most cakes can be made eggless. Ask when you order.', 1),
('5a3e0002-0000-4000-8000-000000000202', '5a3e0000-0000-4000-8000-000000000002', 'Is your kitchen nut-free?', 'No. We bake with nuts, so we cannot promise a nut-free bake.', 2),
('5a3e0002-0000-4000-8000-000000000301', '5a3e0000-0000-4000-8000-000000000003', 'What is the minimum order?', 'There is none. Delivery is free over ₹299 and ₹30 below that.', 1),
('5a3e0002-0000-4000-8000-000000000302', '5a3e0000-0000-4000-8000-000000000003', 'Can I pay on delivery?', 'Yes, by UPI, card or cash.', 2),
('5a3e0002-0000-4000-8000-000000000401', '5a3e0000-0000-4000-8000-000000000004', 'Which boards do you teach?', 'CBSE, ICSE and Karnataka State Board.', 1),
('5a3e0002-0000-4000-8000-000000000402', '5a3e0000-0000-4000-8000-000000000004', 'Are classes online or in person?', 'Group batches are in person. One-to-one sessions can be online.', 2),
('5a3e0002-0000-4000-8000-000000000501', '5a3e0000-0000-4000-8000-000000000005', 'Do you work with clients outside Portugal?', 'Yes. Half of our clients are abroad, and we work in English and Portuguese.', 1),
('5a3e0002-0000-4000-8000-000000000502', '5a3e0000-0000-4000-8000-000000000005', 'How long does a website take?', 'Usually three to four weeks from the first call.', 2),
('5a3e0002-0000-4000-8000-000000000601', '5a3e0000-0000-4000-8000-000000000006', 'Do you travel for weddings?', 'Yes, anywhere in East Africa. Travel is quoted separately.', 1),
('5a3e0002-0000-4000-8000-000000000602', '5a3e0000-0000-4000-8000-000000000006', 'When do we get our photos?', 'Portraits within a week, weddings within four weeks.', 2),
('5a3e0002-0000-4000-8000-000000000701', '5a3e0000-0000-4000-8000-000000000007', 'Do you take walk-ins?', 'We are appointment-only, but same-day bookings are often possible.', 1),
('5a3e0002-0000-4000-8000-000000000702', '5a3e0000-0000-4000-8000-000000000007', 'Is the salon women-only?', 'Yes, with a private room for clients who prefer it.', 2);
