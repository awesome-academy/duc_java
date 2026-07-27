CREATE TABLE tour_includes (
    id      BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES tours (id),
    type    VARCHAR(20) NOT NULL CHECK (type IN ('INCLUDE', 'EXCLUDE')),
    content VARCHAR(500) NOT NULL
);

CREATE INDEX idx_tour_includes_tour_id ON tour_includes (tour_id);
