INSERT INTO categories (name, slug) VALUES
    ('Biển', 'bien'),
    ('Núi', 'nui'),
    ('Thành phố', 'thanh-pho'),
    ('Trekking', 'trekking')
ON CONFLICT (slug) DO NOTHING;
