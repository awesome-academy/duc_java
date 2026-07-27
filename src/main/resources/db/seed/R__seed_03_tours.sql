INSERT INTO tours (
    title, slug, destination_id, category_id, price, discount_price,
    duration_days, max_guests, rating_avg, review_count, is_featured, status, created_at, description
)
SELECT v.title, v.slug, d.id, c.id, v.price, v.discount_price,
       v.duration_days, v.max_guests, v.rating_avg, v.review_count, v.is_featured, v.status,
       now() - (v.age_days || ' days')::interval, v.description
FROM (VALUES
    ('Khám phá Đà Nẵng 3N2Đ', 'kham-pha-da-nang-3n2d', 'da-nang', 'bien', 2990000::numeric, 2490000::numeric, 3, 20, 4.5::numeric, 128, true, 'ACTIVE', 3, 'Hành trình khám phá thành phố biển Đà Nẵng năng động, chinh phục Bà Nà Hills và tắm biển Mỹ Khê.'),
    ('Đà Lạt mộng mơ 2N1Đ', 'da-lat-mong-mo-2n1d', 'da-lat', 'nui', 1990000::numeric, NULL::numeric, 2, 15, 4.7::numeric, 210, true, 'ACTIVE', 10, 'Trải nghiệm không khí se lạnh của thành phố ngàn hoa, dạo quanh hồ Xuân Hương và các vườn hoa nổi tiếng.'),
    ('Phú Quốc thiên đường biển đảo 4N3Đ', 'phu-quoc-thien-duong-4n3d', 'phu-quoc', 'bien', 5990000::numeric, 5490000::numeric, 4, 25, 4.8::numeric, 342, true, 'ACTIVE', 1, 'Khám phá đảo ngọc Phú Quốc với cáp treo Hòn Thơm và lặn ngắm san hô tại An Thới.'),
    ('Trekking Sa Pa - Fansipan 3N2Đ', 'trekking-sa-pa-fansipan-3n2d', 'sa-pa', 'trekking', 3490000::numeric, NULL::numeric, 3, 12, 4.6::numeric, 95, false, 'ACTIVE', 7, 'Chinh phục nóc nhà Đông Dương Fansipan, băng qua ruộng bậc thang Tây Bắc hùng vĩ.'),
    ('Du thuyền Hạ Long 2N1Đ', 'du-thuyen-ha-long-2n1d', 'ha-long', 'bien', 4290000::numeric, 3990000::numeric, 2, 30, 4.4::numeric, 187, false, 'ACTIVE', 14, 'Nghỉ đêm trên du thuyền, chiêm ngưỡng vịnh Hạ Long - kỳ quan thiên nhiên thế giới.'),
    ('Phố cổ Hội An về đêm 1N', 'pho-co-hoi-an-ve-dem-1n', 'hoi-an', 'thanh-pho', 890000::numeric, NULL::numeric, 1, 40, 4.2::numeric, 76, false, 'ACTIVE', 5, 'Dạo bước phố cổ Hội An lung linh ánh đèn lồng về đêm, trải nghiệm thả hoa đăng trên sông Hoài.'),
    ('Tour đã ngừng bán (test status)', 'tour-ngung-ban-test', 'da-nang', 'thanh-pho', 1000000::numeric, NULL::numeric, 1, 10, 3.0::numeric, 5, false, 'INACTIVE', 2, 'Tour dùng để kiểm thử trạng thái ngừng bán.')
) AS v(title, slug, destination_slug, category_slug, price, discount_price, duration_days, max_guests, rating_avg, review_count, is_featured, status, age_days, description)
JOIN destinations d ON d.slug = v.destination_slug
JOIN categories c ON c.slug = v.category_slug
ON CONFLICT (slug) DO UPDATE SET
    destination_id = EXCLUDED.destination_id,
    category_id    = EXCLUDED.category_id,
    price           = EXCLUDED.price,
    discount_price  = EXCLUDED.discount_price,
    duration_days   = EXCLUDED.duration_days,
    max_guests      = EXCLUDED.max_guests,
    rating_avg      = EXCLUDED.rating_avg,
    review_count    = EXCLUDED.review_count,
    is_featured     = EXCLUDED.is_featured,
    status          = EXCLUDED.status,
    created_at      = EXCLUDED.created_at,
    description     = EXCLUDED.description;
