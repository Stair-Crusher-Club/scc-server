CREATE INDEX idx_place_location_for_query_geom ON place USING GIST ((location_for_query::geometry));
