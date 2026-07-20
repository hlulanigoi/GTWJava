# Going That Way

A crowd-sourced parcel delivery platform: senders post parcels, travelers post trips, and the server matches them via a Haversine route-corridor algorithm. Includes admin role, ticket system, and bank-transfer payment flow.

## Run & Operate

- **API Server workflow** — start/restart the "API Server" workflow in Replit (runs `PORT=8080 pnpm --filter @workspace/api-server run dev`)
- `pnpm --filter @workspace/api-server run dev` — run the API server manually (set `PORT=8080`)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string

## API surface

| Domain | Endpoints |
|---|---|
| Auth | `POST auth/register`, `POST auth/login`, `POST auth/logout`, `GET auth/me` |
| Rides | `GET/POST rides`, `GET rides/my`, `GET/PATCH/DELETE rides/:id`, `POST rides/on-demand`, `PATCH rides/:id/status` |
| Parcels | `GET/POST parcels`, `GET parcels/my`, `GET/PATCH/DELETE parcels/:id`, `POST trips/:id/match-parcels` |
| Trips | `GET/POST trips`, `GET trips/my`, `GET/PATCH trips/:id`, `POST trips/:id/match` |
| Bookings | `GET bookings`, `POST bookings/:id/accept|reject|pickup|complete` |
| Matches | `GET matches`, `POST matches/:id/accept|reject|collect|deliver` |
| Tickets | `GET tickets/my`, `GET tickets/price`, `POST tickets/purchase` |
| Payments | `POST payments/initiate`, `POST payments/verify` |
| Users | `GET users/:id`, `PATCH users/profile`, `POST users/me/apply-driver` |
| Admin | `GET admin/stats`, `GET/PATCH admin/users/:id/*`, `GET admin/drivers/pending`, `PATCH admin/drivers/:id/approve|reject`, `GET/PATCH admin/rides/:id/status`, `GET admin/trips`, `GET admin/tickets`, `PATCH admin/tickets/price`, `GET/PATCH admin/payments/pending|:ref/verify|reject`, `GET admin/reports/revenue` |

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

| Area | Path |
|---|---|
| DB schema (source of truth) | `lib/db/src/schema/` — one file per domain (users, parcels, trips, matches, bookings, tickets, payments, rides) |
| API route handlers | `artifacts/api-server/src/routes/` |
| Zod request/response schemas | `lib/api-zod/src/generated/api.ts` (generated from OpenAPI spec) |
| OpenAPI spec + Orval codegen config | `lib/api-spec/` |
| React query hooks (for web clients) | `lib/api-client-react/src/generated/api.ts` |
| Haversine route-matching util | `artifacts/api-server/src/lib/haversine.ts` |
| Nominatim geocoding util | `artifacts/api-server/src/lib/geocode.ts` |
| JWT auth helpers | `artifacts/api-server/src/lib/auth.ts` |
| Runtime config (e.g. ticket price) | `artifacts/api-server/src/lib/config.ts` |
| Android app source | `GoingThatWay/` |
| Post-merge setup script | `scripts/post-merge.sh` |

## Architecture decisions

- **Drizzle ORM over raw SQL** — type-safe queries tied directly to the schema files in `lib/db/src/schema/`; run `pnpm --filter @workspace/db run push` to sync schema changes to the DB (dev only — production schema is managed by Replit's Publish flow).
- **OpenAPI-first codegen** — the OpenAPI spec in `lib/api-spec/` is the contract; Zod validators (`lib/api-zod/`) and React Query hooks (`lib/api-client-react/`) are generated from it via Orval (`pnpm --filter @workspace/api-spec run codegen`).
- **Runtime config for mutable settings** — admin-adjustable values (ticket price) live in `runtimeConfig` (`artifacts/api-server/src/lib/config.ts`) and reset on restart; they are not persisted to the DB yet.
- **DATABASE_URL is runtime-managed** — Replit provisions and injects `DATABASE_URL` (and `PG*` vars) automatically; never set these manually or hardcode them.
- **Server-side Haversine matching** — `POST /api/trips/:id/match` runs the route-corridor algorithm (`haversine.ts`) server-side, mirroring the Android `RouteMatchingUtils.java` logic so the Android app doesn't need to do its own matching.

## Product

Going That Way is a crowd-sourced parcel delivery platform.  
**Senders** post parcels with pickup and destination coordinates; they pay via bank transfer (platform takes 20 %, carrier earns 80 %).  
**Travelers** post trips (origin → destination, departure time, transport mode). The server matches parcels whose route falls within a 2 km corridor of the trip with a max 5 km detour.  
**Tickets** — travelers must buy a carrier ticket (paid via bank EFT) before they can collect parcels.  
**Admin** — role-based admin section with stats dashboard, user management, parcel oversight, and payment verification.  
The Android app (`GoingThatWay/`) is the primary client; this repo contains the backend API server.

## User preferences

_Populate as you build — explicit user instructions worth remembering across sessions._

## Gotchas

- `DATABASE_URL` and all `PG*` env vars are injected by Replit at runtime — they will NOT appear in `viewEnvVars` output and must not be set manually.
- After any schema change in `lib/db/src/schema/`, run `pnpm --filter @workspace/db run push` before restarting the API server.
- The API server mounts all routes under `/api/` (e.g. health check is `GET /api/healthz`, not `/health`).
- The `post-merge.sh` script runs automatically after task-agent merges — it installs deps and pushes the DB schema.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
