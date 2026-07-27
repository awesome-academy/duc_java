CREATE TABLE tour_images (
    id            BIGSERIAL PRIMARY KEY,
    tour_id       BIGINT NOT NULL REFERENCES tours (id),
    image_url     VARCHAR(500) NOT NULL,
    is_thumbnail  BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tour_images_tour_id ON tour_images (tour_id);
