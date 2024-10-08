CREATE TABLE IF NOT EXISTS challenge (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    invitation_code VARCHAR(64) NULL,
    passcode VARCHAR(64) NULL,
    is_complete BOOLEAN NOT NULL DEFAULT FALSE,
    starts_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_at TIMESTAMP(6) WITH TIME ZONE DEFAULT NULL,
    goal INT NOT NULL,
    milestones TEXT NOT NULL,
    conditions TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_challenge_invitation_code_1 ON challenge(created_at);

CREATE INDEX idx_challenge_invitation_code_2 ON challenge(invitation_code);

CREATE TABLE IF NOT EXISTS challenge_participation (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_challenge_participation_1 ON challenge_participation(created_at);

CREATE INDEX idx_challenge_participation_2 ON challenge_participation(challenge_id);

CREATE INDEX idx_challenge_participation_3 ON challenge_participation(user_id, challenge_id);

CREATE TABLE IF NOT EXISTS challenge_contribution (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    challenge_id VARCHAR(36) NOT NULL,
    place_accessibility_id VARCHAR(36) NULL,
    place_accessibility_comment_id VARCHAR(36) NULL,
    building_accessibility_id VARCHAR(36) NULL,
    building_accessibility_comment_id VARCHAR(36) NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_challenge_contribution_1 ON challenge_contribution(created_at);

CREATE INDEX idx_challenge_contribution_2 ON challenge_contribution(challenge_id);

CREATE INDEX idx_challenge_contribution_3 ON challenge_contribution(user_id, challenge_id);