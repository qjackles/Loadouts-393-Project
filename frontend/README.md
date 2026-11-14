# Loadouts frontend

React and TypeScript frontend for the Spring Boot API. Run the backend as described in the repository README, then run npm.cmd ci and npm.cmd run dev from this folder. The local development and preview proxy forwards /api to port 8080 without changing the browser-facing host. This keeps cookie-based requests on the same origin.

Run npm.cmd run build to type check and compile the production bundle. Production hosting must serve /api on the same origin as the frontend; the Vite proxy is only for local development and preview.

