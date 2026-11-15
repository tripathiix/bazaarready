-- Where a launch session started from: a description, or WhatsApp chats.
alter table agent_session add column source text not null default 'DESCRIPTION';
