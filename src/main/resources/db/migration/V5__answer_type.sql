-- What kind of answer the agent's pending question needs: plain text, or a phone number
-- (the screen then shows a country-code picker).
alter table agent_session add column pending_answer_type text;
