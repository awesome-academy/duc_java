CREATE TABLE tour_highlights (
    id      BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES tours (id),
    content VARCHAR(500) NOT NULL
);

CREATE INDEX idx_tour_highlights_tour_id ON tour_highlights (tour_id);
