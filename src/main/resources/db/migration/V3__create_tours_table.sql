CREATE TABLE tours (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    slug           VARCHAR(255) NOT NULL UNIQUE,
    destination_id BIGINT REFERENCES destinations (id),
    category_id    BIGINT REFERENCES categories (id),
    price          NUMERIC(12, 2) NOT NULL,
    discount_price NUMERIC(12, 2),
    duration_days  INT,
    max_guests     INT,
    rating_avg     NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count   INT NOT NULL DEFAULT 0,
    is_featured    BOOLEAN NOT NULL DEFAULT FALSE,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                   CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_tours_destination_id ON tours (destination_id);
CREATE INDEX idx_tours_category_id ON tours (category_id);
CREATE INDEX idx_tours_rating_avg ON tours (rating_avg DESC);
CREATE INDEX idx_tours_is_featured ON tours (is_featured);
CREATE INDEX idx_tours_status ON tours (status);
