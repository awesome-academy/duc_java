-- Seed admin account for the Thymeleaf admin portal (/admin/login).
-- Email: admin@tripgo.vn — Password: Admin@123
-- BCrypt cost 12, matching the PasswordEncoder bean in SecurityConfig.
INSERT INTO users (full_name, email, password_hash, phone, role) VALUES
    ('TripGo Admin', 'admin@tripgo.vn', '$2a$12$vjDXlfFA0ynlwnrY3o/idujQYFxQeYH.niWEACRyto4nO55ewuTvG', '0900000000', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
