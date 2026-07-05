# CleanCity Backend — Architecture Diagrams

> All diagrams render natively on GitHub. Click any diagram title to open the full view.

---

## Table of Contents

| # | Diagram | Description |
|---|---------|-------------|
| 1 | [System Architecture](./01-system-architecture.md) | High-level view of all system components and their connections |
| 2 | [Database ER Diagram](./02-database-er.md) | All database tables, columns, and relationships |
| 3 | [Sequence Diagram](./03-sequence-diagram.md) | Step-by-step complaint lifecycle from submission to resolution |
| 4 | [API Request-Response Flow](./04-api-flow.md) | All REST API endpoints grouped by role and module |
| 5 | [Deployment Diagram](./05-deployment.md) | Infrastructure on Render, AWS S3, Firebase, and Google Cloud |

---

## App Overview

**CleanCity** is a civic complaint management platform that lets residents report garbage/cleanliness issues via a mobile app. Reports are validated by AI, assigned to sanitation drivers, and resolved with photographic proof — with push notifications at every step.

### User Roles

| Role | Capabilities |
|------|-------------|
| **USER** | Register, login, submit complaints with photo + GPS, view own reports, earn reward points |
| **DRIVER** | Login, view assigned reports, upload completion photo, mark complete |
| **ADMIN** | View all reports, assign drivers, approve/reject reports, view driver stats |

### Report Lifecycle

```
PENDING → ASSIGNED → AWAITING_REVIEW → APPROVED / REJECTED
```

| Status | Meaning |
|--------|---------|
| `PENDING` | Submitted by user, not yet assigned |
| `ASSIGNED` | Driver assigned, work in progress |
| `AWAITING_REVIEW` | Driver uploaded completion photo |
| `APPROVED` | Admin approved — user gets +10 reward points + FCM notification |
| `REJECTED` | Admin rejected — user gets FCM notification |

---

## Quick Links

- [Auth Architecture](../AUTH_ARCHITECTURE.md)
- [Backend Flows](../backend-flows.md)
