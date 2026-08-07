# Production Deployment Guide

## 1. Target topology

| Component | Platform | Exposure |
|---|---|---|
| Next.js frontend | Vercel | Public HTTPS |
| Spring Boot API | Railway | Public HTTPS |
| MySQL 8 | Railway | Private to backend where possible |

This guide uses Git-based deployment. Docker Compose is for local development;
Railway maps the backend and database to separate services rather than running the
Compose file directly.

## 2. Pre-deployment checklist

- All intended migrations are committed and reviewed.
- Production seed data contains no real patient data or hard-coded credentials.
- Backend tests, frontend lint/type checks, and production builds have been run by
  the release owner.
- The frontend API base URL is configurable.
- The backend reads the platform port and binds to all required interfaces.
- The backend health endpoint exposes only safe health information.
- OAuth consent screen, privacy contact, and production domains are ready.
- Database backup/restore responsibility is assigned.
- No `.env`, client-secret JSON, database dump with personal data, or generated
  session token is tracked in Git.

This document defines checks; it does not assert that a given deployment has
passed them.

## 3. Deploy MySQL on Railway

1. Create a Railway project and a production environment.
2. Add a MySQL service from Railway's database catalog.
3. Give the service a stable descriptive name such as `mysql`.
4. Keep the database connected to the backend through Railway service/reference
   variables and private networking where available.
5. Configure automated backups appropriate to project risk and budget.
6. Record and test the restore procedure before relying on the deployment.

Railway's MySQL service exposes connection values including `MYSQLHOST`,
`MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, and `MYSQLDATABASE`. See the current
[Railway MySQL documentation](https://docs.railway.com/databases/mysql).

The database must not be called directly by Vercel or the browser.

## 4. Deploy the Spring Boot backend on Railway

### Create the service

1. Add a service from the GitHub repository.
2. Set its root directory to `backend` for the monorepo.
3. Use the committed backend `Dockerfile` when present. Railway can detect a
   `Dockerfile` at the configured source root; see
   [Railway's Spring Boot guide](https://docs.railway.com/guides/spring-boot).
4. Generate a public domain for the API, for example:

   ```text
   https://medical-aid-api-production.up.railway.app
   ```

5. Configure the health-check path as `/actuator/health` if that endpoint is
   enabled and returns `200` when ready. Railway uses a configured health check
   during deployment activation; it is not a replacement for continuous
   monitoring. See [Railway health checks](https://docs.railway.com/deployments/healthchecks).

### Backend variables

Set/seal production values in the Railway backend service:

```dotenv
DB_URL=jdbc:mysql://<private-host>:<port>/<database>
DB_USERNAME=<reference-to-mysql-user>
DB_PASSWORD=<reference-to-mysql-password>

SPRING_PROFILES_ACTIVE=prod
GOOGLE_CLIENT_ID=<google-web-client-id>
GOOGLE_CLIENT_SECRET=<sealed-google-client-secret>

APP_FRONTEND_URL=https://<production-frontend-domain>
CORS_ALLOWED_ORIGINS=https://<production-frontend-domain>
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=None
APP_TIME_ZONE=Asia/Kolkata
```

Optional tuning variables include `SESSION_TIMEOUT`, `DB_POOL_SIZE`,
`DB_POOL_MIN_IDLE`, `APPOINTMENT_DURATION_MINUTES`, and `APP_LOG_LEVEL`.

Use Railway reference variables instead of copying database secrets. The syntax
is `${{SERVICE_NAME.VARIABLE_NAME}}`; Railway documents current behavior in
[Using Variables](https://docs.railway.com/variables).

The exact JDBC construction depends on the MySQL service's private host and
driver configuration. Validate it in a non-production Railway environment first.

The backend reads Railway's platform-provided `PORT` directly, with
`SERVER_PORT` as a local fallback. Do not hard-code `SERVER_PORT` on Railway;
allow the platform to inject `PORT` so the service listens on the assigned port.
Railway documents this under
[public networking](https://docs.railway.com/public-networking).

### Session settings

The backend stores Spring Session data in MySQL. This is required so a restart or
additional backend instance does not silently invalidate all sessions merely
because memory was lost.

For separate `vercel.app` and `railway.app` sites, credentialed browser requests
normally require:

- `Secure` cookie;
- `SameSite=None`;
- exact allowed frontend origin;
- CORS credentials enabled;
- frontend fetch requests with credentials;
- an explicit CSRF policy for state-changing requests.

A custom domain arrangement may allow a stricter `SameSite` value. Validate
cookie behavior in Chrome, Firefox, and Safari because browser privacy controls
can affect third-party/cross-site cookies. A same-site custom-domain topology,
such as `app.example.com` and `api.example.com`, is preferable for a polished
production deployment.

### Migration behavior

Flyway migrations run against the production database as configured by the
backend. Before a migration that could alter or remove data:

1. create/verify a backup;
2. review forward and rollback/restore plans;
3. deploy to a staging database;
4. inspect Flyway history and application startup;
5. then promote the same revision.

Never run `legacy/data.sql` against production.

## 5. Configure Google OAuth for production

In the Google OAuth web client, add the exact Railway backend callback:

```text
https://<backend-domain>/login/oauth2/code/google
```

Add the production frontend/backend origins where the Google client configuration
requires them. Update consent-screen branding, support email, homepage, privacy
policy, and authorized/test users as appropriate.

Google requires redirect URI equality, including scheme and path. Use HTTPS in
production and never expose the client secret to Vercel browser code. Refer to
Google's [OpenID Connect setup guide](https://developers.google.com/identity/openid-connect/openid-connect).

After changing Google credentials or URIs, redeploy/restart the backend and test a
fresh browser session rather than relying on an old cookie.

## 6. Deploy the Next.js frontend on Vercel

1. Import the same Git repository into Vercel.
2. Set the Vercel project root directory to `frontend`.
3. Confirm framework detection is Next.js.
4. Keep install/build/output settings at repository defaults unless the frontend
   explicitly requires overrides.
5. Configure:

   ```dotenv
   NEXT_PUBLIC_API_BASE_URL=https://<railway-backend-domain>
   NEXT_PUBLIC_APP_URL=https://<production-frontend-domain>
   NEXT_PUBLIC_GOOGLE_OAUTH_PATH=/oauth2/authorization/google
   ```

6. Apply the variable to Production and configure an appropriate value for
   Preview.
7. Deploy the production branch.
8. Record the stable production domain and update backend
   `APP_FRONTEND_URL`/`CORS_ALLOWED_ORIGINS`.
9. If that domain changed, update the Google OAuth configuration.
10. Redeploy both sides as needed so build-time public configuration and backend
    origin policy agree.

Vercel treats Production, Preview, and Development variables separately, and an
environment-variable change applies to new deployments rather than existing
ones. See [Vercel environment variables](https://vercel.com/docs/environment-variables).

All `NEXT_PUBLIC_*` values are browser-visible and embedded at build time.
`NEXT_PUBLIC_APP_URL` supplies production metadata/canonical URLs, and
`NEXT_PUBLIC_GOOGLE_OAUTH_PATH` may be omitted when the standard path is used.
None may contain a secret.

## 7. Preview environment policy

OAuth and strict CORS make arbitrary per-branch preview URLs difficult. Choose one
documented policy:

- maintain one stable staging frontend and staging backend/domain; or
- automate preview origin and OAuth URI registration only when governance permits;
  or
- allow UI-only previews to use mock data while full authentication is tested in
  staging.

Do not set `CORS_ALLOWED_ORIGINS=*` to make previews convenient. Vercel supports
separate pre-production deployments within a project; see
[Vercel projects](https://vercel.com/docs/projects).

## 8. Representative provisioning

The production system has no public "become a representative" toggle.
Provisioning is controlled:

1. verify the representative's institutional identity;
2. create an active email-and-hospital invitation/allowlist entry through an
   authorized operational process (the repository includes
   `backend/scripts/provision-representative.sql` as a safe operator template);
3. run the private template once for each hospital assignment required;
4. have the representative sign in with the intended Google account;
5. verify `/api/v1/auth/me` reports the correct role and no excessive assignment;
6. verify successful provisioning marked each invitation inactive, created only
   the intended assignment rows, and wrote the expected provisioning audit
   events.

Do not provision roles using a public seed migration or a shared viva password.
Copy the template outside the repository before inserting a real email, run it
with restricted database credentials, and retain the generated audit evidence.

## 9. Production verification

### Infrastructure

- Railway backend deployment is active.
- Health endpoint returns `200` without exposing database credentials/details.
- Flyway history shows the expected current version.
- Database backup policy is enabled and restore instructions are current.
- Vercel production deployment points to the intended commit.

### Authentication and browser behavior

- Google login starts from the frontend and returns to the correct dashboard.
- Session persists across page refresh and a backend restart.
- Logout invalidates the session.
- Unauthorized API request returns `401`.
- Patient request to representative API returns `403`.
- Cross-site cookie/CORS/CSRF behavior is checked in supported browsers.

### Domain behavior

- Patient sees only their own profile, appointments, and ambulance requests.
- Concurrent booking attempts cannot create two active appointments for one
  doctor/slot.
- Representative updates work for assigned hospitals.
- The same representative receives `403` for an unassigned hospital.
- Status changes and representative cancellation create audit entries.

### UX

- Desktop and mobile navigation work.
- Loading, empty, validation, conflict, and server-error states are readable.
- Keyboard navigation and visible focus are present.
- No secret or stack trace appears in browser responses.

## 10. Observability

For a student-scale deployment:

- send application logs to standard output so Railway captures them;
- expose only a minimal health endpoint publicly;
- watch deployment logs after migration/startup;
- configure an external uptime check if continuous monitoring is required;
- review authentication failures, authorization denials, and unexpected errors;
- alert manually or through the hosting service before adding a large monitoring
  stack.

Never log cookies, OAuth authorization codes, tokens, passwords, or full patient
profiles.

## 11. Backup and recovery

Document for each environment:

- backup frequency and retention;
- person responsible for recovery;
- how to create an on-demand pre-migration backup;
- how to restore into a separate verification database;
- acceptable recovery point and recovery time for a final-year demonstration;
- how Flyway history is verified after restore.

A backup that has never been restored is not proven recovery. Run a controlled
restore rehearsal before the final demonstration.

## 12. Rollback strategy

### Frontend

Promote/redeploy the previous known-good Vercel deployment if a frontend-only
revision fails.

### Backend

Redeploy the previous application image only when it remains compatible with the
current database schema. Prefer backward-compatible expand/migrate/contract
changes.

### Database

Do not attempt an improvised destructive "down" migration. Restore from the
verified backup or apply a reviewed forward-fix migration according to the
incident.

## 13. Secret rotation

If a secret is exposed:

1. revoke/rotate it at the provider immediately;
2. update the relevant Railway sealed variable;
3. redeploy the backend;
4. invalidate affected sessions if required;
5. inspect logs and repository history for exposure;
6. remove the secret from history using an approved process;
7. record the incident and prevention action.

Changing a Vercel or Railway variable does not retroactively change an already
built/running deployment; create a new deployment.

## 14. Cost and scope controls

- Start with one backend instance and one appropriately sized MySQL service.
- Use the platform logs and metrics before adding third-party observability.
- Keep development/staging resources suspended when permitted and not in use.
- Set platform spending alerts/limits where available.
- Never trade away backups or security controls merely to retain unnecessary
  preview environments.
