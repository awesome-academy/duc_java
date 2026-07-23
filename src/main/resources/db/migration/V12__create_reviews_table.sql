CREATE TABLE reviews (
    id         BIGSERIAL PRIMARY KEY,
    tour_id    BIGINT NOT NULL REFERENCES tours (id),
    user_id    BIGINT NOT NULL REFERENCES users (id),
    rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tour_id, user_id)
);

CREATE INDEX idx_reviews_tour_id ON reviews (tour_id);
