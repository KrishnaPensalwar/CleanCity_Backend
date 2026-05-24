-- Finalize schema: remove duplicated auth columns, enforce FK constraints

-- Drop legacy auth columns from users (credentials now live in accounts)
ALTER TABLE users DROP COLUMN IF EXISTS email;
ALTER TABLE users DROP COLUMN IF EXISTS password_hash;
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Drop duplicated identity columns from drivers
ALTER TABLE drivers DROP COLUMN IF EXISTS email;
ALTER TABLE drivers DROP COLUMN IF EXISTS name;
ALTER TABLE drivers DROP COLUMN IF EXISTS phone;

-- Drop legacy refresh token FK to users
ALTER TABLE refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;
ALTER TABLE refresh_tokens DROP COLUMN IF EXISTS user_id;

-- Enforce NOT NULL only when migration completed successfully
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE account_id IS NULL) THEN
        ALTER TABLE users ALTER COLUMN account_id SET NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM drivers WHERE account_id IS NULL) THEN
        ALTER TABLE drivers ALTER COLUMN account_id SET NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM refresh_tokens WHERE account_id IS NULL) THEN
        ALTER TABLE refresh_tokens ALTER COLUMN account_id SET NOT NULL;
    END IF;
END $$;

ALTER TABLE drivers ALTER COLUMN approval_status SET DEFAULT 'PENDING';
UPDATE drivers SET approval_status = 'PENDING' WHERE approval_status IS NULL;
ALTER TABLE drivers ALTER COLUMN approval_status SET NOT NULL;

-- Foreign keys
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_users_account,
    ADD CONSTRAINT fk_users_account FOREIGN KEY (account_id) REFERENCES accounts(id);

ALTER TABLE drivers
    DROP CONSTRAINT IF EXISTS fk_drivers_account,
    ADD CONSTRAINT fk_drivers_account FOREIGN KEY (account_id) REFERENCES accounts(id);

ALTER TABLE refresh_tokens
    DROP CONSTRAINT IF EXISTS fk_refresh_tokens_account,
    ADD CONSTRAINT fk_refresh_tokens_account FOREIGN KEY (account_id) REFERENCES accounts(id);
