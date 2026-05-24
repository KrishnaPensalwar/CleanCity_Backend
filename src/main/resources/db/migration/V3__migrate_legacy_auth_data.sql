-- Migrate legacy users and drivers into accounts + account_roles
-- Safety: ensure auth tables exist (handles baseline-on-migrate skipping V1)

CREATE TABLE IF NOT EXISTS accounts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(50) UNIQUE,
    password    VARCHAR(255) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS account_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL,
    CONSTRAINT uk_account_role UNIQUE (account_id, role)
);

CREATE INDEX IF NOT EXISTS idx_account_roles_account_id ON account_roles(account_id);
CREATE INDEX IF NOT EXISTS idx_accounts_email ON accounts(email);

-- 1) Users -> accounts (one account per legacy user email)
INSERT INTO accounts (id, email, phone, password, status, created_at, updated_at)
SELECT gen_random_uuid(),
       LOWER(TRIM(u.email)),
       NULL,
       u.password_hash,
       'ACTIVE',
       COALESCE(u.created_at, NOW()),
       COALESCE(u.updated_at, NOW())
FROM users u
WHERE u.email IS NOT NULL
  AND u.account_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM accounts a WHERE LOWER(a.email) = LOWER(TRIM(u.email))
  );

UPDATE users u
SET account_id = a.id
FROM accounts a
WHERE u.account_id IS NULL
  AND u.email IS NOT NULL
  AND LOWER(TRIM(u.email)) = LOWER(a.email);

-- 2) User roles (USER or ADMIN from legacy role column)
INSERT INTO account_roles (id, account_id, role)
SELECT gen_random_uuid(), u.account_id,
       CASE
           WHEN u.role IN ('ROLE_ADMIN', 'ADMIN') THEN 'ADMIN'
           ELSE 'USER'
       END
FROM users u
WHERE u.account_id IS NOT NULL
ON CONFLICT (account_id, role) DO NOTHING;

-- 3) Drivers with existing account (same email) -> link profile
UPDATE drivers d
SET account_id = a.id
FROM accounts a
WHERE d.account_id IS NULL
  AND d.email IS NOT NULL
  AND LOWER(TRIM(d.email)) = LOWER(a.email);

-- 4) Driver-only records -> new account (requires password reset; status PENDING)
INSERT INTO accounts (id, email, phone, password, status, created_at, updated_at)
SELECT gen_random_uuid(),
       LOWER(TRIM(d.email)),
       NULLIF(TRIM(d.phone), ''),
       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
       'PENDING',
       COALESCE(d.created_at, NOW()),
       COALESCE(d.updated_at, NOW())
FROM drivers d
WHERE d.email IS NOT NULL
  AND d.account_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM accounts a WHERE LOWER(a.email) = LOWER(TRIM(d.email))
  );

UPDATE drivers d
SET account_id = a.id
FROM accounts a
WHERE d.account_id IS NULL
  AND d.email IS NOT NULL
  AND LOWER(TRIM(d.email)) = LOWER(a.email);

-- 5) DRIVER role for all linked driver profiles
INSERT INTO account_roles (id, account_id, role)
SELECT gen_random_uuid(), d.account_id, 'DRIVER'
FROM drivers d
WHERE d.account_id IS NOT NULL
ON CONFLICT (account_id, role) DO NOTHING;

-- 6) User profile for driver-only accounts (no existing users row)
INSERT INTO users (id, account_id, name, email, password_hash, role, reward_points, reports_filed, reports_resolved, is_verified, created_at)
SELECT gen_random_uuid(),
       d.account_id,
       COALESCE(d.name, 'Driver'),
       a.email,
       a.password,
       'ROLE_USER',
       0, 0, 0, FALSE,
       COALESCE(d.created_at, NOW())
FROM drivers d
JOIN accounts a ON a.id = d.account_id
WHERE d.account_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.account_id = d.account_id);

-- Cleanup rows that could not be linked
DELETE FROM refresh_tokens WHERE account_id IS NULL;
DELETE FROM drivers WHERE account_id IS NULL;

-- 7) Refresh tokens: user_id -> account_id
UPDATE refresh_tokens rt
SET account_id = u.account_id
FROM users u
WHERE rt.account_id IS NULL
  AND rt.user_id = u.id
  AND u.account_id IS NOT NULL;

-- 8) Reports: legacy user profile id -> account id
UPDATE reports r
SET user_id = u.account_id::text
FROM users u
WHERE r.user_id = u.id::text
  AND u.account_id IS NOT NULL;

-- Also map reports stored by email string
UPDATE reports r
SET user_id = a.id::text
FROM accounts a
WHERE r.user_id = a.email;

-- 9) User devices: legacy user profile id -> account id
UPDATE user_devices ud
SET user_id = u.account_id::text
FROM users u
WHERE ud.user_id = u.id::text
  AND u.account_id IS NOT NULL;

-- 10) Driver approval status from legacy is_active flag
UPDATE drivers
SET approval_status = CASE
    WHEN is_active = TRUE THEN 'APPROVED'
    ELSE 'PENDING'
END
WHERE approval_status IS NULL;

-- 11) Report assignment audit: actor_user_id user profile -> account
UPDATE report_assignments ra
SET actor_user_id = u.account_id
FROM users u
WHERE ra.actor_user_id = u.id
  AND u.account_id IS NOT NULL;
