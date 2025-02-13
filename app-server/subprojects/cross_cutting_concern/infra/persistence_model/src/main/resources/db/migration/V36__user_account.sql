CREATE TABLE IF NOT EXISTS user_account (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    account_type VARCHAR(36) NULL,
    deleted_at TIMESTAMP(6) WITH TIME ZONE NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_account_1 ON user_account(created_at);

CREATE TABLE IF NOT EXISTS user_account_connection (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    identified_user_account_id VARCHAR(36) NOT NULL,
    anonymous_user_account_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_account_connection_1 ON user_account_connection(identified_user_account_id);
CREATE INDEX idx_user_account_connection_2 ON user_account_connection(anonymous_user_account_id);
CREATE INDEX idx_user_account_connection_3 ON user_account_connection(created_at);
