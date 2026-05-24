-- UPDATE reports
-- SET assigned_driver_id = 'ae15367c-57b3-4c48-8d6c-37f71c72c426'::uuid,
--     assigned_at = now(),
--     status = 'ASSIGNED',
--     updated_at = now()
-- WHERE id = '4add0380-ec99-4840-9844-88fc313fff2c'::uuid
--   AND status = 'PENDING'
--   AND assigned_driver_id IS NULL
-- RETURNING id, assigned_driver_id, status, assigned_at, updated_at;

-- SELECT * from reports;
-- SELECT * from accounts;
-- SELECT * from users;
SELECT * from account_roles;
-- DELETE from users WHERE name ='Driver';

-- UPDATE users set name = 'Sample2' WHERE name = 'Admin';

-- SELECT * from drivers;
-- UPDATE users set email = 'driver123@email.com' where id = 'c009587b-206f-4744-b9d4-cece27af69d5';

-- SELECT * from report_assignments;

-- SELECT * from refresh_tokens;

-- SELECT * from user_devices;

-- SELECT tablename
-- FROM pg_tables
-- WHERE schemaname = 'public';

-- TRUNCATE TABLE
-- user_devices,
-- users,
-- refresh_tokens,
-- reports,
-- report_assignments,
-- drivers,
-- accounts
-- RESTART IDENTITY CASCADE;


-- ALTER TABLE users ADD COLUMN reports_filed INTEGER DEFAULT 0 NOT NULL;
-- ALTER TABLE users ADD COLUMN reports_resolved INTEGER DEFAULT 0 NOT NULL;


-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'accounts';

-- SELECT conname, pg_get_constraintdef(oid)
-- FROM pg_constraint
-- WHERE conrelid = 'reports'::regclass;

-- ALTER TABLE reports DROP CONSTRAINT reports_status_check;

-- ALTER TABLE reports ADD CONSTRAINT reports_status_check
-- CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED'));


-- WITH new_account AS (
--     INSERT INTO accounts (id, email, phone, password, status, created_at, updated_at)
--     VALUES (
--         gen_random_uuid(), 
--         'admin@cleancity.com', -- Email address
--         NULL,                  -- Phone number (optional)
--         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- BCrypt hash for 'password'
--         'ACTIVE', 
--         NOW(), 
--         NOW()
--     )
--     RETURNING id
-- ),
-- new_admin_role AS (
--     INSERT INTO account_roles (id, account_id, role)
--     SELECT gen_random_uuid(), id, 'ADMIN'
--     FROM new_account
-- ),
-- new_user_role AS (
--     INSERT INTO account_roles (id, account_id, role)
--     SELECT gen_random_uuid(), id, 'USER'
--     FROM new_account
-- )
-- INSERT INTO users (id, account_id, name, address, profile_image, is_verified, reward_points, reports_filed, reports_resolved, created_at, updated_at)
-- SELECT 
--     gen_random_uuid(), 
--     id, 
--     'Admin User',             -- Display Name
--     'Clean City Headquarters', -- Address
--     NULL,                      -- Profile Image
--     TRUE,                      -- Is Verified
--     0,                         -- Reward Points
--     0,                         -- Reports Filed
--     0,                         -- Reports Resolved
--     NOW(), 
--     NOW()
-- FROM new_account;