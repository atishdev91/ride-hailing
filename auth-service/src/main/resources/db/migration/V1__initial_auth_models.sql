CREATE TABLE driver
(
    driver_id      BIGINT       NOT NULL,
    created_at     datetime     NOT NULL,
    updated_at     datetime     NOT NULL,
    name           VARCHAR(255) NULL,
    email          VARCHAR(255) NULL,
    password       VARCHAR(255) NULL,
    phone_number   VARCHAR(10)  NULL,
    license_number VARCHAR(255) NULL,
    vehicle_number VARCHAR(255) NULL,
    active         BIT(1)       NOT NULL,
    CONSTRAINT pk_driver PRIMARY KEY (driver_id)
);

CREATE TABLE rider
(
    rider_id     BIGINT       NOT NULL,
    created_at   datetime     NOT NULL,
    updated_at   datetime     NOT NULL,
    name         VARCHAR(255) NULL,
    email        VARCHAR(255) NULL,
    password     VARCHAR(255) NULL,
    phone_number VARCHAR(10)  NULL,
    CONSTRAINT pk_rider PRIMARY KEY (rider_id)
);

ALTER TABLE driver
    ADD CONSTRAINT uc_driver_email UNIQUE (email);

ALTER TABLE rider
    ADD CONSTRAINT uc_rider_email UNIQUE (email);