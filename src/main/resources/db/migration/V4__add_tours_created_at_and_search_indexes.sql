ALTER TABLE tours
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_tours_price ON tours (price);
CREATE INDEX idx_tours_created_at ON tours (created_at DESC);
