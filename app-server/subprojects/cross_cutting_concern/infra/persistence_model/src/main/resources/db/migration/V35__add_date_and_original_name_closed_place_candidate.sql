ALTER TABLE closed_place_candidate
    ADD COLUMN original_name TEXT NOT NULL,
    ADD COLUMN original_address TEXT NOT NULL,
    ADD COLUMN closed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL;