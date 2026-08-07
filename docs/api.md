# REST API

## 1. Contract conventions

- Base path: `/api/v1`
- Data format: JSON unless otherwise stated.
- Authentication: Google OAuth login followed by a database-backed HTTP session.
- Browser requests that require authentication include credentials.
- Timestamps: ISO 8601 strings.
- Identifiers: opaque values; clients must not infer meaning from their format.
- Successful controller responses use an `ApiResponse` wrapper with `success`,
  `data`, and `timestamp`.
- Errors use RFC 9457-style `application/problem+json`.
- Authorization: enforced by Spring Security and service-level ownership/scope
  checks.
- Interactive contract: the running backend exposes its generated OpenAPI/Swagger
  documentation when enabled for the environment.

This document describes the application-level contract. The generated OpenAPI
document is authoritative for exact field schemas implemented by a particular
revision.

## 2. Roles

| Label | Meaning |
|---|---|
| Public | No application session required. |
| Authenticated | Either application role. |
| Patient | Authenticated user with role `PATIENT` and, where required, a completed patient profile. |
| Representative | Authenticated user with role `REPRESENTATIVE`; results and writes are restricted to assigned hospitals. |

Representative scope is checked per target resource. Having the representative
role does not grant access to every hospital.

## 3. OAuth and session endpoints

The Spring Security OAuth routes do not use the `/api/v1` prefix.

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/oauth2/authorization/google` | Start Google authorization. | Public |
| `GET` | `/login/oauth2/code/google` | Google callback handled by Spring Security. | Public/provider |
| `GET` | `/api/v1/auth/me` | Return the current safe user, role, and profile-completion state. | Authenticated |
| `GET` | `/api/v1/auth/csrf` | Return the current CSRF token/header metadata. | Authenticated |
| `POST` | `/api/v1/auth/logout` | Invalidate the server-side session and expire the cookie. | Authenticated |

The callback is not called directly by application JavaScript. Google redirects
to it after authorization.

### Current-user response concept

```json
{
  "success": true,
  "data": {
    "id": "opaque-user-id",
    "email": "patient@example.com",
    "displayName": "Priya Sharma",
    "role": "PATIENT",
    "profileCompleted": true
  },
  "timestamp": "2026-07-23T09:30:00Z"
}
```

OAuth provider subject identifiers, tokens, representative invitation metadata,
and session identifiers are never returned.

## 4. Profile endpoints

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/profile` | Read the current user's role-specific profile. | Authenticated |
| `PATCH` | `/api/v1/profile` | Update permitted current-user profile fields. | Authenticated |

Patient-editable fields include date of birth, contact number, blood group, and
permitted display information. Role, representative assignments, provider
subject, and audit fields cannot be changed through this endpoint.

### Patient profile update concept

```json
{
  "dateOfBirth": "2001-08-14",
  "contactNumber": "+91-9876543210",
  "bloodGroup": "O+"
}
```

## 5. Discovery endpoints

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/doctors` | Search and filter doctors. | Authenticated |
| `GET` | `/api/v1/doctors/{doctorId}` | Read doctor and hospital details. | Authenticated |
| `GET` | `/api/v1/doctors/{doctorId}/slots` | Get valid available appointment slots. | Authenticated |
| `GET` | `/api/v1/hospitals` | Search/list hospitals. | Authenticated |
| `GET` | `/api/v1/hospitals/{hospitalId}` | Read hospital detail and availability summary. | Authenticated |
| `GET` | `/api/v1/hospitals/emergency-availability` | Find hospitals with the requested bed type and available doctors. | Authenticated |
| `GET` | `/api/v1/ambulances` | List available ambulance services, optionally by hospital/location. | Authenticated |

### Common doctor query parameters

| Parameter | Meaning |
|---|---|
| `specialization` | Exact or normalized specialization filter. |
| `hospitalId` | Limit to one hospital. |
| `availabilityStatus` | `AVAILABLE` or `UNAVAILABLE`. |
| `wardType` | Optionally require an `ICU` or `GENERAL` available-bed context. |
| `query` | Supported name/text search. |
| `page`, `size`, `sort` | Pagination and allowed sort fields. |

Sort fields are validated against the endpoint-specific allowlists in the
pagination section. A field outside that list returns `400 INVALID_SORT`.

### Slot query parameters

| Parameter | Required | Meaning |
|---|---:|---|
| `date` | Yes | Requested local appointment date (`YYYY-MM-DD`). |

The server calculates slots from doctor working hours, current availability,
existing active appointments, slot duration, application timezone, and the
current time.

### Emergency availability query parameters

| Parameter | Required | Meaning |
|---|---:|---|
| `wardType` | Yes | `ICU` or `GENERAL`. |
| `query` | No | Search hospital name/address text. |

An empty result is a successful `200` response with an empty collection, not a
`404`.

## 6. Patient appointment endpoints

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/appointments` | List the current patient's appointments. | Patient |
| `POST` | `/api/v1/appointments` | Book an appointment. | Patient |
| `PATCH` | `/api/v1/appointments/{appointmentId}/cancel` | Cancel the current patient's appointment. | Patient/owner |

The backend derives the patient profile from the authenticated user. A request
body never accepts a patient identifier.

The list supports `status`, `dateFrom`, `dateTo`, `startsAfter`, and
Spring-style `page`, `size`, and `sort` pagination parameters. `startsAfter` is
an ISO-8601 offset date-time and is useful for exact upcoming views.

For filtering and responses, a stored `BOOKED` appointment whose end time has
passed is treated as effectively `COMPLETED`. This preserves the immutable
booking history without requiring a background status-update job.

`status=BOOKED` therefore returns only future/in-progress stored bookings whose
end time has not passed; `status=COMPLETED` includes stored `COMPLETED` rows and
elapsed stored `BOOKED` rows. When both `dateFrom` and `startsAfter` are supplied,
the later lower bound is used. `dateTo` includes the named local date by applying
an exclusive boundary at the next local day.

### Booking request concept

```json
{
  "doctorId": "opaque-doctor-id",
  "startsAt": "2026-07-30T10:00:00+05:30"
}
```

The server verifies that:

- the profile is complete;
- the doctor exists and is available;
- the time is in the future and within working hours;
- the slot aligns with supported slot boundaries;
- the doctor has no conflicting active appointment.

A client does not supply `hospitalId`; the backend derives the authoritative
hospital from the selected doctor.

A successful creation returns `201 Created`. A concurrency conflict returns
`409 Conflict`; the UI should refresh the slot list.

### Cancellation request concept

```json
{
  "reason": "Schedule changed"
}
```

Cancellation updates lifecycle fields. It does not delete the appointment row.
Cancelling an appointment in a terminal state produces a validation or conflict
response.

## 7. Patient ambulance endpoints

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/ambulance-requests` | List the current patient's request history. | Patient |
| `POST` | `/api/v1/ambulance-requests` | Create a request for a hospital. | Patient |

### Request concept

```json
{
  "hospitalId": "opaque-hospital-id",
  "pickupAddress": "Ganeshguri, Guwahati",
  "contactNumber": "+91-9876543210"
}
```

The feature is an application request tracker, not a guarantee of emergency
dispatch. The user interface must display appropriate emergency-service guidance
for the deployment location.

The implemented API is **intake and patient-owned history only**: `POST` creates
a `PENDING` request and `GET` lists only the authenticated patient's requests.
It does not dispatch an ambulance, assign one, change ambulance availability, or
advance/cancel request status. The nullable `ambulance` response field remains
`null` unless a later controlled integration supplies an assignment.

## 8. Representative endpoints

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/representative/dashboard` | Operational counts for assigned hospitals. | Representative |
| `GET` | `/api/v1/representative/doctors` | List doctors within assigned hospitals. | Representative |
| `PATCH` | `/api/v1/representative/doctors/{doctorId}/availability` | Change doctor availability. | Representative/scoped |
| `GET` | `/api/v1/representative/beds` | List/filter beds within assigned hospitals. | Representative |
| `PATCH` | `/api/v1/representative/beds/{bedId}/status` | Change bed status. | Representative/scoped |
| `GET` | `/api/v1/representative/appointments` | List appointments for assigned hospitals. | Representative |
| `PATCH` | `/api/v1/representative/appointments/{appointmentId}/cancel` | Operationally cancel an appointment with a reason. | Representative/scoped |

An optional `hospitalId` filter must be one of the current representative's
assignments. Supplying another hospital returns `403`, not an empty response that
could make authorization mistakes difficult to detect.

Representative list filters are:

- doctors: `query`, `hospitalId`, `availabilityStatus`, and pagination;
- beds: `hospitalId`, `wardType`, `status`, and pagination;
- appointments: `hospitalId`, `status`, `dateFrom`, `dateTo`, `startsAfter`,
  and pagination.

The representative appointment list uses the same effective-status and date
boundary semantics as the patient list. All results remain restricted to the
representative's assigned hospitals.

### Doctor availability update

```json
{
  "availabilityStatus": "UNAVAILABLE"
}
```

### Bed status update

```json
{
  "status": "OCCUPIED"
}
```

### Representative cancellation

```json
{
  "reason": "Doctor unavailable"
}
```

These mutations write an audit event containing the actor, action, target,
timestamp, and a minimal safe detail string. Doctor and bed updates include
their previous and new status; cancellation events record the reason.

## 9. Pagination

A paginated collection uses a stable shape such as:

```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-23T09:30:00Z"
}
```

The backend enforces a maximum page size of 100 and a short endpoint-specific sort
allowlist. Unsupported or nested sort fields return `400 INVALID_SORT`; they do
not reach JPA as arbitrary property paths.

| Endpoint | Allowed sort properties |
|---|---|
| `/doctors`, `/representative/doctors` | `id`, `name`, `specialization`, `availabilityStatus` |
| `/hospitals`, `/hospitals/emergency-availability` | `id`, `name` |
| `/ambulances` | `id`, `vehicleNumber`, `lastLocation`, `status` |
| `/appointments`, `/representative/appointments` | `id`, `startsAt`, `status` |
| `/ambulance-requests` | `id`, `requestedAt`, `status` |
| `/representative/beds` | `id`, `bedCode`, `wardType`, `status` |

## 10. Success status codes

| Code | Use |
|---:|---|
| `200 OK` | Successful read, update, cancellation, or logout response. |
| `201 Created` | Appointment or ambulance request created. |

## 11. Error contract

Errors use one safe, machine-readable shape:

```json
{
  "type": "https://medical-aid-tracker.dev/problems/appointment_slot_unavailable",
  "title": "Conflict",
  "status": 409,
  "detail": "The selected appointment slot is no longer available.",
  "instance": "/api/v1/appointments",
  "code": "APPOINTMENT_SLOT_UNAVAILABLE",
  "timestamp": "2026-07-23T09:30:00Z"
}
```

For validation:

```json
{
  "type": "https://medical-aid-tracker.dev/problems/validation_failed",
  "title": "Bad Request",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/profile",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-07-23T09:30:00Z",
  "fieldErrors": {
    "dateOfBirth": "must be a past date"
  }
}
```

| HTTP status | Typical code | Meaning |
|---:|---|---|
| `400` | `VALIDATION_FAILED` | Request syntax or field values are invalid. |
| `401` | `AUTHENTICATION_REQUIRED` | Session is missing or expired. |
| `403` | `ACCESS_DENIED` | Role, ownership, or hospital scope check failed. |
| `404` | `RESOURCE_NOT_FOUND` | Requested visible resource does not exist. |
| `409` | `APPOINTMENT_SLOT_UNAVAILABLE`, `APPOINTMENT_NOT_CANCELLABLE` | Current state conflicts with the requested action. |
| `500` | `INTERNAL_ERROR` | Unexpected server failure; safe message only. |

Database messages, stack traces, SQL, OAuth tokens, and secrets never appear in
the API response.

## 12. Authentication and CSRF behavior

- Frontend API requests include cookies using `credentials: "include"`.
- The session cookie is `HttpOnly`; JavaScript cannot read it.
- The frontend first calls `GET /api/v1/auth/csrf`, then sends the returned,
  session-bound token in the response's `headerName` header (currently
  `X-CSRF-TOKEN`) for state-changing requests. The token is stored with the
  server-side HTTP session; frontend JavaScript does not need to read a
  cross-domain cookie.
- CORS allows only configured frontend origins and explicitly permits credentialed
  requests.
- Logout invalidates the server-side session, not merely local frontend state.

## 13. Idempotency and concurrency

- Repeating a cancellation on an already-cancelled resource does not restore or
  duplicate it; it returns the current resource or a consistent conflict.
- Representative status updates are safe to repeat with the same target state.
- Appointment creation pessimistically locks the doctor row, rechecks overlap,
  and inserts within the same transaction.
- A client receiving `409` must refresh server state before retrying.

## 14. Example end-to-end interaction

1. Browser starts `/oauth2/authorization/google`.
2. After callback and redirect, it calls `GET /api/v1/auth/me`.
3. A first-time patient calls `PATCH /api/v1/profile`.
4. It searches `GET /api/v1/doctors?specialization=Cardiologist`.
5. It requests `GET /api/v1/doctors/{id}/slots?date=2026-07-30`.
6. It books with `POST /api/v1/appointments`.
7. It displays the authoritative response and later lists appointments with
   `GET /api/v1/appointments`.

## 15. Contract evolution

- Backward-compatible fields may be added within `/api/v1`.
- Removing/renaming a field or changing semantics requires a coordinated version
  change.
- Database entities are never exposed directly, which allows internal schema
  evolution without coupling every column to the public API.
- Generated OpenAPI output should be reviewed in CI or during release preparation
  so that implemented routes and this narrative remain aligned.
