-- Supports existsByUser_IdAndTour_IdAndStatusIn (CreateReviewService's booking-eligibility check):
-- idx_bookings_user_id alone narrows to one user's rows, but a composite index lets Postgres
-- filter by (user_id, tour_id) directly instead of scanning that user's remaining bookings.
CREATE INDEX idx_bookings_user_id_tour_id ON bookings (user_id, tour_id);

-- idx_bookings_user_id (from V15) is now a redundant prefix of the composite index above:
-- Postgres can use (user_id, tour_id) for a query that only filters user_id, so keeping both
-- would just make every INSERT/UPDATE/DELETE on bookings maintain two indexes for one purpose.
DROP INDEX idx_bookings_user_id;
