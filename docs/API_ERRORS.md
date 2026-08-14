# CleanCity API Error Catalog

Every error response uses this body:

```json
{
  "isSuccess": false,
  "status": 401,
  "errorCode": "AUTH_002",
  "error": "Invalid Credentials",
  "message": "The email or password you entered is incorrect. Please try again.",
  "timestamp": "2026-08-11T12:10:00.123Z",
  "path": "/auth/login",
  "details": [
    {
      "field": "email",
      "message": "Email is required",
      "rejectedValue": null
    }
  ]
}
```

| Field | Meaning |
|-------|---------|
| `isSuccess` | Always `false` for errors |
| `status` | HTTP status code |
| `errorCode` | Stable machine code — match on this in the app |
| `error` | Short title for UI headers / toasts |
| `message` | Clear explanation of what went wrong (show this to the user) |
| `timestamp` | ISO-8601 UTC time |
| `path` | Request path that failed |
| `details` | Optional field-level validation errors (`null` when not applicable) |

---

## Auth (`AUTH_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `AUTH_001` | 401 | Unauthorized | Authentication is required. Please sign in and send a valid Bearer token. |
| `AUTH_002` | 401 | Invalid Credentials | The email or password you entered is incorrect. Please try again. |
| `AUTH_003` | 403 | Access Denied | You do not have permission to perform this action. |
| `AUTH_004` | 401 | Invalid Refresh Token | The refresh token is invalid or has already been used. Please sign in again. |
| `AUTH_005` | 401 | Refresh Token Expired | Your session has expired. Please sign in again. |
| `AUTH_006` | 409 | Email Already Registered | An account with this email already exists. Try signing in or use a different email. |
| `AUTH_007` | 409 | Phone Already Registered | An account with this phone number already exists. Use a different phone number. |
| `AUTH_008` | 403 | Account Inactive | This account is inactive, suspended, or pending activation. Contact support if you need help. |
| `AUTH_009` | 409 | Already a Driver | This account already has the driver role. |
| `AUTH_010` | 401 | Invalid Token | The access token is invalid or malformed. Please sign in again. |

## Reports / Complaints (`REPORT_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `REPORT_001` | 404 | Report Not Found | No report was found for the given ID. |
| `REPORT_002` | 409 | Report Not Pending | Only reports in PENDING status can be assigned to a driver. |
| `REPORT_003` | 409 | Report Not Awaiting Review | This report is not awaiting admin review, so it cannot be approved or rejected. |
| `REPORT_004` | 409 | Report Already Approved | This report is already approved and cannot be rejected. |
| `REPORT_005` | 400 | Completion Image Missing | A completion photo is required before this report can be approved. |
| `REPORT_006` | 400 | Invalid Report ID | The report ID format is invalid. Expected a UUID. |
| `REPORT_007` | 404 | Complaint Not Found | No complaint was found for the given ID. |
| `REPORT_008` | 400 | Invalid Complaint ID | The complaint ID format is invalid. Expected a UUID. |
| `REPORT_009` | 409 | Report Not Assigned | This report must be in ASSIGNED status before it can be completed. |

## Drivers (`DRIVER_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `DRIVER_001` | 404 | Driver Not Found | No driver profile was found for this account. |
| `DRIVER_002` | 403 | Driver Inactive | This driver account is inactive and cannot perform driver actions. |
| `DRIVER_003` | 400 | Driver ID Required | Admin assignment requires a driverId in the request body. |
| `DRIVER_004` | 403 | Driver Cannot Approve | Drivers cannot approve reports. Upload a completion photo and wait for admin review. |
| `DRIVER_005` | 403 | Driver Not Approved | Your driver account is pending approval or was rejected. You cannot take driver actions yet. |

## Users / Profile (`USER_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `USER_001` | 404 | User Not Found | No user profile was found for this account. |
| `USER_002` | 400 | Nothing to Update | Provide at least one profile field to update: name, address, phone, or profileImage. |
| `USER_003` | 400 | Invalid Name | Name must be between 2 and 100 characters. |
| `USER_004` | 400 | Invalid Profile Image | Profile image must be a valid https URL. |

## Validation (`VALID_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `VALID_001` | 400 | Validation Failed | One or more request fields are invalid. Check the details for each field. |
| `VALID_002` | 400 | Invalid Coordinates | Latitude must be between -90 and 90, and longitude between -180 and 180. |
| `VALID_003` | 400 | Invalid Latitude | Latitude is required and must be a number between -90 and 90. |
| `VALID_004` | 400 | Invalid Longitude | Longitude is required and must be a number between -180 and 180. |
| `VALID_005` | 400 | Invalid Timestamp | Timestamp is required, must not be in the future, and must be within the last year. |
| `VALID_006` | 400 | Description Too Long | Description must be at most 2000 characters. |
| `VALID_007` | 400 | Image Required | An image file is required and cannot be empty. |
| `VALID_008` | 400 | Invalid Image Type | Only JPEG and PNG images are allowed. |
| `VALID_009` | 400 | Invalid Image Content | The uploaded file is not a valid JPEG or PNG image. |

## System (`SYS_xxx`)

| Code | HTTP | error | message |
|------|------|-------|---------|
| `SYS_001` | 500 | Internal Server Error | Something went wrong on our side. Please try again later. |
| `SYS_002` | 413 | File Too Large | The uploaded file exceeds the maximum allowed size of 5MB. |
| `SYS_003` | 429 | Too Many Requests | You have made too many requests. Please wait a minute and try again. |
| `SYS_004` | 404 | Not Found | The requested resource was not found. |
| `SYS_005` | 409 | Conflict | The request could not be completed because of a conflict with the current resource state. |
| `SYS_006` | 400 | Bad Request | The request could not be understood or is missing required data. |
