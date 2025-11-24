CREATE TABLE trip
(
    trip_id         BIGINT       NOT NULL,
    created_at      datetime     NOT NULL,
    updated_at      datetime     NOT NULL,
    driver_id       BIGINT       NULL,
    rider_id        BIGINT       NULL,
    trip_status     ENUM ('REQUESTED', 'ASSIGNED','IN_PROGRESS', 'COMPLETED', 'CANCELLED') NULL,
    start_latitude  DOUBLE       NULL,
    start_longitude DOUBLE       NULL,
    end_latitude    DOUBLE       NULL,
    end_longitude   DOUBLE       NULL,
    CONSTRAINT pk_trip PRIMARY KEY (trip_id)
);