DROP INDEX idx_place_2;

-- geometry 타입을 geography 타입으로 변경
ALTER TABLE place
    ALTER COLUMN location_for_query TYPE geography(Point, 4326) -- WGS84 경위도 체계
    USING location_for_query::geography;

CREATE INDEX idx_place_2 ON place USING GIST (location_for_query);
