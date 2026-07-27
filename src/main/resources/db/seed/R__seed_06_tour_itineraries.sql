DELETE FROM tour_itineraries WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO tour_itineraries (tour_id, day_number, title, description)
SELECT t.id, v.day_number, v.title, v.description
FROM (VALUES
    ('kham-pha-da-nang-3n2d', 1, 'Đà Nẵng - Bà Nà Hills', 'Tham quan Bà Nà Hills, Cầu Vàng.'),
    ('kham-pha-da-nang-3n2d', 2, 'Bán đảo Sơn Trà - Ngũ Hành Sơn', 'Khám phá Sơn Trà và Ngũ Hành Sơn.'),
    ('kham-pha-da-nang-3n2d', 3, 'Tự do - Tiễn sân bay', 'Tự do mua sắm, tiễn đoàn ra sân bay.'),
    ('phu-quoc-thien-duong-4n3d', 1, 'Đón đoàn - Grand World', 'Đón sân bay, tham quan Grand World.'),
    ('phu-quoc-thien-duong-4n3d', 2, 'Cáp treo Hòn Thơm', 'Trải nghiệm cáp treo vượt biển dài nhất thế giới.'),
    ('phu-quoc-thien-duong-4n3d', 3, 'Lặn ngắm san hô', 'Tour lặn ngắm san hô tại quần đảo An Thới.'),
    ('phu-quoc-thien-duong-4n3d', 4, 'Tự do - Tiễn sân bay', 'Tự do nghỉ ngơi, tiễn đoàn ra sân bay.')
) AS v(tour_slug, day_number, title, description)
JOIN tours t ON t.slug = v.tour_slug;
