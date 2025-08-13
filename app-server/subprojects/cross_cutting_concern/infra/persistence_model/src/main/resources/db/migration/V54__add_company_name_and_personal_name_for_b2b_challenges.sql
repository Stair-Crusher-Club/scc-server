-- Add participant_name and company_name to challenge_participation table for recording participant info
-- Each participant can belong to different companies in the same challenge
ALTER TABLE challenge_participation ADD COLUMN participant_name VARCHAR(64) NULL;
ALTER TABLE challenge_participation ADD COLUMN company_name VARCHAR(128) NULL;

-- Add isB2B flag to challenge table to indicate if it's a B2B challenge
ALTER TABLE challenge ADD COLUMN is_b2b BOOLEAN NOT NULL DEFAULT FALSE;
