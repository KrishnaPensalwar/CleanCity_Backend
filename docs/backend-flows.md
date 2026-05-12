# CleanCity Backend — Flow Reference

This document explains the main backend flows in sentences, step-by-step, with 1–2 line code examples showing the key hand-offs. It also lists where data comes from, where it is stored, what happens on success and failure, and any recommended improvements or missing pieces.

---

## Overview
The application is a Spring Boot REST API. Controllers receive HTTP requests, perform coarse validation, and call Services. Services implement business logic and use Spring Data JPA Repositories to persist entities. File uploads go to S3 via `S3StorageService`; only the returned URL is stored in the database. Security is JWT-based and enforced by an authentication filter and `@PreAuthorize` annotations.

---

## 1) Authentication (signup / login / refresh)

What triggers it
- Client calls `POST /api/auth/signup` or `POST /api/auth/login`.

Flow — signup (step-by-step)
1. `AuthController.registerUser(SignupRequest)` validates the payload.
2. Controller (or a service) constructs a `User` entity, hashes password, sets optional `role` and `cityId`.
3. `userRepository.save(user)` persists the user.
4. Controller returns a `MessageResponse` with success or failure message.

Where data is stored
- `users` table: `id`, `email`, `password_hash`, `role`, `city_id`, `reward_points`, `created_at`, `updated_at`.

Code snippet (save user)
- `User saved = userRepository.save(newUser);`

Success
- Returns 200/201 with confirmation JSON.

Failure
- Duplicate email → 400 with message; DB constraint errors or validation failures → 400/500.

Missing / improvements
- Add email verification, rate limiting, and persistent refresh tokens (a `refresh_tokens` table) to allow secure token rotation.

---

## 2) Login and JWT generation

What triggers it
- `POST /api/auth/login` with credentials.

Flow
1. `AuthController.authenticateUser(LoginRequest)` calls `AuthenticationManager.authenticate(...)`.
2. If authentication succeeds, `JwtUtils.generateJwtToken(authentication)` creates an access JWT with a `roles` claim.
3. Controller returns `AuthResponse` containing `accessToken`, `refreshToken` (if implemented), and `UserDto`.

Key line
- `String jwt = jwtUtils.generateJwtToken(authentication);`

Success
- 200 with JWT and user profile.

Failure
- Invalid credentials → 401.

Missing / improvements
- Implement refresh token storage with rotation and revocation.

---

## 3) Report creation (user uploads original garbage photo)

What triggers it
- Client `POST /api/reports` with multipart form data (`ReportRequestDto`) containing `image`, latitude, longitude, optional description.

Flow (detailed)
1. `ReportController.uploadReport(@ModelAttribute ReportRequestDto dto)` receives a multipart request.
2. Controller forwards to `ReportService.createReport(dto, userId)`.
3. Service calls `s3StorageService.uploadFile(image)` to upload the image to S3 and receives `imageUrl`.
4. Service optionally runs ML validation (sets `confidence`, `labels`).
5. Service constructs a `Report` entity (userId, imageUrl, timestamp, lat/lon, status=PENDING) and calls `reportRepository.save(report)`.
6. A `ReportResponse` DTO is created and returned to client.

Where data is stored
- `reports` table columns: `id`, `user_id`, `image_url`, `timestamp`, `latitude`, `longitude`, `status`, `completion_image_url`, `assigned_driver_id`, `created_at`, `completed_at`, `confidence`, `labels`.

Key lines
- `String imageUrl = s3StorageService.uploadFile(image);`
- `Report saved = reportRepository.save(report);`

Success
- 201/200 with `ReportResponse` containing `imageUrl` and meta fields.

Failure
- S3 upload error → 500, invalid/missing fields → 400, DB constraint violation → 500/400.

Missing / improvements
- Validate file type and size; reject unsupported formats early. Consider returning a 202 and processing ML asynchronously to improve responsiveness.

---

## 4) Admin: list/get reports (getAllReports)

What triggers it
- `GET /api/reports` (admin-only, optional `?status=` filter).

Flow
1. `ReportController.getAllReports(status)` calls `ReportService.getAllReports(status)`.
2. Service queries `ReportRepository` for the requested reports and maps each `Report` to `ReportResponse` (includes `completionImageUrl`).
3. Controller returns the JSON array.

Key line
- `return reportRepository.findByStatus(status).stream().map(ReportResponse::new).collect(...)`.

Success
- 200 with array of report DTOs.

Failure
- 403 if not admin; DB errors → 500.

Missing / improvements
- Add pagination and additional filters (cityId, date range). Use DTO projection in repo to avoid loading entire entity when not needed.

---

## 5) Driver: nearby discovery

What triggers it
- `GET /api/driver/reports/nearby?lat=...&lon=...&radiusMeters=...` (driver or admin).

Flow
1. Controller extracts parameters and calls `DriverService.findNearby(lat, lon, radius, limit)`.
2. Service computes a bounding box for latitude/longitude and calls a native query `reportRepository.findNearbyPending(...)` which returns rows (`id`, `lat`, `lon`, `distance`).
3. For each returned id the service loads the `Report` and maps to `ReportResponse`.
4. The service returns a list of DTOs.

Key lines
- `List<Object[]> rows = reportRepository.findNearbyPending(...);`
- `Report rep = reportRepository.findById(id).orElse(null);`

Success
- 200 with list of nearby `ReportResponse` items.

Failure
- Invalid params → 400; slow DB query → 500.

Missing / improvements
- Use PostGIS or a single query to fetch full rows instead of an id + findById to avoid N+1. Add spatial indexes and limit distance computations.

---

## 6) Assignment (driver self-assign and admin assign)

What triggers it
- `POST /api/driver/reports/{reportId}/assign`
  - Drivers call with only `note` (they are the assigner).
  - Admins call with `{"driverId":"<uuid>","note":"..."}` to assign to any driver.

Flow (step-by-step)
1. Controller receives `idStr` path parameter and body JSON.
2. Controller parses `idStr` into `UUID reportId` and chooses `driverId`: if caller has `ROLE_ADMIN`, parse `driverId` from body; otherwise use authenticated user's id.
3. Controller calls `driverService.assignReport(reportId, driverId, note)`.
4. Service calls a native update `reportRepository.assignIfPending(reportId, driverId)` which does an atomic SQL `UPDATE reports SET assigned_driver_id=?, status='ASSIGNED' WHERE id=? AND status='PENDING' AND assigned_driver_id IS NULL`.
5. If update count == 0, assignment failed (another actor assigned first) → service throws `IllegalStateException` and controller returns 409.
6. If success, Service creates a `ReportAssignment` audit record and returns `ReportResponse`.

Key lines
- `int updated = reportRepository.assignIfPending(reportId, driverId);`
- `if (updated == 0) throw new IllegalStateException("Report already assigned");`

Where data is stored
- `reports.assigned_driver_id`, `reports.status` set to `ASSIGNED`.
- `report_assignments` audit table gets an `ASSIGNED` row.

Success
- 200 with updated `ReportResponse` (status and assignedDriverId present).

Failure
- 409 if already assigned; 404 if report missing; 400 if invalid driverId passed by admin; 403 if driver not allowed.

Missing / improvements
- Validate that `driverId` exists in `drivers` table before assignment; send notification to driver on assignment.

---

## 7) Driver uploads completion photo (awaiting admin review)

What triggers it
- `POST /api/driver/reports/{reportId}/completion-photo` with multipart `image`.

Flow
1. Controller parses `reportId` and validates the authenticated driver.
2. `DriverService.uploadCompletionPhoto(reportId, driverId, image)` ensures the driver is the assigned driver and report is in `ASSIGNED` state.
3. Service uploads the image to S3 (`s3StorageService.uploadFile(image)`), receives `completionImageUrl`.
4. Service sets `report.completionImageUrl = completionImageUrl`, `completedAt`, `completedByDriverId = driverId`, `status = AWAITING_REVIEW` and saves report.
5. Service creates a `ReportAssignment` audit row with action `COMPLETION_UPLOADED`.
6. Return `ReportResponse` (which includes `completionImageUrl`).

Key lines
- `String url = s3StorageService.uploadFile(image);`
- `r.setCompletionImageUrl(url); r.setStatus(ReportStatus.AWAITING_REVIEW); reportRepository.save(r);`

Success
- 200 with updated `ReportResponse` containing `completionImageUrl`.

Failure
- 403 if driver is not assigned; 400 invalid file; 500 if S3 upload error.

Missing / improvements
- Save thumbnail or low-res preview for admin, validate file type/size, and consider resumable uploads for large files.

---

## 8) Admin approve / reject (final review)

What triggers it
- `POST /api/reports/{id}/approve` (admin)
- `POST /api/reports/{id}/reject` (admin)

Flow (approve)
1. Controller calls `reportService.approveReport(reportId)`.
2. Service checks `report.status == AWAITING_REVIEW` (or allowed states), sets `status = APPROVED`, sets `completedAt` if missing, and awards reward points to the original uploader by updating `users.reward_points`.
3. Service saves the `Report`, and writes an approval row to `report_assignments` with action `APPROVED` and actor=admin.
4. Controller returns the updated `ReportResponse`.

Key lines
- `r.setStatus(ReportStatus.APPROVED); user.setRewardPoints(user.getRewardPoints() + 10); userRepository.save(user); reportRepository.save(r);`

Success
- 200 with final `ReportResponse` and uploader's points increased.

Failure
- 409 if state invalid; 404 if report not found.

Missing / improvements
- Record approval user id and timestamp separately; make awarding points idempotent (avoid double awarding on retry).

---

## 9) Driver completes a task (non-approval actions)

What triggers it
- `POST /api/driver/reports/{id}/complete` with `{ "action": "REJECTED" }` (drivers cannot set `APPROVED`).

Flow
1. Controller disallows `APPROVED` actions from drivers (403).
2. Service verifies assignment and state, updates `Report.status` to `REJECTED` (or other action), sets `completedByDriverId`, `completedAt`, saves and logs an audit row.

Key line
- `r.setStatus(ReportStatus.REJECTED); reportRepository.save(r);`

Success
- 200 with `ReportResponse`.

Failure
- 403 if driver not assigned; 409 if wrong state.

---

## 10) Storage — S3 details

What it does
- Accepts `MultipartFile`, constructs a key (e.g., `reports/{uuid}.jpg`) and `putObject` to S3, returning a public or presigned URL.

Key line
- `String url = s3StorageService.uploadFile(file);`

Where data is stored
- File bytes in S3 bucket; returned URL stored in `reports.image_url` or `reports.completion_image_url`.

Failure
- S3 network or credentials failure → 500.

Missing / improvements
- Consider presigned short-lived URLs and a CDN; store object metadata (content-type, size).

---

## 11) Security — JWT / filters / roles

How it works
1. `AuthTokenFilter` reads the `Authorization: Bearer <token>` header, validates signature via `JwtUtils`, extracts username and roles and builds a `UserDetailsImpl`.
2. The filter sets `SecurityContextHolder.getContext().setAuthentication(authentication)` so controllers can use `@PreAuthorize` and `Authentication` injection.

Key line
- `SecurityContextHolder.getContext().setAuthentication(authentication);`

Failure
- Missing or invalid token → request rejected (401). Missing role → 403 when hitting `@PreAuthorize`.

Missing / improvements
- Key rotation, storing JWT expiration and token revocation list for critical flows.

---

## 12) Persistence, atomic updates and concurrency

Important patterns
- `assignIfPending` native update ensures one writer wins when multiple drivers try to assign the same report.
- `@Version` optimistic locking used on `Report` for other concurrent updates.

Key line
- `int updated = reportRepository.assignIfPending(reportId, driverId);`

Failure
- If updated == 0 → a concurrent assign happened and caller should handle 409.

Missing / improvements
- Add DB-level constraints and explicit migrations (Flyway) for deterministic schema changes.

---

## 13) DTOs, mapping and API exposure

Notes
- Controllers return DTOs (`ReportResponse`, `DriverDto`, `AuthResponse.UserDto`) and do not expose internal entity objects directly.
- `ReportResponse` now includes `completionImageUrl` so admins can see driver uploads.

Key mapping example
- `return new ReportResponse(report); // ReportResponse copies report.getCompletionImageUrl()`

Failure
- If a DTO lacks a field, clients can't see that data. Always keep DTOs in sync with required response data.

---

## 14) Logs, audit and error handling

What exists
- `ReportAssignment` audit table records actor, action and notes.
- `AuthAccessDeniedHandler` returns JSON for 403s.

Missing / improvements
- Add `@ControllerAdvice` to standardize error JSON shapes across endpoints. Add structured logs (request id, user id) for tracing.

---

## 15) Practical example: full assign-by-admin flow (concise)

1. Admin calls:
```bash
POST /api/driver/reports/<reportId>/assign
{ "driverId":"<driverUuid>", "note":"Please handle" }
```
2. Controller parses `reportId` and `driverId` (UUIDs). It calls `driverService.assignReport(reportId, driverId, note)`.
3. Service executes `assignIfPending`. If success, it saves an `ASSIGNED` state and an assignment audit row.
4. Response: 200 with `ReportResponse` (contains assignedDriverId, status=ASSIGNED).

---

## 16) Missing items & roadmap (summary)
- Add Flyway migrations and include a migration to add `completion_image_url`, new driver columns, and audit table schema.
- Add unit and integration tests around assignment concurrency and admin approval awarding points.
- Add validation for uploads (size/type) and sanitize inputs.
- Add notifications to drivers on assignment and to users on approval.
- Add pagination and filtering for admin listing endpoints.

---

If you want, I will commit this file to `docs/backend-flows.md` in the repository now (so it appears in the project). I can also append example cURL commands for each flow or add a small sequence diagram image if you'd like.
