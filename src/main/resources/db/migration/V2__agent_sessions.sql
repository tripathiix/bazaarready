-- A session is one conversation between an owner and the launch agent.
-- The agent can pause (to ask a question or wait for approval), so its message history is stored.

create table agent_session (
    id                  uuid primary key,
    business_id         uuid references business (id) on delete set null,
    status              text        not null,
    messages            text        not null,
    pending_tool_use_id text,
    pending_tool        text,
    pending_question    text,
    final_message       text,
    model_calls         integer     not null default 0,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

-- Actions are logged from the first step, before a business exists.
alter table agent_action add column session_id uuid references agent_session (id) on delete cascade;
alter table agent_action alter column business_id drop not null;
alter table agent_action alter column run_id drop not null;
create index agent_action_session_idx on agent_action (session_id, step_no);
