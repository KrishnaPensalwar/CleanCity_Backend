# Clean City — Authentication Architecture

Production-grade role-based authentication with a centralized `accounts` table, multi-role support, and profile separation.

---

## 1. Recommended Folder Structure

```
src/main/java/com/cleancity/backend/
├── auth/
│   ├── domain/           # Account, AccountRole, enums
│   ├── dto/              # Register*, LoginResponse, MeResponse, AccountDto
│   ├── repository/       # AccountRepository, AccountRoleRepository
│   ├── security/         # AccountDetailsImpl, AccountDetailsService
│   └── service/          # AuthService (registration, login, convert-to-driver)
├── controller/           # AuthController (thin HTTP layer)
├── entity/               # User (profile), Driver (profile), RefreshToken
├── repository/           # UserRepository, DriverRepository, RefreshTokenRepository
├── security/
│   ├── jwt/              # JwtUtils, AuthTokenFilter, entry/denied handlers
│   └── WebSecurityConfig.java
├── exception/            # ErrorCode, ErrorResponse, ApiException, GlobalExceptionHandler
└── service/              # Domain services (Report, Driver, User, …)

src/main/resources/db/migration/   # Flyway SQL migrations
```

---

## 2. Database Schema

```sql
accounts
├── id UUID PK
├── email VARCHAR UNIQUE NOT NULL
├── phone VARCHAR UNIQUE
├── password VARCHAR NOT NULL        -- BCrypt hash
├── status VARCHAR NOT NULL          -- ACTIVE | INACTIVE | PENDING | SUSPENDED
├── created_at, updated_at

account_roles
├── id UUID PK
├── account_id UUID FK → accounts
├── role VARCHAR NOT NULL            -- USER | DRIVER | ADMIN
└── UNIQUE (account_id, role)

users (profile — citizen)
├── id UUID PK
├── account_id UUID FK UNIQUE → accounts
├── name, address, profile_image
├── reward_points, reports_filed, reports_resolved, is_verified
└── created_at, updated_at

drivers (profile — driver)
├── id UUID PK
├── account_id UUID FK UNIQUE → accounts
├── license_number, vehicle_number
├── approval_status                  -- PENDING | APPROVED | REJECTED
├── zone, vehicle_type, shift_time, rating, …
└── created_at, updated_at

refresh_tokens
├── id UUID PK
├── account_id UUID FK → accounts
├── token, expiry, created_at
```

---

## 3. Entity Models

| Entity | Package | Purpose |
|--------|---------|---------|
| `Account` | `auth.domain` | Single login identity |
| `AccountRole` | `auth.domain` | Many roles per account |
| `User` | `entity` | Citizen profile linked 1:1 to account |
| `Driver` | `entity` | Driver profile linked 1:1 to account |
| `RefreshToken` | `entity` | Session refresh linked to account |

---

## 4. DTOs

| DTO | Use |
|-----|-----|
| `RegisterUserRequest` | POST `/auth/register/user` |
| `RegisterDriverRequest` | POST `/auth/register/driver` |
| `ConvertToDriverRequest` | POST `/auth/convert-to-driver` |
| `LoginRequest` | POST `/auth/login` |
| `LoginResponse` | Login success (token + roles + account) |
| `MeResponse` | GET `/auth/me` |
| `AccountDto` | Account summary in responses |
| `TokenRefreshRequest/Response` | Refresh flow |

---

## 5. Repository Layer

| Repository | Key methods |
|------------|-------------|
| `AccountRepository` | `findByEmail`, `existsByEmail`, `existsByPhone` |
| `AccountRoleRepository` | `findByAccountId`, `existsByAccountIdAndRole` |
| `UserRepository` | `findByAccountId`, `findByAccountEmail` |
| `DriverRepository` | `findByAccountId`, `findByIsActiveTrue` |
| `RefreshTokenRepository` | `findByToken`, `deleteByAccount` |

---

## 6. Service Layer

**`AuthService`** — single entry for all auth operations:

- `registerUser()` — account + USER role + user profile
- `registerDriver()` — account + USER + DRIVER roles + both profiles
- `convertToDriver()` — add DRIVER role + driver profile to existing account
- `login()` — authenticate, issue JWT + refresh token, return all roles
- `refreshToken()` — rotate refresh token
- `logout()` — invalidate refresh tokens
- `getMe()` — account + roles + profiles

---

## 7. Authentication Flow

```
Client → POST /auth/login { email, password }
       → AuthenticationManager
       → AccountDetailsService.loadUserByUsername(email)
       → AccountRepository + roles
       → AccountDetailsImpl (multi-role authorities)
       → JwtUtils.generateJwtToken()  → access token (roles[], accountId)
       → RefreshTokenService.createRefreshToken(accountId)

Protected request → AuthTokenFilter
                  → validate JWT → load AccountDetailsImpl
                  → SecurityContextHolder
                  → @PreAuthorize("hasRole('DRIVER')") etc.
```

---

## 8. JWT Middleware

- **`AuthTokenFilter`** — parses `Authorization: Bearer`, validates, loads account
- **`JwtUtils`** — HS256, claims: `sub` (email), `accountId`, `roles[]`
- **`AuthEntryPointJwt`** — 401 JSON (`AUTH_001`)
- **`AuthAccessDeniedHandler`** — 403 JSON (`AUTH_003`)

---

## 9. Registration APIs

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register/user` | New citizen account |
| POST | `/auth/register/driver` | New driver (USER + DRIVER roles) |
| POST | `/auth/convert-to-driver` | Upgrade existing USER to DRIVER |
| POST | `/auth/signup` | Legacy alias (deprecated) |

---

## 10. Login API

**POST `/auth/login`**

Request:
```json
{ "email": "user@example.com", "password": "secret123" }
```

Response:
```json
{
  "token": "eyJhbG...",
  "refreshToken": "uuid-refresh-token",
  "roles": ["USER", "DRIVER"],
  "account": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "phone": null,
    "status": "ACTIVE"
  },
  "profile": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Krishna",
    "email": "user@example.com",
    "roles": ["USER", "DRIVER"],
    "rewardPoints": 10
  }
}
```

---

## 11. Role Authorization Middleware

Spring Security `@EnableMethodSecurity` + `@PreAuthorize`:

```java
@PreAuthorize("hasRole('ADMIN')")           // admin only
@PreAuthorize("hasRole('DRIVER')")           // driver only
@PreAuthorize("hasRole('USER')")            // any logged-in user with USER role
@PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
@PreAuthorize("isAuthenticated()")          // any valid token
```

Roles in JWT map to `ROLE_USER`, `ROLE_DRIVER`, `ROLE_ADMIN` authorities.

---

## 12. Example API Responses

**Duplicate email (409)**
```json
{
  "isSuccess": false,
  "status": 409,
  "errorCode": "AUTH_006",
  "error": "Email Already Registered",
  "message": "An account with this email already exists. Try signing in or use a different email.",
  "timestamp": "2026-08-11T12:10:00.123Z",
  "path": "/auth/register/user"
}
```

**Invalid credentials (401)**
```json
{
  "isSuccess": false,
  "status": 401,
  "errorCode": "AUTH_002",
  "error": "Invalid Credentials",
  "message": "The email or password you entered is incorrect. Please try again.",
  "timestamp": "2026-08-11T12:10:00.123Z",
  "path": "/auth/login"
}
```

**GET `/auth/me`**
```json
{
  "account": { "id": "...", "email": "...", "status": "ACTIVE" },
  "roles": ["USER", "DRIVER"],
  "userProfile": { "name": "Krishna", "rewardPoints": 10 },
  "driverProfile": { "id": "...", "licenseNumber": "DL-123", "approvalStatus": "PENDING" }
}
```

---

## 13. Best Practices

1. **Single source of truth** — credentials only in `accounts`; never duplicate email/password in profile tables.
2. **Multi-role accounts** — one login for USER + DRIVER; JWT carries all roles.
3. **Profile separation** — gamification stats on `users`; operational data on `drivers`.
4. **Flyway migrations** — schema versioned; Hibernate `ddl-auto=validate`.
5. **Refresh token rotation** — old token deleted on refresh.
6. **Normalized email** — always `trim().toLowerCase()` on write.
7. **Account status gate** — `PENDING` / `SUSPENDED` accounts cannot authenticate.
8. **Use account ID** — reports, devices, and audit fields reference `account_id`, not profile IDs.

---

## 14. Migration Strategy (Old → New)

Flyway scripts (run automatically on startup):

| Version | Script | Action |
|---------|--------|--------|
| V1 | `create_accounts_tables` | Create `accounts`, `account_roles` |
| V2 | `add_account_link_columns` | Add `account_id` to users, drivers, refresh_tokens |
| V3 | `migrate_legacy_auth_data` | Copy users/drivers → accounts; merge duplicate emails; map reports/devices/tokens |
| V4 | `finalize_auth_schema` | Drop legacy email/password/role columns; add FK constraints |

**Merge rules:**
- Legacy `users` row → new `accounts` row + `USER`/`ADMIN` role
- Legacy `driver` with same email → link to existing account + add `DRIVER` role
- Driver-only (no user) → new account (status `PENDING`, placeholder password) + driver profile + user profile stub

**After migration:** restart app; all existing users log in with same email/password; drivers with merged accounts gain both roles.

---

## 15. Production Checklist

- [ ] Set strong `JWT_SECRET` in environment (not default)
- [ ] Use HTTPS in production
- [ ] Rotate refresh tokens (already implemented)
- [ ] Monitor failed login rates
- [ ] Admin approval workflow for `drivers.approval_status`
- [ ] Password reset flow for migrated driver-only accounts (`PENDING` status)
- [ ] Backup DB before running first Flyway migration on production

---

## Quick Reference — Endpoints

| Method | Path | Auth |
|--------|------|------|
| POST | `/auth/register/user` | Public |
| POST | `/auth/register/driver` | Public |
| POST | `/auth/convert-to-driver` | USER |
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/logout` | Public |
| GET | `/auth/me` | Authenticated |
| PUT | `/api/users/me` | Authenticated — update name, address, phone, profileImage |
