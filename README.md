# Loadouts

Loadouts is a web app for building and sharing collections of products for hobbies and activities. The Demo 2 build includes session restoration, private drafts and profiles, owner-only editing and deletion, manual products with optional information, discovery filters and sorting, and community interactions.

## Run the local demo

Open two terminals in the repository. You need a Java 21 or newer JDK and Node.js.

In the first terminal, from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\mvnw-demo.ps1
```

Wait until Spring Boot starts on port 8080. The launcher downloads Maven on its first run. The local demo uses a persistent H2 database in .demo-data and binds to loopback. You do not need Docker. If an older backend is already running in VS Code, stop it there with Ctrl+C before starting the new one.

In the second terminal:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Open Vite's printed URL. Restart Vite after updating the proxy configuration. Run the tests and view coverage using [the Demo 2 runbook](DEMO2.md). The [issue log](ISSUES.md) records defects, their status, and supporting evidence.

## Database and deployment

The default Spring profile uses PostgreSQL. For local PostgreSQL testing, start it with docker compose up -d and run Maven with COOKIE_SECURE=false and LOADOUTS_AUTH_REQUIRE_HTTPS=false only for loopback HTTP. The demo profile already has those local settings.

For deployment set DATABASE_URL, DATABASE_USERNAME, and DATABASE_PASSWORD. Keep the default HTTPS requirement and secure cookies. Serve the frontend and backend under the same public origin and have the deployment proxy provide trusted forwarded headers. The local demo does not verify a production deployment.

## Demo 2 boundary

Store links, prices, availability, and notes are entered manually. Unknown values remain unknown; displayed totals use stored USD prices and exclude tax and shipping. Loadout reporting, automatic retailer data retrieval, stronger email format rules, and the tutorial belong to the later milestones.

