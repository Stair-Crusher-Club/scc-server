ALTER TABLE scc_user ADD COLUMN user_id VARCHAR(36) NULL;

CREATE INDEX idx_user_table_3 ON scc_user(user_id);
