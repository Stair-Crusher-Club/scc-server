-- Add quest support to challenge table
ALTER TABLE challenge 
ADD COLUMN quests TEXT;

-- Add quest progress support to challenge_participation table  
ALTER TABLE challenge_participation
ADD COLUMN quest_progresses TEXT;