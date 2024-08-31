CREATE TABLE IF NOT EXISTS user_auth_info (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    auth_provider_type VARCHAR(16) NOT NULL,
    external_id VARCHAR(64) NOT NULL,
    external_refresh_token TEXT NOT NULL,
    external_refresh_token_expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_auth_info_1 ON user_auth_info(user_id, auth_provider_type);

CREATE UNIQUE INDEX idx_user_auth_info_2 ON user_auth_info(external_id, auth_provider_type);

CREATE INDEX idx_user_auth_info_3 ON user_auth_info(external_refresh_token_expires_at);

CREATE INDEX idx_user_auth_info_4 ON user_auth_info(created_at);