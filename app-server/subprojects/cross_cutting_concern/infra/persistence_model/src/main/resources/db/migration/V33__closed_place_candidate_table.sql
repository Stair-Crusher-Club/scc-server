CREATE TABLE IF NOT EXISTS closed_place_candidate (
    id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    external_id VARCHAR(64) NOT NULL,
    accepted_at TIMESTAMP(6) WITH TIME ZONE NULL DEFAULT NULL,
    ignored_at TIMESTAMP(6) WITH TIME ZONE NULL DEFAULT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_closed_place_candidate_1 ON closed_place_candidate(place_id);
CREATE INDEX idx_closed_place_candidate_2 ON closed_place_candidate(external_id);
CREATE INDEX idx_closed_place_candidate_3 ON closed_place_candidate(created_at);