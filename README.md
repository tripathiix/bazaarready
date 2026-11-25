# BazaarReady

**Describe your business in any language. BazaarReady builds the website, tests it with AI customers, and keeps it working after launch.**

Live at **[bazaarready.com](https://bazaarready.com)**. Built for the GoDaddy Inclusive Innovation Hackathon 2026. The original design is in [docs/DESIGN.md](docs/DESIGN.md) and the security notes in [SECURITY.md](SECURITY.md).

Millions of small sellers and creators already sell through WhatsApp and Instagram, in their own language, but never get a website: builders expect forms in English, design skills and hours of setup, and stop helping the day the site goes live. BazaarReady starts from a chat and keeps going after launch.

## How it works

1. **Describe** the business by typing, speaking, or uploading WhatsApp chats and Instagram downloads, in 50+ languages. An Idea coach helps when the idea is still fuzzy.
2. **Answer a short form.** The agent asks only what is missing (prices, area, hours); every box can be answered by voice.
3. **Test.** Ten AI customers, picked from a pool of forty for your kind of site (a bargainer, Spanish, Chinese and Arabic readers, a blind visitor, someone on slow phone data, someone new to the internet and more), try to order, book, bargain and get in touch, then report what they couldn't find. A deep test once a day sends everyone who fits.
4. **Publish** with one tap, in one of 16 designs (four of them 3D, five made for one trade). Every site gets its own link.
5. **Keep running** with the owner dashboard: orders, bargaining, opening hours, chat, and an upgrade path to the owner's own domain.

## Features

| Feature | What it does |
| --- | --- |
| Build from a chat | Text or voice in 50+ languages, including right-to-left scripts; the microphone stops when you stop talking |
| Import chats | WhatsApp exports and Instagram downloads (zip, JSON or HTML), several files at once; names, numbers and emails are removed first, and Instagram zips are opened in the browser so photos never leave the device |
| AI test customers | Ten shopper personas test the site before launch, with a plain-words summary and "fix my website from these results" |
| Haggle mode | Customers bargain by text or voice; the lowest price is enforced on the server, and agreed deals go into the cart |
| Living shutter | The site opens and closes with the shop's hours; the owner can close it by voice, and customers can still pre-order |
| Payments | The owner picks UPI (to their own UPI ID), cash on delivery and/or ordering on WhatsApp, by telling the agent or from the dashboard. UPI opens the customer's UPI app with the amount and order number filled in (a QR code on a computer); the money goes straight to the owner, who marks it received. No card payments yet |
| Your language, by location | A visitor whose browser is in English is offered the main languages of their country (Hindi, Tamil, Kannada and more in India; Spanish in Mexico), on BazaarReady and on every shop site. It's an offer, never a switch; the country comes from a database on our own server and the address isn't stored. Shop pages keep prices and names exactly when translated |
| Two-language chat | Customer and owner each write and read in their own language |
| For creators | A media kit page, and a scam check for every brand-collab request |
| Test my idea | A coming-soon page with a waitlist that tests up to three prices, plus an AI focus group |
| Own domain estimate | A line-by-line cost of the owner's own .com, server and Titan email, in their currency, showing who gets each part of the money |
| 16 designs | Classic, Boutique, Mart, Split, Bento, Magazine and Noir; Globe, Poppy, Gradient and Neon in 3D; and Stay, Menu, Celebrate, Listing and Clinic for hotels, restaurants, weddings, rentals and clinics. 41 live sample sites from around the world on the [Templates](https://bazaarready.com/templates) page |
| Open about AI use | [bazaarready.com/usage](https://bazaarready.com/usage) shows every AI call, tokens, cost and what prompt caching saves |

## Architecture

```
 Owners (React builder)          Customers (shop sites)
            \                          /
             v                        v
  GoDaddy VPS  ─────────────────────────────────────────
  │ Caddy: HTTPS with Let's Encrypt                     │
  │ Spring Boot app: agent, site engine, haggle, chat,  │ ──> Claude API (Sonnet 5.5 builds,
  │   owner dashboard, rate limits, AI budget           │      Haiku 5.5 replies, prompt caching)
  │ PostgreSQL: shops, orders, chats, AI usage log      │ ──> Wikimedia Commons (shop photos)
  │ Backup: nightly pg_dump, last 14 kept               │
  ──────────────────────────────────────────────────────
```

- **Builder screens** (`frontend/`): React 18, TypeScript and Vite. The 8 hand-checked languages ship with the page; any other language is translated once by Claude and cached.
- **Shop sites** (`src/main/resources/templates/site/`): server-rendered Thymeleaf pages, one engine for all 16 designs.
- **Backend** (`src/main/java/com/bazaarready/`): `agent` (the builder agent and its tools), `site` (shop pages and BazaarReady's own pages), `extras` (haggle, shutter, chat, focus group, own-domain prices), `intake` (WhatsApp and Instagram import), `testing` (AI test customers), `coach`, `guard` (scope guardrail), `security`, `orders`, `account`, `support`.
- **Database**: PostgreSQL 16 with Flyway migrations in `src/main/resources/db/migration/`.

## Stack

- Java 17, Spring Boot 3.5, PostgreSQL 16, Flyway
- React 18, TypeScript 5, Vite 5 for the builder; Thymeleaf for shop pages
- Claude API: `claude-sonnet-5-5` for building and testing, `claude-haiku-5-5` for fast replies
- Docker Compose; Caddy for HTTPS in production

## Run locally

1. Copy `.env.example` to `.env` and fill in `DB_PASSWORD` and `ANTHROPIC_API_KEY`. The other values are optional.
2. Start Docker Desktop.
3. Run `docker compose up --build`.
4. Open http://localhost:8090. Health check: http://localhost:8090/actuator/health

To work on the builder screens with hot reload, run `npm install` and `npm run dev` in `frontend/`.

## Deploy

Production runs on a GoDaddy VPS with Docker Compose. From the repository root:

```
git archive --format=tar main | ssh deploy@<server> "cd ~/bazaarready && tar xf - && docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build"
```

`docker-compose.prod.yml` adds Caddy (the only service facing the internet, on ports 80 and 443), secure cookies, automatic restarts and the backup service. The server's `.env` holds the secrets and is never copied from git.

## Backups

- The `backup` service makes a compressed `pg_dump` when it starts and every 24 hours after, into `~/bazaarready/backups` on the server, keeping the last 14.
- `sh scripts/pull-backups.sh` copies the newest backup to `~/bazaarready-backups` on your computer, so a copy lives off the server.
- [scripts/restore-backup.md](scripts/restore-backup.md) explains a restore.

## Security

Passwords are hashed with BCrypt; sessions and forms are protected against CSRF; every page has a Content-Security-Policy with a fresh nonce; requests are rate-limited per address; Claude calls are capped per day, per account and for anonymous use; imported chats are cleaned of names, numbers and emails before the model sees them; uploaded zips are opened without decompressing files we don't need. Details in [SECURITY.md](SECURITY.md).

## Secrets

Secrets live only in `.env`, which is listed in `.gitignore`. `.env.example` lists the names without values. Nothing secret goes in the code or the repository.

## Built with

Claude Code was used as an assistant for the design and the code.
