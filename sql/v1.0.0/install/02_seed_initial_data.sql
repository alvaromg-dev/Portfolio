-- Portfolio Database Seed Data v1.0.0
-- Initial data for Portfolio Application

-- Insert default languages
INSERT INTO languages (code, name, is_default, enabled)
VALUES
    ('es', 'Español', true, true),
    ('en', 'English', false, true)
ON CONFLICT (code) DO NOTHING;

-- Insert default roles
INSERT INTO roles (code, description)
VALUES
    ('admin', 'Administrator role with full access')
ON CONFLICT (code) DO NOTHING;

-- Insert default admin user (username: admin, password: admin)
-- Password is stored using bcrypt so authenticateUser/compareSync can validate it.
DO $$
DECLARE
    admin_password_hash TEXT := '$2a$10$MyUvasX6SfoYgXdpeNaz0eKcL0tH/9AFn0zNlhYWc71M//91GMNK6';
    admin_user_id UUID;
BEGIN
    SELECT id INTO admin_user_id
    FROM users
    WHERE name = 'admin'
    LIMIT 1;

    IF admin_user_id IS NULL THEN
        INSERT INTO users (name, password)
        VALUES ('admin', admin_password_hash)
        RETURNING id INTO admin_user_id;
    ELSE
        UPDATE users
        SET password = admin_password_hash
        WHERE id = admin_user_id;
    END IF;

    INSERT INTO users_roles (user_id, role_id)
    SELECT admin_user_id, r.id
    FROM roles r
    WHERE r.code = 'admin'
    ON CONFLICT (user_id, role_id) DO NOTHING;
END $$;
