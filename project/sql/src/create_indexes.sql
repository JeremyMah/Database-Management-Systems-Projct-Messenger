CREATE INDEX ma ON MEDIA_ATTACHMENT
USING btree(msg_id);

CREATE INDEX hello on MESSAGE
USING btree(chat_id);
