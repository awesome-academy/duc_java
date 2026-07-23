INSERT INTO users (full_name, email, password_hash, phone, role) VALUES
    ('Nguyễn Văn A', 'nguyenvana@example.com', '$2a$10$placeholderplaceholderplaceholderplaceholderplaceh', '0901111111', 'USER'),
    ('Trần Thị B', 'tranthib@example.com', '$2a$10$placeholderplaceholderplaceholderplaceholderplaceh', '0902222222', 'USER'),
    ('Lê Văn C', 'levanc@example.com', '$2a$10$placeholderplaceholderplaceholderplaceholderplaceh', '0903333333', 'USER')
ON CONFLICT (email) DO NOTHING;
