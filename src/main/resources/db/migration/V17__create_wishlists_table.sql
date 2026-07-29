CREATE TABLE wishlists (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    tour_id    BIGINT NOT NULL REFERENCES tours (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, tour_id)
);

-- No separate index on user_id: the UNIQUE(user_id, tour_id) constraint above already creates
-- a composite btree index usable for lookups filtering by user_id alone (leftmost-column rule).
