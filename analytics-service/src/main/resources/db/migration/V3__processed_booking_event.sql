CREATE TABLE processed_booking_event (
    event_id VARCHAR(36) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    workout_id BIGINT NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
