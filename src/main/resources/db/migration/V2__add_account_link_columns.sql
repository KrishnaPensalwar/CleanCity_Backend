-- Link profile tables to centralized accounts

ALTER TABLE users ADD COLUMN IF NOT EXISTS account_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image VARCHAR(1000);

ALTER TABLE drivers ADD COLUMN IF NOT EXISTS account_id UUID;
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_number VARCHAR(100);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'PENDING';

ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS account_id UUID;

CREATE INDEX IF NOT EXISTS idx_users_account_id ON users(account_id);
CREATE INDEX IF NOT EXISTS idx_drivers_account_id ON drivers(account_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_account_id ON refresh_tokens(account_id);
