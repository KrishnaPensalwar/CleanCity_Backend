# Sequence Diagram — Full Complaint Lifecycle

> End-to-end flow from a citizen submitting a complaint to receiving a push notification when it's resolved.

---

## 1. User Registration & Login

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f4fd",
    "primaryBorderColor": "#1976d2",
    "primaryTextColor": "#1a1a2e",
    "secondaryColor": "#e8f5e9",
    "secondaryBorderColor": "#388e3c",
    "activationBorderColor": "#1976d2",
    "activationBkgColor": "#e3f2fd",
    "sequenceNumberColor": "#ffffff",
    "actorBkg": "#e3f2fd",
    "actorBorder": "#1565c0",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "labelBoxBkgColor": "#e8f4fd",
    "labelBoxBorderColor": "#1976d2",
    "labelTextColor": "#1a1a2e",
    "loopTextColor": "#1a1a2e",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "noteTextColor": "#1a1a2e",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor U as 📱 User (App)
    participant API as 🌿 Spring Boot API
    participant DB as 🐘 PostgreSQL

    U->>API: POST /auth/register/user<br/>{name, email, password}
    API->>DB: INSERT accounts (email, password_hash, ACTIVE)
    API->>DB: INSERT account_roles (USER)
    API->>DB: INSERT users (name, reward_points=0)
    API-->>U: 200 OK — "User registered successfully"

    U->>API: POST /auth/login<br/>{email, password}
    API->>DB: SELECT account WHERE email=?
    API->>API: Verify BCrypt password
    API->>DB: INSERT refresh_tokens (token, expiry)
    API-->>U: 200 OK — {token, refreshToken, roles, account, profile}

    Note over U,API: User stores JWT and uses it in all future requests
```

---

## 2. Complaint Submission & AI Validation

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f4fd",
    "primaryBorderColor": "#1976d2",
    "primaryTextColor": "#1a1a2e",
    "actorBkg": "#e3f2fd",
    "actorBorder": "#1565c0",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "noteTextColor": "#1a1a2e",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor U as 📱 User (App)
    participant API as 🌿 Spring Boot API
    participant S3 as 🗂️ AWS S3
    participant Vision as 👁️ Cloud Vision
    participant DB as 🐘 PostgreSQL

    U->>API: POST /api/reports<br/>(multipart: image + lat + lon + description)<br/>Authorization: Bearer JWT

    API->>API: Validate JWT → extract accountId

    API->>S3: Upload image file
    S3-->>API: Return public image URL

    API->>Vision: Send image bytes for label detection
    Vision-->>API: Return labels + confidence score

    alt Image contains waste/garbage
        API->>DB: INSERT reports (userId, imageUrl, lat, lon, labels, confidence, PENDING)
        API-->>U: 201 Created — {report with status: PENDING}
    else Image fails validation
        API-->>U: 422 — Image does not appear to contain waste
    end

    Note over U,DB: Report is now in PENDING state awaiting admin action
```

---

## 3. Admin Reviews & Assigns Driver

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#fce4ec",
    "primaryBorderColor": "#c62828",
    "primaryTextColor": "#1a1a2e",
    "actorBkg": "#fce4ec",
    "actorBorder": "#c62828",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "noteTextColor": "#1a1a2e",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor A as 🖥️ Admin
    participant API as 🌿 Spring Boot API
    participant DB as 🐘 PostgreSQL

    A->>API: GET /api/reports?status=PENDING<br/>Authorization: Bearer JWT (ADMIN role)
    API->>DB: SELECT reports WHERE status=PENDING
    DB-->>API: List of pending reports
    API-->>A: 200 OK — [{report1}, {report2}, ...]

    A->>API: GET /api/driver/active<br/>Authorization: Bearer JWT (ADMIN role)
    API->>DB: SELECT drivers WHERE is_active=true AND approval_status=APPROVED
    DB-->>API: List of active drivers
    API-->>A: 200 OK — [{driver1}, {driver2}, ...]

    A->>API: POST /api/driver/reports/{reportId}/assign<br/>{driverId: "uuid"}<br/>Authorization: Bearer JWT (ADMIN role)
    API->>DB: UPDATE reports SET status=ASSIGNED, driver_id=? WHERE id=? AND status=PENDING
    API->>DB: INSERT report_assignments (ASSIGNED, actorUserId=adminId)
    DB-->>API: Updated report
    API-->>A: 200 OK — {report with status: ASSIGNED}

    Note over A,DB: Driver is now assigned; report status = ASSIGNED
```

---

## 4. Driver Completes the Task

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f5e9",
    "primaryBorderColor": "#2e7d32",
    "primaryTextColor": "#1a1a2e",
    "actorBkg": "#e8f5e9",
    "actorBorder": "#2e7d32",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "noteTextColor": "#1a1a2e",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor D as 🚛 Driver (App)
    participant API as 🌿 Spring Boot API
    participant S3 as 🗂️ AWS S3
    participant DB as 🐘 PostgreSQL

    D->>API: GET /api/driver/reports/assigned<br/>Authorization: Bearer JWT (DRIVER role)
    API->>DB: SELECT reports WHERE driver_id=? AND status=ASSIGNED
    DB-->>API: Assigned reports
    API-->>D: 200 OK — [{report1}, ...]

    D->>API: POST /api/driver/reports/{id}/completion-photo<br/>(multipart: image)<br/>Authorization: Bearer JWT (DRIVER role)
    API->>S3: Upload completion photo
    S3-->>API: Completion image URL
    API->>DB: UPDATE reports SET completion_image_url=?, status=AWAITING_REVIEW
    API-->>D: 200 OK — {report with status: AWAITING_REVIEW}

    Note over D,DB: Report is now AWAITING_REVIEW — admin must approve/reject
```

---

## 5. Admin Approves → User Receives Push Notification

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#fce4ec",
    "primaryBorderColor": "#c62828",
    "primaryTextColor": "#1a1a2e",
    "actorBkg": "#fce4ec",
    "actorBorder": "#c62828",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "noteTextColor": "#1a1a2e",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor A as 🖥️ Admin
    actor U as 📱 User (App)
    participant API as 🌿 Spring Boot API
    participant DB as 🐘 PostgreSQL
    participant FCM as 🔔 Firebase FCM

    A->>API: POST /api/reports/{id}/approve<br/>Authorization: Bearer JWT (ADMIN role)

    API->>DB: SELECT report WHERE id=? AND status=AWAITING_REVIEW
    API->>DB: UPDATE reports SET status=APPROVED
    API->>DB: INSERT report_assignments (APPROVED, actorUserId=adminId)

    API->>DB: UPDATE users SET reward_points += 10, reports_resolved += 1
    API->>DB: SELECT user_devices WHERE user_id=accountId AND is_active=true
    DB-->>API: [fcm_token1, fcm_token2, ...]

    loop For each active FCM token
        API->>FCM: Send message {title: "Complaint Approved", body: "...", reportId}
        FCM-->>U: 🔔 Push notification delivered
    end

    API-->>A: 200 OK — {report with status: APPROVED}

    Note over U: User sees "Complaint Approved" push notification<br/>and their reward points increased by 10
```

---

## 6. Token Refresh & Logout

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f4fd",
    "primaryBorderColor": "#1976d2",
    "primaryTextColor": "#1a1a2e",
    "actorBkg": "#e3f2fd",
    "actorBorder": "#1565c0",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    autonumber
    actor U as 📱 User (App)
    participant API as 🌿 Spring Boot API
    participant DB as 🐘 PostgreSQL

    Note over U,API: JWT expires after 15 minutes

    U->>API: POST /auth/refresh<br/>{refreshToken: "..."}
    API->>DB: SELECT refresh_tokens WHERE token=? AND expiry > NOW()
    alt Token valid
        API->>API: Generate new JWT (15 min)
        API-->>U: 200 OK — {token, refreshToken}
    else Token expired
        API-->>U: 401 — Refresh token expired. Please log in again.
    end

    U->>API: POST /auth/logout<br/>{refreshToken: "..."}
    API->>DB: DELETE refresh_tokens WHERE token=?
    API-->>U: 200 OK — "Logged out successfully"
```

---

[← Back to Diagrams Index](./README.md)
