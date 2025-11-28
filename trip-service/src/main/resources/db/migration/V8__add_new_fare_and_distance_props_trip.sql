ALTER TABLE trip
    ADD started_at datetime NULL;

ALTER TABLE trip
    ADD completed_at datetime NULL;

ALTER TABLE trip
    ADD distance_km DOUBLE NULL;

ALTER TABLE trip
    ADD fare DOUBLE NULL;

ALTER TABLE trip
    ADD last_latitude DOUBLE NULL;

ALTER TABLE trip
    ADD last_longitude DOUBLE NULL;


