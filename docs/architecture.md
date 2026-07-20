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
| `api-gateway` | Routing, auth enforcement, rate limiting, CORS | None (stateless routing layer) |
| `shared` | Auth/session library, validation helpers, common DTOs | None — imported as a dependency by every service |

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
