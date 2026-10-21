# Loadouts Backend Starter

Minimal Spring Boot backend for the demo.

Implemented:
- account registration
- login/logout using a server session
- create loadout (login required)
- view all loadouts
- view one loadout
- PostgreSQL locally/production
- H2 for JUnit tests

Run PostgreSQL:
`docker compose up -d`

Run backend:
`mvn spring-boot:run`

Run tests:
`mvn test`

Endpoints:
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout
- POST /api/loadouts
- GET /api/loadouts
- GET /api/loadouts/{id}

For Vercel, Dockerfile.vercel is included. Set DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, and COOKIE_SECURE=true.

This is intentionally a demo starter. Before production, add migrations, fuller validation, CORS/CSRF policy, and persistent/distributed sessions.


## Local demo without Docker

Java 21 or newer JDK and Node.js are required. From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\mvnw-demo.ps1
```

Wait for the backend to report that it started on port 8080. The first run downloads checksum-verified Apache Maven and Java dependencies. This explicit demo profile stores real accounts and loadouts in `.demo-data/` using H2; data survives restarts. It does not connect to or migrate the PostgreSQL database. Use PostgreSQL as documented above for the normal profile.

In a second terminal:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Open the URL Vite prints. Create an account using a unique email and username and a password of at least eight characters. Registration signs you in automatically. Keep both terminals running. Run backend tests with `powershell -ExecutionPolicy Bypass -File .\mvnw-demo.ps1 -Test`.