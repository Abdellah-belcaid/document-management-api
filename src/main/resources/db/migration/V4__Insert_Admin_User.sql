-- Add admin user if not already exists
DO
$$
BEGIN
    IF
NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin') THEN
        INSERT INTO users (id, name, email, username, password, enabled, role)
        VALUES (
            gen_random_uuid(),
            'Admin Account',
            'admin@gmail.com',
            'admin',
            '$2a$10$ZsfMEuDy/afNbXd1lnOLruCMko8i98k5jjKu6GeZVaDqqo.iYQWAa', --code : belcaid2001
            true,
            'ADMIN'
        );
END IF;
END $$;
