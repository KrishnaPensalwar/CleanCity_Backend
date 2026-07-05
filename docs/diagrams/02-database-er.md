# Database ER Diagram

> All PostgreSQL tables, columns, data types, and relationships.

---

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e3f2fd",
    "primaryBorderColor": "#1565c0",
    "primaryTextColor": "#0d1b2a",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "17px"
  }
}}%%

erDiagram

    accounts {
        UUID   id              PK
        STRING email           UK
        STRING phone           UK
        STRING password
        ENUM   status          "ACTIVE | PENDING | SUSPENDED"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    account_roles {
        BIGINT id              PK
        UUID   account_id      FK
        ENUM   role            "USER | DRIVER | ADMIN"
    }

    users {
        UUID    id             PK
        UUID    account_id     FK
        STRING  name
        STRING  address
        STRING  profile_image
        BOOLEAN is_verified
        INT     reward_points
        INT     reports_filed
        INT     reports_resolved
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    drivers {
        UUID    id                  PK
        UUID    account_id          FK
        STRING  license_number
        STRING  vehicle_number
        ENUM    approval_status     "PENDING | APPROVED | REJECTED"
        BOOLEAN is_active
        STRING  zone
        STRING  vehicle_type
        STRING  shift_time
        DOUBLE  rating
        INT     streak_days
        INT     total_tasks
        INT     completion_percentage
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    refresh_tokens {
        BIGINT    id          PK
        UUID      account_id  FK
        STRING    token       UK
        TIMESTAMP expiry_date
    }

    user_devices {
        BIGINT  id          PK
        STRING  user_id     "account_id as string"
        STRING  device_id
        STRING  fcm_token
        STRING  device_name
        ENUM    platform    "ANDROID | IOS | WEB"
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    reports {
        UUID      id                    PK
        STRING    user_id               "account_id as string"
        STRING    image_url
        BIGINT    timestamp
        DOUBLE    latitude
        DOUBLE    longitude
        ENUM      status                "PENDING | ASSIGNED | AWAITING_REVIEW | APPROVED | REJECTED"
        UUID      driver_id             FK
        TIMESTAMP assigned_at
        UUID      completed_by_driver_id
        TIMESTAMP completed_at
        STRING    completion_image_url
        DOUBLE    confidence
        STRING    labels
        STRING    description
        BIGINT    version               "Optimistic lock"
        TIMESTAMP created_at
    }

    report_assignments {
        UUID      id              PK
        UUID      report_id       FK
        STRING    action          "ASSIGNED | APPROVED | REJECTED | COMPLETED"
        UUID      actor_driver_id
        UUID      actor_user_id
        STRING    notes
        TIMESTAMP created_at
    }

    accounts        ||--o{  account_roles       : "has roles"
    accounts        ||--o|  users               : "has profile"
    accounts        ||--o|  drivers             : "has profile"
    accounts        ||--o{  refresh_tokens      : "has sessions"
    accounts        ||--o{  user_devices        : "has devices"
    drivers         ||--o{  reports             : "assigned to"
    reports         ||--o{  report_assignments  : "audit trail"
```

---

## Table Descriptions

### `accounts` — Authentication & Identity
Central login table. Every human actor (user, driver, admin) has exactly one account.

### `account_roles` — Role Assignments
Many-to-many bridge between accounts and roles. A single account can have multiple roles (e.g. `USER` + `DRIVER`).

### `users` — Citizen Profile
Stores public-facing citizen data: name, address, reward points, report stats. Linked 1-to-1 with `accounts`.

### `drivers` — Sanitation Driver Profile
Driver-specific data: vehicle info, zone, rating, task completion metrics. Linked 1-to-1 with `accounts`.

### `refresh_tokens` — Session Management
JWT refresh tokens. Each login session gets one token; logout deletes it.

### `user_devices` — FCM Push Targets
Stores Firebase Cloud Messaging tokens registered by the mobile app. Used to send push notifications.

### `reports` — Complaint Reports
Core table. Each row is one complaint submitted by a citizen. Tracks full lifecycle from `PENDING` to `APPROVED`/`REJECTED`.

### `report_assignments` — Audit Trail
Immutable log of every action taken on a report (assign, approve, reject, complete). Used for accountability.

---

## Key Relationships

| Relationship | Type | Details |
|-------------|------|---------|
| account → account_roles | One-to-Many | One account can have multiple roles |
| account → users | One-to-One | Each citizen account has one profile |
| account → drivers | One-to-One | Each driver account has one profile |
| account → refresh_tokens | One-to-Many | Multiple active sessions allowed |
| account → user_devices | One-to-Many | Multiple devices per user |
| driver → reports | One-to-Many | One driver can be assigned many reports |
| report → report_assignments | One-to-Many | Full audit trail per report |

---

[← Back to Diagrams Index](./README.md)
