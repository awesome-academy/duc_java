CREATE TABLE tour_itineraries (
    id          BIGSERIAL PRIMARY KEY,
    tour_id     BIGINT NOT NULL REFERENCES tours (id),
    day_number  INT NOT NULL,
    title       VARCHAR(255),
    description TEXT
);

CREATE INDEX idx_tour_itineraries_tour_id ON tour_itineraries (tour_id);
