DELETE FROM tour_images WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO tour_images (tour_id, image_url, is_thumbnail, display_order)
SELECT t.id, v.image_url, v.is_thumbnail, v.display_order
FROM (VALUES
    ('kham-pha-da-nang-3n2d', 'https://picsum.photos/seed/tour-da-nang-1/800/600', true, 1),
    ('kham-pha-da-nang-3n2d', 'https://picsum.photos/seed/tour-da-nang-2/800/600', false, 2),
    ('kham-pha-da-nang-3n2d', 'https://picsum.photos/seed/tour-da-nang-3/800/600', false, 3),
    ('phu-quoc-thien-duong-4n3d', 'https://picsum.photos/seed/tour-phu-quoc-1/800/600', true, 1),
    ('phu-quoc-thien-duong-4n3d', 'https://picsum.photos/seed/tour-phu-quoc-2/800/600', false, 2)
) AS v(tour_slug, image_url, is_thumbnail, display_order)
JOIN tours t ON t.slug = v.tour_slug;
