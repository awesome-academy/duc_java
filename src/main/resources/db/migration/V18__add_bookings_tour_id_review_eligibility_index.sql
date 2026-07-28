-- Supports existsByUser_IdAndTour_IdAndStatusIn (CreateReviewService's booking-eligibility check):
-- idx_bookings_user_id alone narrows to one user's rows, but a composite index lets Postgres
-- filter by (user_id, tour_id) directly instead of scanning that user's remaining bookings.
CREATE INDEX idx_bookings_user_id_tour_id ON bookings (user_id, tour_id);
