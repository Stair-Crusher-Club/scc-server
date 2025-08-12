-- Add company_name to challenge table for B2B join conditions
ALTER TABLE challenge ADD COLUMN company_name VARCHAR(128) NULL;

-- Add participant_name to challenge_participation table for recording participant info
ALTER TABLE challenge_participation ADD COLUMN participant_name VARCHAR(64) NULL;

-- Add index on company_name for efficient lookups
CREATE INDEX idx_challenge_company_name ON challenge(company_name);