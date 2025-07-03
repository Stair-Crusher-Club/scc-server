CREATE TABLE IF NOT EXISTS place_review (
    id                           VARCHAR(36) PRIMARY KEY,
    place_id                     VARCHAR(36) NOT NULL,
    recommended_mobility_types   JSON        NOT NULL,
    spacious_type                VARCHAR(32) NOT NULL,
    comment                      TEXT        NOT NULL,
    mobility_tool                VARCHAR(64) NOT NULL,
    seat_types                   JSON        NOT NULL,
    order_methods                JSON        NOT NULL,
    features                     JSON        NOT NULL,
    created_at                   TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_place_review_place_id   ON place_review (place_id);
CREATE INDEX idx_place_review_created_at ON place_review (created_at DESC);

CREATE TABLE IF NOT EXISTS toilet_review (
    id                     VARCHAR(36) PRIMARY KEY,
    toilet_location_type   VARCHAR(32) NOT NULL,
    target_id              VARCHAR(36) NOT NULL,
    floor                  INT         NULL,
    entrance_door_types    JSON        NULL,
    comment                TEXT        NULL,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Recommended indexes
CREATE INDEX idx_toilet_review_target_id   ON toilet_review (target_id);
CREATE INDEX idx_toilet_review_created_at  ON toilet_review (created_at DESC);
