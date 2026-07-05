# API Request-Response Flow Diagram

> All REST API endpoints, access control, and response shapes grouped by module.

---

## Access Control Legend

| Symbol | Meaning |
|--------|---------|
| 🌐 | Public — no authentication required |
| 🔑 | Authenticated — any valid JWT |
| 👤 | USER role required |
| 🚛 | DRIVER role required |
| 🛡️ | ADMIN role required |
| 🔑🚛 | DRIVER or ADMIN |

---

## API Overview Flow

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e3f2fd",
    "primaryBorderColor": "#1565c0",
    "primaryTextColor": "#0d1b2a",
    "secondaryColor": "#e8f5e9",
    "secondaryBorderColor": "#2e7d32",
    "tertiaryColor": "#fff3e0",
    "tertiaryBorderColor": "#e65100",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#fafafa",
    "clusterBorder": "#bdbdbd",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart TD
    REQ(["📨 Incoming Request"])

    REQ --> FILTER["🛡️ JWT Auth Filter\nExtract & Validate Bearer Token"]

    FILTER -->|"No token / invalid"| E401["❌ 401 Unauthorized\n{error: AUTH_001}"]
    FILTER -->|"Valid token"| ROUTER["🔀 Route to Controller"]

    ROUTER --> AUTH_MOD["🔐 /auth/**\n(Public endpoints)"]
    ROUTER --> REPORT_MOD["📋 /api/reports/**"]
    ROUTER --> DRIVER_MOD["🚛 /api/driver/**"]
    ROUTER --> DEVICE_MOD["📲 /api/devices/**"]
    ROUTER --> CITY_MOD["🏙️ /api/city/**"]

    AUTH_MOD --> AUTH_ENDPOINTS["POST /auth/register/user\nPOST /auth/register/driver\nPOST /auth/login\nPOST /auth/refresh\nPOST /auth/logout\nGET  /auth/me"]

    REPORT_MOD --> REPORT_ENDPOINTS["POST   /api/reports             🔑\nGET    /api/reports             🛡️\nGET    /api/reports/me          🔑\nPOST   /api/reports/:id/approve 🛡️\nPOST   /api/reports/:id/reject  🛡️"]

    DRIVER_MOD --> DRIVER_ENDPOINTS["GET  /api/driver/all                    🛡️\nGET  /api/driver/active                🛡️\nGET  /api/driver/top                   🛡️\nGET  /api/driver/zone?zone=X           🛡️\nGET  /api/driver/reports/nearby        🔑🚛\nGET  /api/driver/reports/assigned      🔑🚛\nGET  /api/driver/reports/profile       🔑🚛\nPOST /api/driver/reports/:id/assign    🔑🚛\nPOST /api/driver/reports/:id/complete  🔑🚛\nPOST /api/driver/reports/:id/completion-photo  🔑🚛"]

    DEVICE_MOD --> DEVICE_ENDPOINTS["POST   /api/devices/register  🔑\nDELETE /api/devices/:deviceId 🔑"]

    CITY_MOD --> CITY_ENDPOINTS["GET /api/city/rankings\nGET /api/complaints/:id"]
```

---

## Auth Endpoints — `/auth/**`

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
    "clusterBkg": "#f0f7ff",
    "clusterBorder": "#90caf9",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart LR
    subgraph REG["Registration"]
        R1["POST /auth/register/user\n🌐 Public\n→ {name, email, password}"]
        R2["POST /auth/register/driver\n🌐 Public\n→ {name, email, password,\nlicenseNumber, vehicleNumber}"]
        R3["POST /auth/convert-to-driver\n👤 USER only\n→ {licenseNumber, vehicleNumber}"]
    end

    subgraph SESSION["Session"]
        S1["POST /auth/login\n🌐 Public\n→ {email, password}\n← {token, refreshToken, roles, account, profile}"]
        S2["POST /auth/refresh\n🌐 Public\n→ {refreshToken}\n← {token, refreshToken}"]
        S3["POST /auth/logout\n🌐 Public\n→ {refreshToken}\n← {message}"]
        S4["GET /auth/me\n🔑 Authenticated\n← {account, profile, roles}"]
    end

    R1 --> S1
    R2 --> S1
    R3 --> S1
```

---

## Report Endpoints — `/api/reports/**`

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f5e9",
    "primaryBorderColor": "#2e7d32",
    "primaryTextColor": "#0d1b2a",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#f1f8e9",
    "clusterBorder": "#a5d6a7",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart TD
    subgraph USER_FLOW["👤 Citizen Actions"]
        U1["POST /api/reports\n🔑 Authenticated\nBody: multipart/form-data\n  image (file)\n  latitude (double)\n  longitude (double)\n  description (string)\n← 201 {ReportResponse}"]

        U2["GET /api/reports/me\n🔑 Authenticated\n← 200 [{ReportResponse}]"]
    end

    subgraph ADMIN_FLOW["🛡️ Admin Actions"]
        A1["GET /api/reports\n?status=PENDING|ASSIGNED|AWAITING_REVIEW\n🛡️ ADMIN\n← 200 [{ReportResponse}]"]

        A2["POST /api/reports/:id/approve\n🛡️ ADMIN\nStatus must be AWAITING_REVIEW\n← 200 {ReportResponse with APPROVED}\nSide effects:\n  +10 reward points to user\n  FCM push notification sent"]

        A3["POST /api/reports/:id/reject\n🛡️ ADMIN\nStatus must be AWAITING_REVIEW\n← 200 {ReportResponse with REJECTED}\nSide effects:\n  FCM push notification sent"]
    end

    subgraph STATUS["Report Status Flow"]
        S1(["PENDING"]) --> S2(["ASSIGNED"])
        S2 --> S3(["AWAITING_REVIEW"])
        S3 --> S4(["APPROVED ✅"])
        S3 --> S5(["REJECTED ❌"])
    end

    U1 --> S1
    A1 --> A2
    A1 --> A3
    A2 --> S4
    A3 --> S5
```

---

## Driver Endpoints — `/api/driver/**`

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#fff3e0",
    "primaryBorderColor": "#e65100",
    "primaryTextColor": "#0d1b2a",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#fff8f0",
    "clusterBorder": "#ffcc80",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart LR
    subgraph ADMIN_ONLY["🛡️ Admin Only"]
        DA1["GET /api/driver/all\n← [{DriverDto}]"]
        DA2["GET /api/driver/active\n← [{DriverDto}]"]
        DA3["GET /api/driver/top\n← [{DriverDto}] sorted by rating"]
        DA4["GET /api/driver/zone?zone=NORTH\n← [{DriverDto}]"]
    end

    subgraph DRIVER_ACTIONS["🚛 Driver Actions (also accessible by Admin)"]
        DD1["GET /api/driver/reports/nearby\n?lat=&lon=&radiusMeters=5000&limit=50\n← [{ReportResponse}] within radius"]
        DD2["GET /api/driver/reports/assigned\n← [{ReportResponse}] assigned to me"]
        DD3["GET /api/driver/reports/profile\n← {DriverDto} my profile"]
        DD4["POST /api/driver/reports/:id/assign\nDriver: auto-assigns to self\nAdmin: body {driverId}\n← {ReportResponse with ASSIGNED}"]
        DD5["POST /api/driver/reports/:id/completion-photo\nbody: multipart image\n← {ReportResponse with AWAITING_REVIEW}"]
        DD6["POST /api/driver/reports/:id/complete\nbody: {action, notes}\n← {ReportResponse}"]
    end

    DA4 --> DD4
    DD4 --> DD5
    DD5 --> DD6
```

---

## Device & Notification Endpoints — `/api/devices/**`

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#f3e5f5",
    "primaryBorderColor": "#6a1b9a",
    "primaryTextColor": "#0d1b2a",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart LR
    APP["📱 Mobile App"]

    APP -->|"After login"| REG["POST /api/devices/register\n🔑 Authenticated\nBody:\n  deviceId (string)\n  fcmToken (string)\n  deviceName (string)\n  platform (ANDROID/IOS/WEB)\n← {deviceId, message}"]

    APP -->|"On logout or uninstall"| DEL["DELETE /api/devices/:deviceId\n🔑 Authenticated\n← 204 No Content"]

    REG --> DB[("🐘 user_devices\ntable")]
    DEL -->|"Sets is_active=false"| DB
    DB -->|"Queried on approve/reject"| FCM["🔔 Firebase FCM\nPush Notification"]
```

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "code": "REPORT_NOT_FOUND",
  "message": "Report not found",
  "status": 404,
  "timestamp": "2026-07-05T17:45:00"
}
```

| Error Code | HTTP Status | Meaning |
|-----------|------------|---------|
| `AUTH_001` | 401 | Unauthorized / invalid token |
| `AUTH_002` | 403 | Forbidden / insufficient role |
| `AUTH_003` | 409 | Email already registered |
| `REPORT_NOT_FOUND` | 404 | Report does not exist |
| `REPORT_ALREADY_APPROVED` | 409 | Report already approved |
| `REPORT_NOT_AWAITING_REVIEW` | 409 | Report not in correct state |
| `DRIVER_NOT_FOUND` | 404 | Driver profile not found |
| `DRIVER_ID_REQUIRED` | 400 | Admin must provide driverId |
| `INVALID_REPORT_ID` | 400 | Malformed UUID |
| `DRIVER_CANNOT_APPROVE` | 403 | Drivers cannot approve reports |

---

[← Back to Diagrams Index](./README.md)
