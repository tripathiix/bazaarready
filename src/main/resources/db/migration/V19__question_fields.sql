-- A question can come as a short form: labelled boxes and drop-downs instead of one long answer.
alter table agent_session add column pending_fields text;
