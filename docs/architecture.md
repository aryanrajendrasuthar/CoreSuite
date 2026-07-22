# Architecture — CoreSuite

## Style: modular monolith, extraction-ready

CoreSuite ships as a set of independently-buildable Spring Boot modules under
one Maven multi-module reactor (`backend/pom.xml`), deployed together during
early phases and split into independently-deployable services later. Each
module owns its own package namespace, its own persistence, and talks to
other modules only through defined interfaces — never by reaching into
another module's package or database.

This is a deliberate choice over starting with real microservices: a solo
developer building five domains gets a runnable system faster as a monolith,
while the module boundaries below make a later extraction (Phase 7) a real
refactor, not a rewrite.

## Modules

| Module | Owns | Data store |
|---|---|---|
| `product-service` | Catalog, SKUs, pricing, variants | Own MySQL database (`product_service`) |
| `crm-service` | Customer profiles, communication history, segmentation | Own MySQL database (`crm_service`) + MongoDB (comms history) |
| `inventory-service` | Stock levels, warehouses, reorder thresholds | Own MySQL database (`inventory_service`) |
| `order-service` | Order lifecycle, status transitions, history | Own MySQL database (`order_service`) |
| `reporting-service` | KPI aggregation, CSV/PDF export | Reads from other modules' APIs, no database of its own |
| `api-gateway` | Routing, auth (accounts, sessions), rate limiting, CORS | Own MySQL database (`auth_service`) for user accounts; Redis for sessions and rate-limit counters |
| `shared` | Auth primitives (Argon2id hashing, session tokens, the trusted-header filter every other service runs), validation helpers, common DTOs | None — imported as a dependency by every service |

Each MySQL-backed service gets its own **database**, not just its own tables
in a shared schema, on the one MySQL server `infra/docker-compose.yml` runs
(created by `infra/mysql-init` on first boot). This isn't just the standard
microservices pattern — it's load-bearing: Flyway's non-empty-schema safety
check operates at the database level, so if two services' tables lived in one
shared schema, the second service to start would find the schema already
non-empty (from the first service's tables) and refuse to run its own
migrations.

`frontend/dashboard` is a single React/TypeScript app using Redux Toolkit
Query (`app/api.ts`, with endpoints injected per domain in
`features/*/*.Api.ts`) for all data fetching — no hand-written thunks or
slices. All traffic goes through `api-gateway`, never to a service directly.

## Why MySQL + MongoDB

Transactional, relationally-shaped data (orders, inventory counts, pricing)
goes in MySQL, where foreign keys and transactions matter. Less-structured,
schema-flexible data (CRM communication history, product variant attribute
bags) goes in MongoDB, where the shape varies per record and strict schemas
add friction without adding safety.

## Authentication and trust boundary

`api-gateway` is the only module that authenticates anyone. The flow:

1. The browser calls `POST /api/auth/login` with credentials; `api-gateway`
   checks the password hash (Argon2id) against its `users` table. If the
   account has TOTP enabled, a valid 6-digit code (RFC 6238, `TotpService`)
   must also be present in the same request — the response distinguishes
   "code required" from "wrong password" via a `totpRequired` field in the
   error body, without confirming which credential was wrong. Only then does
   it create a session in Redis (opaque 256-bit token, 24h TTL) and set it as
   an HttpOnly/Secure/SameSite=Strict cookie. TOTP secrets are generated and
   verified via `/api/auth/totp/{setup,enable,disable}` (session-authenticated,
   not gateway-routed) and stored AES-256-GCM encrypted (`FieldEncryptor` in
   `shared`) — decrypted only in memory, only to check a submitted code.
2. Every subsequent request carries that cookie. `AuthenticationGatewayFilter`
   (a `GlobalFilter`, runs before routing) looks up the session in Redis; no
   valid session means an immediate 401, before the request ever reaches a
   backend service.
3. On a valid session, the filter attaches `X-Gateway-Secret`, `X-User-Id`,
   `X-User-Email`, and `X-User-Roles` to the proxied request — a trusted
   identity the backend service didn't have to derive itself.
4. Every other service runs `shared`'s `TrustedHeaderAuthenticationFilter` +
   a `SecurityConfig` requiring `.anyRequest().authenticated()`. It never
   sees the cookie, never talks to Redis, and never validates a password —
   it just trusts the headers, provided `X-Gateway-Secret` matches its own
   configured value.

That shared secret is defense-in-depth, not the real trust boundary: a
service reached directly (bypassing the gateway) with a correct or leaked
secret would still be trusted. The real boundary is network isolation —
only `api-gateway` should be able to reach the other services — which is a
Phase 8 deployment concern (see below), not something enforceable in
application code alone. Locally, everything listens on `localhost` with no
such isolation.

## Module boundary rules

1. A service never queries another service's database directly — only via
   its REST API.
2. Cross-cutting auth/session/validation logic lives once, in `shared`, and
   is imported — never copy-pasted between services.
3. `reporting-service` is read-only from the other services' perspective: it
   aggregates via API calls, it does not write to their schemas.
4. `api-gateway` is the only module aware of all service addresses; services
   don't call each other directly.

## Monolith → microservice extraction path (Phase 7, stretch)

`reporting-service` is the first extraction candidate: it already has the
cleanest boundary (read-only, aggregates over APIs, no other module depends
on it). Extracting it means giving it its own deployable artifact and its own
CI job — no code changes to its internal logic, because the module boundary
was already respected from Phase 0.

## Deployment topology (Phase 8)

- Backend: containerized Spring Boot services, deployed to Fly.io or Render
  (free tier)
- Frontend: static build, deployed to Vercel or Netlify (free tier)
- MySQL: Railway or Aiven free tier (hosted) / Docker locally
- MongoDB: Atlas free tier (M0)
- Redis: same host as backend compute, or a free-tier Redis add-on

See [`docs/project-plan.md`](project-plan.md) for the full phase breakdown
and the zero-cost service substitutions for the documented Azure-based stack.
