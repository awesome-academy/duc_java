DELETE FROM reviews WHERE tour_id IN (
    SELECT id FROM tours WHERE slug IN ('kham-pha-da-nang-3n2d', 'phu-quoc-thien-duong-4n3d')
);

INSERT INTO reviews (tour_id, user_id, rating, comment)
SELECT t.id, u.id, v.rating, v.comment
FROM (VALUES
    ('kham-pha-da-nang-3n2d', 'nguyenvana@example.com', 5, 'Chuyến đi tuyệt vời, hướng dẫn viên nhiệt tình!'),
    ('kham-pha-da-nang-3n2d', 'tranthib@example.com', 4, 'Lịch trình hợp lý, khách sạn sạch sẽ.'),
    ('phu-quoc-thien-duong-4n3d', 'levanc@example.com', 5, 'Cáp treo Hòn Thơm quá đẹp, đáng tiền!'),
    ('phu-quoc-thien-duong-4n3d', 'nguyenvana@example.com', 4, 'Lặn ngắm san hô rất thú vị.')
) AS v(tour_slug, email, rating, comment)
JOIN tours t ON t.slug = v.tour_slug
JOIN users u ON u.email = v.email;
