# Deployment Diagram

> How CleanCity Backend is deployed across Render, AWS, Google Cloud, and Firebase.

---

## Infrastructure Overview

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
    "lineColor": "#546e7a",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#fafafa",
    "clusterBorder": "#bdbdbd",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

graph TB
    subgraph USERS["👥  End Users"]
        MOBILE["📱 Android App\n(Citizens & Drivers)"]
        ADMINS["🖥️ Admin\n(Browser / HTTP Client)"]
    end

    subgraph RENDER["☁️  Render.com"]
        subgraph CONTAINER["🐳 Docker Container\neclipse-temurin:17-jdk"]
            BOOT["🌿 Spring Boot 3\nJava 17\nPort: 8080\n(exposed via PORT env var)"]
            GRADLE["🔨 Built with Gradle\n./gradlew bootJar"]
        end

        subgraph RENDER_DB["🐘 Render PostgreSQL"]
            PG["PostgreSQL 15\nManaged by Render\nAuto-backups"]
        end
    end

    subgraph AWS["☁️  AWS"]
        S3["🗂️ S3 Bucket\ncleancity-reports\nRegion: ap-south-1\nStores complaint photos\n& completion photos"]
    end

    subgraph GCP["☁️  Google Cloud Platform"]
        VISION["👁️ Cloud Vision API\nLabel Detection\nWaste image validation"]
        FCM["🔔 Firebase FCM\nCloud Messaging\nPush notifications"]
    end

    MOBILE -->|"HTTPS\napi.render.com"| BOOT
    ADMINS -->|"HTTPS\napi.render.com"| BOOT

    BOOT -->|"JDBC over TLS\nDB_URL env var"| PG

    BOOT -->|"AWS SDK v2\nS3 PutObject"| S3
    S3 -->|"Public URL returned"| BOOT

    BOOT -->|"Vision API gRPC\nCredentials from env"| VISION
    VISION -->|"Labels + confidence"| BOOT

    BOOT -->|"Firebase Admin SDK\nHTTPS"| FCM
    FCM -->|"FCM Push"| MOBILE
```

---

## Docker Build & Run Flow

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
    "fontSize": "13px"
  }
}}%%

flowchart LR
    A["📁 Git Push\nto main branch"] --> B["🔄 Render detects\nDockerfile"]
    B --> C["docker build\nFROM eclipse-temurin:17-jdk\nWORKDIR /app\nCOPY . .\nRUN chmod +x gradlew\nRUN ./gradlew bootJar --no-daemon"]
    C --> D["📦 JAR artifact\nbuild/libs/*.jar"]
    D --> E["▶️ Container starts\njava -jar build/libs/*.jar\nEXPOSE 8080"]
    E --> F["🌐 Render routes\nPort 8080 → Public HTTPS URL"]
```

---

## Environment Variables on Render

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#fff8e1",
    "primaryBorderColor": "#f57f17",
    "primaryTextColor": "#0d1b2a",
    "lineColor": "#455a64",
    "edgeLabelBackground": "#ffffff",
    "clusterBkg": "#fffde7",
    "clusterBorder": "#ffe082",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

flowchart TD
    subgraph REQUIRED["🔴 Required Environment Variables"]
        DB_URL["DB_URL\njdbc:postgresql://host/db"]
        DB_USER["DB_USERNAME\nDB_PASSWORD"]
        JWT["JWT_SECRET\n(long random string)"]
        S3B["AWS_S3_BUCKET_NAME"]
        S3K["AWS_ACCESS_KEY_ID\nAWS_SECRET_ACCESS_KEY"]
        FIREBASE["FIREBASE_SERVICE_ACCOUNT_JSON\n(full JSON — one line, minified)"]
    end

    subgraph OPTIONAL["🟡 Optional Environment Variables"]
        PORT["PORT (default: 8080)"]
        REGION["AWS_REGION (default: ap-south-1)"]
        JWTEXP["JWT_EXPIRATION_MS (default: 900000)"]
        JWTREF["JWT_REFRESH_EXPIRATION_MS (default: 604800000)"]
        GCREDS["GOOGLE_APPLICATION_CREDENTIALS_JSON\n(if different from Firebase)"]
        SHOWSQL["SHOW_SQL (default: false)"]
    end

    subgraph SPRING["🌿 Spring Boot application.properties"]
        SP1["spring.datasource.url=${DB_URL}"]
        SP2["app.jwtSecret=${JWT_SECRET}"]
        SP3["aws.s3.bucket-name=${AWS_S3_BUCKET_NAME}"]
        SP4["firebase.service-account.json=${FIREBASE_SERVICE_ACCOUNT_JSON}"]
    end

    DB_URL --> SP1
    JWT --> SP2
    S3B --> SP3
    FIREBASE --> SP4
```

---

## Production URL & Endpoints

| Resource | Value |
|----------|-------|
| **Base URL** | `https://cleancity-backend-au86.onrender.com` |
| **Health** | `GET /` (Spring Boot default) |
| **Auth** | `/auth/login`, `/auth/register/user`, `/auth/register/driver` |
| **Reports** | `/api/reports` |
| **Drivers** | `/api/driver/all` |
| **Devices** | `/api/devices/register` |

---

## Startup & Initialization Sequence

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "background": "#ffffff",
    "primaryColor": "#e3f2fd",
    "primaryBorderColor": "#1565c0",
    "primaryTextColor": "#0d1b2a",
    "actorBkg": "#e3f2fd",
    "actorBorder": "#1565c0",
    "actorTextColor": "#0d1b2a",
    "actorLineColor": "#455a64",
    "signalColor": "#37474f",
    "signalTextColor": "#37474f",
    "noteBorderColor": "#ff9800",
    "noteBkgColor": "#fff8e1",
    "fontFamily": "Segoe UI, Arial, sans-serif",
    "fontSize": "13px"
  }
}}%%

sequenceDiagram
    participant JVM as ☕ JVM
    participant SPRING as 🌿 Spring Context
    participant DB as 🐘 PostgreSQL
    participant S3C as 🗂️ S3 Client
    participant FB as 🔔 Firebase

    JVM->>SPRING: Start Spring Boot application
    SPRING->>DB: Test JDBC connection (DB_URL env)
    DB-->>SPRING: Connection established

    Note over SPRING,DB: JPA creates / validates tables (ddl-auto=create)

    SPRING->>S3C: Initialize AmazonS3Client\n(AWS_ACCESS_KEY_ID + AWS_SECRET_ACCESS_KEY)
    S3C-->>SPRING: S3 client ready

    SPRING->>FB: FirebaseConfig @PostConstruct\nRead FIREBASE_SERVICE_ACCOUNT_JSON

    alt JSON env var present and valid
        FB-->>SPRING: Firebase initialized successfully ✅
    else JSON missing or empty
        FB-->>SPRING: Firebase skipped — push notifications disabled ⚠️
    else JSON malformed
        FB-->>SPRING: Failed to initialize Firebase ❌ (app may fail to start)
    end

    SPRING-->>JVM: Application started on port 8080 🚀
```

---

## Cost & Scaling Notes

| Service | Plan | Notes |
|---------|------|-------|
| Render Web Service | Free / Starter | Spins down after 15 min inactivity on free tier |
| Render PostgreSQL | Free | 90-day expiry on free tier; upgrade for production |
| AWS S3 | Pay-as-you-go | Very low cost for image storage |
| Google Cloud Vision | Free tier | 1,000 units/month free |
| Firebase FCM | Free | No cost for push notifications |

> **Note:** On Render's free tier, the first request after idle may take 30–60 seconds (cold start). Upgrade to a paid plan for always-on service.

---

[← Back to Diagrams Index](./README.md)
