INSERT INTO destinations (name, slug, description, image_url) VALUES
    ('Đà Nẵng', 'da-nang', 'Thành phố biển năng động miền Trung.', 'https://picsum.photos/seed/da-nang/640/480'),
    ('Đà Lạt', 'da-lat', 'Thành phố ngàn hoa, khí hậu se lạnh quanh năm.', 'https://picsum.photos/seed/da-lat/640/480'),
    ('Phú Quốc', 'phu-quoc', 'Đảo ngọc với biển xanh cát trắng.', 'https://picsum.photos/seed/phu-quoc/640/480'),
    ('Sa Pa', 'sa-pa', 'Vùng núi Tây Bắc, ruộng bậc thang và Fansipan.', 'https://picsum.photos/seed/sa-pa/640/480'),
    ('Hạ Long', 'ha-long', 'Vịnh biển kỳ quan thiên nhiên thế giới.', 'https://picsum.photos/seed/ha-long/640/480'),
    ('Hội An', 'hoi-an', 'Phố cổ với đèn lồng và kiến trúc xưa.', 'https://picsum.photos/seed/hoi-an/640/480')
ON CONFLICT (slug) DO NOTHING;
