DELETE FROM tour_highlights WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO tour_highlights (tour_id, content)
SELECT t.id, v.content
FROM (VALUES
    ('kham-pha-da-nang-3n2d', 'Chinh phục Bà Nà Hills, check-in Cầu Vàng'),
    ('kham-pha-da-nang-3n2d', 'Tắm biển Mỹ Khê - top biển đẹp nhất hành tinh'),
    ('phu-quoc-thien-duong-4n3d', 'Cáp treo vượt biển dài nhất thế giới'),
    ('phu-quoc-thien-duong-4n3d', 'Lặn ngắm san hô tại An Thới')
) AS v(tour_slug, content)
JOIN tours t ON t.slug = v.tour_slug;
