ALTER TABLE place
    ADD COLUMN location_for_query geometry(Point, 4326); -- WGS84 경위도 체계

CREATE INDEX idx_place_2 ON place USING GIST (location_for_query);
