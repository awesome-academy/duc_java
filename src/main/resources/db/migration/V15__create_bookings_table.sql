CREATE TABLE bookings (
    id            BIGSERIAL PRIMARY KEY,
    booking_code  VARCHAR(50) NOT NULL UNIQUE,
    user_id       BIGINT NOT NULL REFERENCES users (id),
    tour_id       BIGINT NOT NULL REFERENCES tours (id),
    departure_id  BIGINT NOT NULL REFERENCES tour_departures (id),
    adults        INT NOT NULL,
    children      INT NOT NULL DEFAULT 0,
    total_price   NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    contact_name  VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
