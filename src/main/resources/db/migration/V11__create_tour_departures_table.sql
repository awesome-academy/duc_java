CREATE TABLE tour_departures (
    id             BIGSERIAL PRIMARY KEY,
    tour_id        BIGINT NOT NULL REFERENCES tours (id),
    departure_date DATE NOT NULL,
    total_slots    INT NOT NULL,
    booked_slots   INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tour_departures_tour_id_date ON tour_departures (tour_id, departure_date);
