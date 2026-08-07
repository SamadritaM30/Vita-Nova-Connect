# Local Installation Guide

## 1. Prerequisites

Install:

- Git
- Java Development Kit 21
- Docker Desktop or MySQL 8
- Node.js 22 (the version declared by `.nvmrc`)
- npm (included with Node.js)
- a Google Cloud project for OAuth testing

Optional but useful:

- an IDE with Java and TypeScript support;
- MySQL Workbench or another SQL client;
- an API client such as Bruno, Postman, or `curl`;
- Mermaid preview support for the diagrams in `docs/`.

The repository includes Maven wrappers for Unix/macOS (`backend/mvnw`) and
Windows (`backend/mvnw.cmd`), so a machine-wide Maven installation is optional.

## 2. Obtain and inspect the project

```bash
git clone <repository-url>
cd Medical_Aid_Tracker-main
git status
```

The modern applications are in `frontend/` and `backend/`. Files under `legacy/`
are design history and are not part of the modern build.

## 3. Start MySQL

### Option A: repository Docker Compose

The repository provides a MySQL service in `compose.yaml`:

```bash
docker compose up -d mysql
docker compose ps
```

Use the database name, user, and password shown in the non-secret local example
configuration.

### Option B: existing MySQL 8

Create a database and a dedicated local application user:

```sql
CREATE DATABASE medical_aid_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'medical_aid_app'@'localhost'
  IDENTIFIED BY 'replace-with-a-local-password';

GRANT ALL PRIVILEGES ON medical_aid_tracker.*
  TO 'medical_aid_app'@'localhost';
```

Do not reuse root credentials in application configuration. Do not import
`legacy/data.sql` directly; Flyway owns the modern schema.

## 4. Configure Google OAuth

In Google Cloud:

1. Create or select a project.
2. Configure the OAuth consent/branding screen.
3. Create an OAuth client of type **Web application**.
4. Add this local authorized redirect URI exactly:

   ```text
   http://localhost:8080/login/oauth2/code/google
   ```

5. If authorized JavaScript origins are requested for the client configuration,
   add the local origins actually used by the application:

   ```text
   http://localhost:3000
   http://localhost:8080
   ```

6. Store the client ID and client secret only in local untracked environment
   configuration.

Google requires the configured redirect URI to match exactly, including scheme,
host, port, path, and trailing-slash behavior. The production URI is configured
separately. See Google's
[web-server OAuth guidance](https://developers.google.com/identity/protocols/oauth2/web-server).

Only the `openid`, `profile`, and `email` scopes are needed for sign-in. This
project does not need access to Drive, Calendar, Gmail, or other Google data.

## 5. Backend configuration

Set the following variables in the terminal/IDE run configuration. Use an
untracked local file if the repository provides a safe loader for it.

```dotenv
DB_URL=jdbc:mysql://localhost:3306/medical_aid_tracker
DB_USERNAME=medical_aid_app
DB_PASSWORD=replace-with-a-local-password

GOOGLE_CLIENT_ID=replace-with-google-client-id
GOOGLE_CLIENT_SECRET=replace-with-google-client-secret

APP_FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=Lax
APP_TIME_ZONE=Asia/Kolkata
SERVER_PORT=8080
```

Optional variables include `SESSION_TIMEOUT`, `DB_POOL_SIZE`,
`DB_POOL_MIN_IDLE`, `APPOINTMENT_DURATION_MINUTES`, and `APP_LOG_LEVEL`.

Notes:

- `DB_URL` must be a JDBC URL.
- `CORS_ALLOWED_ORIGINS` is a comma-separated allowlist when multiple origins are
  supported. Never use `*` with credentialed requests.
- `SESSION_COOKIE_SECURE=false` is local-only because local HTTP is used.
- `SESSION_COOKIE_SAME_SITE=Lax` is suitable for the local `localhost` setup.
- Never commit the real `.env`, OAuth secret, database password, session cookie,
  or a downloaded Google client-secret file.

Check backend example/property files for any additional required variable in the
current revision.

For the repository's `backend/.env.example`, a `zsh`/`bash` terminal can export
the copied values before starting Spring Boot:

```bash
cd backend
cp .env.example .env
set -a
source .env
set +a
```

Spring Boot does not load an arbitrary `.env` file by itself; use these exports
or configure the same variables in the IDE.

## 6. Run the backend

From a terminal:

```bash
cd backend
./mvnw spring-boot:run
```

On first successful connection, Flyway creates and versions the schema, including
the Spring Session tables. Do not run ad hoc schema creation scripts afterward.

Useful local URLs, when enabled:

| URL | Purpose |
|---|---|
| `http://localhost:8080/actuator/health` | Backend readiness/health. |
| `http://localhost:8080/swagger-ui.html` | Interactive OpenAPI UI. |
| `http://localhost:8080/v3/api-docs` | Generated OpenAPI JSON. |
| `http://localhost:8080/oauth2/authorization/google` | Start Google login. |

The exact health and documentation exposure is controlled by the active Spring
profile.

## 7. Frontend configuration

Create `frontend/.env.local` from the frontend example file if provided:

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
NEXT_PUBLIC_GOOGLE_OAUTH_PATH=/oauth2/authorization/google
```

`NEXT_PUBLIC_*` variables are embedded into browser-visible code and therefore
must never contain secrets. The API/app origins and OAuth path are public
configuration. `NEXT_PUBLIC_GOOGLE_OAUTH_PATH` is optional unless the backend
authorization path is customized.

## 8. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

For reproducible automated environments, prefer `npm ci` when a lockfile is
present and current.

## 9. First login behavior

### Patient

1. Select **Continue with Google**.
2. Complete Google authentication.
3. The backend creates or resolves a `PATIENT` identity.
4. Complete the patient profile before using booking features.

### Representative

A user cannot select the representative role. A representative must have a valid
pre-created invitation/allowlist record and assigned hospital.

1. Copy `backend/scripts/provision-representative.sql` to a private, untracked
   working file outside the repository.
2. Replace the example email with the exact verified Google email used for the
   local/viva test, choose an existing hospital ID, and replace the employee and
   organization placeholders.
3. Run the private copy through an administrator-controlled MySQL client before
   that account's first sign-in. Run it once per hospital assignment required.
4. Sign in with that Google account. The backend consumes the invitation,
   assigns the representative role and hospital scope, and writes audit events.
5. Verify the invitation now has `active = FALSE` and a non-null `accepted_at`,
   the expected `representative_hospitals` row exists, and `audit_logs` contains
   `USER_PROVISIONED` or `USER_ROLE_ASSIGNED`,
   `REPRESENTATIVE_HOSPITAL_ASSIGNED`, and
   `REPRESENTATIVE_INVITATION_ACCEPTED` as applicable.

If the account already signed in as a patient, create the invitation and then
sign out and back in. Never change a browser payload or manually update
`users.role`; the allowlist is the authorization source.

This template is not an automatic production seed. Never put a real Google email
in a committed migration, source file, screenshot, or shared viva artifact.

## 10. Verify local behavior

Perform these smoke checks:

1. Backend health endpoint responds successfully.
2. Flyway reports the expected migration version and no checksum mismatch.
3. Frontend landing page loads without browser console errors.
4. Google login returns to the frontend and `/api/v1/auth/me` succeeds.
5. Patient profile completion persists after refresh.
6. Doctor and hospital search return seed data, if seed data is enabled.
7. An appointment can be booked only in a valid free slot.
8. A second conflicting booking receives a conflict response.
9. A patient cannot access representative endpoints.
10. A representative cannot mutate a hospital outside their assignment.

These are verification steps to run; this guide does not claim they have been run
in a particular clone.

## 11. Test commands

Backend:

```bash
cd backend
./mvnw verify
```

Frontend commands depend on the scripts declared in `frontend/package.json`:

```bash
cd frontend
npm run lint
npm run typecheck
npm test
npm run build
```

The repository also exposes these through `make backend-test` and
`make frontend-check` when the corresponding host tools are installed. See
[`testing.md`](testing.md) for the intended test strategy and evidence to retain.

## 12. Stop local services

Stop the frontend and backend with `Ctrl+C`. If Docker Compose started MySQL:

```bash
docker compose stop mysql
```

Use `stop` for normal work so local database data remains available. Remove
volumes only when intentionally resetting development data and after confirming
that no needed data is stored there.

## 13. Troubleshooting

### `redirect_uri_mismatch`

- Compare the URI reported by Google with the configured URI character by
  character.
- Confirm backend port `8080`.
- Confirm callback path `/login/oauth2/code/google`.
- Do not use the frontend URL as the callback.

### Browser receives `401` after successful Google login

- Confirm frontend requests use `credentials: "include"`.
- Confirm `APP_FRONTEND_URL` and `CORS_ALLOWED_ORIGINS`.
- Inspect whether the browser accepted `JSESSIONID`.
- For local HTTP, ensure `SESSION_COOKIE_SECURE=false`.
- Confirm Spring Session tables exist and the database connection is healthy.

### CORS error

- Add the exact frontend origin, without a path, to the allowlist.
- Do not combine wildcard origin with credentials.
- Restart the backend after changing configuration.
- Distinguish a real CORS error from an API `401`/`500` hidden by browser output.

### Flyway checksum mismatch

Do not edit an applied migration. Restore the original file and add a new
migration. Use Flyway repair only after understanding and documenting the
environment-specific cause.

### MySQL connection failure

- Confirm MySQL is running and reachable.
- Confirm the JDBC prefix in `DB_URL`.
- Confirm database/user permissions.
- Confirm port `3306` or the explicitly configured local port.
- Review backend logs while ensuring credentials remain redacted.

### Port already in use

Stop the conflicting process or set an alternate local backend port. If the
backend port changes, update the OAuth callback, frontend API URL, and allowed
origins consistently.
