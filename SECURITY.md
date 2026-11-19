# Security in BazaarReady

BazaarReady handles small businesses' orders, customer messages and an AI budget. This page lists how each of those is protected, and where in the code to look.

## Who can do what

| Area | Who | How it's enforced |
|---|---|---|
| Home page, guide, every generated customer site (`/s/...`) | Anyone | Public by design: customers never need an account. |
| Building a site and the Idea coach | Anyone (sign-in available behind a flag) | Off for the hackathon demo so judges can try it with no sign-up. `AUTH_FOR_BUILDING=true` makes `SecurityConfig` require sign-in for `/api/agent/**` and `/api/coach/**`. Rate limits, the daily spending cap and the scope guardrail apply either way. |
| Changing a site from the builder screen | Whoever has the private link, until an owner claims the site; then only that owner | `OwnerAccess.openForChange`. |
| Owner dashboard (orders, messages, AI tests, collab checks, products, photos), My sites, `/api/businesses/**` | The site's owner only, always signed in | `OwnerAccess` checks every owner page and action against the signed-in account. Anyone else gets "not found", so nothing leaks. |
| An agent conversation | The account that started it | `AgentService.find` refuses other accounts. |

Sites made before accounts existed are claimed by the first signed-in person who opens their private owner link, because only the owner was given that link.

## Accounts and passwords

- Passwords are hashed with **BCrypt** (Spring Security's delegating encoder) and never stored or logged in plain text.
- Sign-up rejects short passwords (under 8 characters), passwords over BCrypt's 72-byte limit, very common passwords, and passwords equal to the email.
- After sign-in or sign-up the session id is replaced (session fixation protection).
- Session cookies are `HttpOnly`, `SameSite=Lax`, and `Secure` in production (`COOKIE_SECURE=true`).
- Failed sign-ins show one message for both wrong email and wrong password, so accounts can't be discovered.

## Request forgery (CSRF)

- Every change made by a signed-in owner needs a CSRF token. The React screens send it from the `XSRF-TOKEN` cookie in an `X-XSRF-TOKEN` header; server-rendered forms carry it as a hidden field.
- Customer forms on generated sites (`/s/...`) are exempt: their visitors are anonymous, so there is no signed-in session to forge.

## Browser protections (headers)

Set in `SecurityConfig` and `SecurityHeadersFilter`:

- **Content-Security-Policy** with a fresh nonce on every page: only this site's scripts, or inline scripts carrying the nonce, can run. No inline event handlers anywhere. Images only from this site, Wikimedia and Pexels; fonts only from Google Fonts.
- `frame-ancestors 'none'` and `X-Frame-Options: DENY` (no clickjacking).
- `Strict-Transport-Security` over HTTPS, `Referrer-Policy: strict-origin-when-cross-origin`, `X-Content-Type-Options: nosniff`.
- `Permissions-Policy`: microphone for "Speak instead" only; camera, location, payment and USB off.

## Abuse and cost controls

- **Rate limits per address** (`RateLimitFilter`): AI requests 12 a minute and 250 a day; sign-in and sign-up 10 per 15 minutes; customer orders and messages 20 per 10 minutes; owner edits and uploads 40 per 10 minutes. The client address is taken from the proxy only when the proxy is on a private network, so it can't be spoofed.
- **Daily AI spending cap** (`ClaudeClient`): every call to Claude is counted in the database, per day and per account. Defaults: 3,000 calls a day for the whole service and 400 per account (`CLAUDE_DAILY_CAP`, `CLAUDE_PER_ACCOUNT_DAILY_CAP`).
- **Scope guardrail** (`ScopeGuard`): a small model checks that free text is about the user's business or website before it reaches the builder, the update agent or the Idea coach. Requests like "write binary search in Java" are refused, so the service can't be used as a free general-purpose AI.

## AI safety

- The agents treat everything a user writes, every web page the AI test customers read, and every pasted collab message as **data, never instructions** (stated in each system prompt).
- Agents can only use the tools they're given. Publishing a site needs the owner's explicit approval.
- AI test customers can only browse the site under test (`SiteBrowser.allowed`); their test orders and messages are deleted afterwards.
- Collab checks look up public facts only (RDAP, DNS over HTTPS) and never follow links in the pasted message.

## Data handling

- WhatsApp chat exports are read in memory. Customers' names and phone numbers are replaced before anything reaches the model, and the files are never stored.
- Instagram "Download your information" exports are handled the same way: only the message files inside the zip are read (photos are skipped unread), names, numbers and emails are replaced, and nothing is stored. Uploads are limited to 60 MB, and at most 12 MB of messages are read.
- Uploaded photos: 3 MB limit, file type checked from the content (not the name), served with `nosniff`.
- All SQL uses bound parameters. Input lengths are limited on the server, not just in the browser.

## Secrets

- API keys and passwords live only in environment variables (`.env`, which is git-ignored). `.env.example` lists the names without values. Nothing secret is in the repository.

## Reporting a problem

Please email the address on bazaarready.com rather than opening a public issue.
