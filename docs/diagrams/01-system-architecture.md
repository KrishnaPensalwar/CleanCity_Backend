# System Architecture Diagram

> High-level view of all CleanCity system components and how they communicate.

---

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e8f4fd",
    "primaryBorderColor": "#2196f3",
    "primaryTextColor": "#1a1a2e",
    "secondaryColor": "#e8f5e9",
    "secondaryBorderColor": "#4caf50",
    "tertiaryColor": "#fff3e0",
    "tertiaryBorderColor": "#ff9800",
    "lineColor": "#546e7a",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#f5f5f5",
    "clusterBorder": "#bdbdbd",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "14px"
  }
}}%%

graph TB
    subgraph CLIENTS["📱  Client Layer"]
        APP["📱 Mobile App\n(Android)"]
        ADMIN["🖥️ Admin Dashboard\n(Web / REST Client)"]
    end

    subgraph RENDER["☁️  Render.com — Production"]
        subgraph SPRING["🌿 Spring Boot API"]
            AUTH["🔐 Auth Module\n/auth/**"]
            REPORTS["📋 Reports Module\n/api/reports/**"]
            DRIVER["🚛 Driver Module\n/api/driver/**"]
            DEVICES["📲 Device Module\n/api/devices/**"]
            CITY["🏙️ City Module\n/api/city/**"]
        end

        subgraph SECURITY["🛡️ Security"]
            JWT["JWT Filter\nHS512 Token"]
            RBAC["Role-Based Access\nUSER / DRIVER / ADMIN"]
        end

        DB[("🐘 PostgreSQL\n(Render)")]
    end

    subgraph EXTERNAL["🌐  External Services"]
        S3["🗂️ AWS S3\nImage Storage"]
        VISION["👁️ Google Cloud Vision\nML Image Validation"]
        FCM["🔔 Firebase FCM\nPush Notifications"]
    end

    APP -->|"HTTPS + JWT"| AUTH
    APP -->|"HTTPS + JWT"| REPORTS
    APP -->|"HTTPS + JWT"| DRIVER
    APP -->|"HTTPS + JWT"| DEVICES
    ADMIN -->|"HTTPS + JWT"| REPORTS
    ADMIN -->|"HTTPS + JWT"| DRIVER

    AUTH --> SECURITY
    REPORTS --> SECURITY
    DRIVER --> SECURITY
    DEVICES --> SECURITY

    SECURITY --> JWT
    SECURITY --> RBAC

    SPRING --> DB

    REPORTS -->|"Upload photo"| S3
    REPORTS -->|"Validate image"| VISION
    REPORTS -->|"Approve/Reject\nnotification"| FCM

    FCM -->|"Push notification"| APP
```

---

## Component Descriptions

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Mobile App** | Android | Users submit complaints, drivers view/complete tasks |
| **Admin Dashboard** | REST Client / Web | Admins manage reports, drivers, approvals |
| **Spring Boot API** | Java 17, Spring Boot 3 | Central backend REST API |
| **JWT Filter** | JJWT / HS512 | Validates Bearer tokens on every request |
| **RBAC** | Spring Security `@PreAuthorize` | Enforces USER / DRIVER / ADMIN access |
| **PostgreSQL** | Render Managed DB | All persistent data (accounts, reports, etc.) |
| **AWS S3** | AWS SDK v2 | Stores complaint and completion photos |
| **Google Cloud Vision** | Vision API v1 | ML validation — confirms image contains waste |
| **Firebase FCM** | Firebase Admin SDK | Push notifications on approve/reject |

---

## Data Flow Summary

```
User submits photo → S3 (stored) → Cloud Vision (validated)
                  → PostgreSQL (saved as PENDING)

Admin assigns driver → PostgreSQL (status: ASSIGNED)

Driver completes task → S3 (completion photo) → PostgreSQL (status: AWAITING_REVIEW)

Admin approves → PostgreSQL (status: APPROVED) → FCM → User's phone notification
```

---

[← Back to Diagrams Index](./README.md)
