DELETE FROM tour_includes WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO tour_includes (tour_id, type, content)
SELECT t.id, v.type, v.content
FROM (VALUES
    ('kham-pha-da-nang-3n2d', 'INCLUDE', 'Xe du lịch đời mới, máy lạnh'),
    ('kham-pha-da-nang-3n2d', 'INCLUDE', 'Khách sạn 4 sao, ăn 3 bữa/ngày'),
    ('kham-pha-da-nang-3n2d', 'EXCLUDE', 'Chi phí cá nhân, đồ uống ngoài thực đơn'),
    ('phu-quoc-thien-duong-4n3d', 'INCLUDE', 'Vé máy bay khứ hồi'),
    ('phu-quoc-thien-duong-4n3d', 'INCLUDE', 'Vé cáp treo Hòn Thơm'),
    ('phu-quoc-thien-duong-4n3d', 'EXCLUDE', 'Tip hướng dẫn viên')
) AS v(tour_slug, type, content)
JOIN tours t ON t.slug = v.tour_slug;
