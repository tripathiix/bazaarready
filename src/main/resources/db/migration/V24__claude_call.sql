-- One row per call to Claude: what it was for, the model, and the token counts Claude reported.
-- Numbers only: no prompts, replies or account details. Feeds the public /usage page.
create table claude_call (
    id              bigserial primary key,
    at              timestamptz not null default now(),
    feature         varchar(30) not null,
    model           varchar(60) not null,
    input_tokens    int not null,          -- read at full price
    cache_write     int not null,          -- written to the prompt cache (1.25x price)
    cache_read      int not null,          -- read from the prompt cache (a fraction of the price)
    output_tokens   int not null,
    cost_usd        numeric(12, 6) not null,
    uncached_usd    numeric(12, 6) not null, -- what the same tokens would have cost with no caching
    duration_ms     int not null
);

create index claude_call_at on claude_call (at);
