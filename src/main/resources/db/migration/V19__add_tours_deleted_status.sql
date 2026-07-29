-- Admin "Xóa tour" is a soft delete: tours are referenced by bookings and reviews, so a hard
-- DELETE would either violate those foreign keys or force us to destroy a customer's booking
-- history. DELETED tours stay in the table (history intact) but are filtered out of both the
-- public API and the admin list.
ALTER TABLE tours
    DROP CONSTRAINT tours_status_check;

ALTER TABLE tours
    ADD CONSTRAINT tours_status_check
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'));
