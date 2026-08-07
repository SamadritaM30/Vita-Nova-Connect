# VitaNova Connect - Medical Aid Tracker

VitaNova Connect is a full-stack healthcare coordination application for
discovering doctors, checking hospital bed availability, booking appointments,
and managing day-to-day hospital availability.

The project modernizes the original Java/JDBC console prototype into a
deployment-ready final-year B.Tech project while preserving the original
artifacts in [`legacy/`](legacy/).

## Product roles

### Patient

- Sign in securely with Google.
- Complete and maintain a patient profile.
- Search doctors by specialization and availability.
- Find hospitals with available ICU or general beds.
- view appointment slots, book appointments, and cancel future bookings;
- find available ambulances and create ambulance requests.

### Medical representative

- Access only assigned hospitals.
- Maintain doctor and bed availability.
- Review operational appointment data.
- Cancel appointments with a recorded reason.
- View dashboard totals while operational changes are recorded in an audit log.

## Architecture

```text
Browser
  │
  ├── Next.js 16 / TypeScript / Tailwind CSS (Vercel)
  │        │
  │        └── credentialed REST requests
  │
  └── Spring Boot 3.5 / Spring Security / OAuth 2.0 (Railway)
           │
           ├── Spring Data JPA
           ├── Flyway migrations
           └── MySQL 8 (Railway)
```

The application is a modular monolith: one web client, one Java REST API, and
one relational database. This keeps the implementation understandable while
still demonstrating professional separation of concerns.

## Repository

```text
.
├── frontend/             Next.js web application
├── backend/              Spring Boot REST API
├── docs/                 Architecture, database, API, deployment, and viva docs
├── legacy/               Preserved original Java/JDBC prototype
├── .github/workflows/    Automated verification
├── compose.yaml          Complete local container environment
└── Makefile              Common development commands
```

## Local development

### Container workflow

1. Copy `.env.example` to `.env` and replace placeholder values.
2. Create Google OAuth credentials for the backend callback URL:
   `http://localhost:8080/login/oauth2/code/google`.
3. Start all services:

   ```bash
   docker compose up --build
   ```

4. Open `http://localhost:3000`.

### Run services separately

Requirements:

- Node.js 22+
- Java 21
- Maven 3.9+ (or the included wrapper)
- MySQL 8

Frontend:

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Backend:

```bash
cd backend
cp .env.example .env
set -a
source .env
set +a
./mvnw spring-boot:run
```

The frontend runs at `http://localhost:3000`, the API at
`http://localhost:8080`, and interactive OpenAPI documentation at
`http://localhost:8080/swagger-ui.html`.

## Verification

```bash
cd frontend
npm run lint
npm run typecheck
npm test
npm run build

cd ../backend
./mvnw verify
```

GitHub Actions runs the same checks for every pull request.

## Configuration and secrets

No credentials are stored in source. Each service includes a safe
`.env.example` documenting the required settings. Production values belong in
Vercel and Railway environment settings.

Representative access is never selected in the browser. A representative must
be provisioned by an authorized database migration or administrator and linked
to one or more hospitals. The administrator-only
[`backend/scripts/provision-representative.sql`](backend/scripts/provision-representative.sql)
template makes the local/viva setup reproducible without committing a real
Google email.

## Documentation

- [Architecture and sequences](docs/architecture.md)
- [Database schema](docs/database-schema.md)
- [REST API](docs/api.md)
- [Module descriptions](docs/modules.md)
- [Installation guide](docs/installation.md)
- [Deployment guide](docs/deployment.md)
- [Security design](docs/security.md)
- [Testing strategy](docs/testing.md)
- [Viva guide](docs/viva-guide.md)

## Project evolution

The original implementation demonstrated Java, JDBC, SQL joins, constraints,
and transactions. The modern application retains those concepts while adding
typed REST boundaries, OAuth authentication, role-based authorization,
versioned migrations, automated tests, responsive UI, structured error
handling, logging, and reproducible deployments.
