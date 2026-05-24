-- Central authentication tables

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
CREATE INDEX IF NOT EXISTS idx_accounts_phone ON accounts(phone);
