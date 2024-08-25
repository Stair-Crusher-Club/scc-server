ALTER TABLE scc_user
    ADD COLUMN email VARCHAR(64) NULL,
    ADD COLUMN mobility_tools TEXT NULL,
    ALTER COLUMN encrypted_password DROP NOT NULL;

CREATE UNIQUE INDEX idx_user_table_2 ON scc_user(email);