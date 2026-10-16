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
