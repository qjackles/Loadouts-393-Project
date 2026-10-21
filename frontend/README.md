# Loadouts frontend

React + TypeScript + Vite UI for the existing Spring Boot API. No mock accounts or collections are inserted. Registration, login/logout, browse, detail, and creation use real endpoints. Search, category filters, and sorting operate on the returned collection list locally.

## Run locally

1. Start PostgreSQL from the repository root: `docker compose up -d`.
2. Start Spring Boot from the repository root: `mvn spring-boot:run` (Java 21 and Maven required).
3. In another terminal: `cd frontend`, `npm install`, then `npm run dev`.
4. Open the localhost URL printed by Vite (usually http://127.0.0.1:5173).

Vite proxies `/api` to `http://localhost:8080`, forwarding session cookies through the same browser origin. Use `COOKIE_SECURE=false` for local HTTP development (the backend default). Never use this setting for production HTTPS.

## Verify

`npm run build` runs TypeScript checking and creates `dist/`. For a production deployment, serve `dist/` behind a host that also proxies `/api` to Spring Boot. Vite's development proxy is not included in the built files.

## Current demo behavior

- Registration signs in immediately, matching the backend.
- Login state is held in memory: after refresh, sign in again to create. The backend has no endpoint to check the current session.
- Every new collection is public immediately; no draft/publish endpoints exist.
- Creators can manually add product names, links, and notes after creating a loadout, and remove those entries later. Visitors can open product links. Prices, images, profiles, loadout editing/deletion, and social interactions are not implemented.
- Collection illustrations are decorative CSS; they are not product images.
- Google Fonts are optional; local sans-serif fallbacks work without them.

## Manual smoke check

With the backend running: register an account, create a collection, add a product link, refresh its detail URL, open the product link, remove it, return to browse, search/filter/sort, sign out, and verify creation prompts for login. Also stop the backend and retry browse to inspect the error state. Test a narrow mobile viewport and keyboard navigation through account forms.
