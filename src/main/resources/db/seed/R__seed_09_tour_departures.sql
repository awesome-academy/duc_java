DELETE FROM tour_departures WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO tour_departures (tour_id, departure_date, total_slots, booked_slots)
SELECT t.id, v.departure_date::date, v.total_slots, v.booked_slots
FROM (VALUES
    ('kham-pha-da-nang-3n2d', '2026-07-28', 20, 15),
    ('kham-pha-da-nang-3n2d', '2026-08-05', 20, 5),
    ('kham-pha-da-nang-3n2d', '2026-08-12', 20, 20),
    ('phu-quoc-thien-duong-4n3d', '2026-07-30', 25, 10),
    ('phu-quoc-thien-duong-4n3d', '2026-08-15', 25, 0)
) AS v(tour_slug, departure_date, total_slots, booked_slots)
JOIN tours t ON t.slug = v.tour_slug;
