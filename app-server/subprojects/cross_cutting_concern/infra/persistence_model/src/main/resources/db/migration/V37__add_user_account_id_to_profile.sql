ALTER TABLE scc_user ADD COLUMN user_account_id VARCHAR(32) NULL;

CREATE INDEX idx_user_table_3 ON scc_user(user_account_id);
