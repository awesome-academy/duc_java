CREATE TABLE bookings (
    id               BIGSERIAL PRIMARY KEY,
    booking_code     VARCHAR(50) NOT NULL UNIQUE,
    idempotency_key  VARCHAR(100) NOT NULL,
    user_id          BIGINT NOT NULL REFERENCES users (id),
    tour_id          BIGINT NOT NULL REFERENCES tours (id),
    departure_id     BIGINT NOT NULL REFERENCES tour_departures (id),
    adults           INT NOT NULL,
    children         INT NOT NULL DEFAULT 0,
    total_price      NUMERIC(12, 2) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    contact_name     VARCHAR(255),
    contact_email    VARCHAR(255),
    contact_phone    VARCHAR(20),
    created_at       TIMESTAMPTZ NOT NULL,
    -- Idempotency keys are client-generated and only meaningful per submitting user; a global
    -- unique index would let one user's key collide with another user's and leak their booking.
    CONSTRAINT uq_bookings_user_idempotency_key UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
