# CLAUDE.md

Guidance for Claude Code (or any agent) working in this repository.

## What this is

CoreSuite is a production-grade, single-developer-owned business management
platform (product catalog, CRM, inventory, orders, reporting). Full context:
[`docs/project-plan.md`](docs/project-plan.md) (why it exists, tech stack,
feature scope, build phases) and [`docs/architecture.md`](docs/architecture.md)
(module boundaries, data ownership).

This is a clean-room rebuild of a problem domain, not a copy of any prior
employer's code. No proprietary source, schemas, or internal naming from any
prior codebase should ever be introduced here.

## Ground rules

- **Production-grade, not portfolio-shortcut.** License is proprietary
  (all rights reserved). Security baseline in [`SECURITY.md`](SECURITY.md)
  is non-negotiable for anything that touches auth, mutations, or customer
  data — apply it as work lands in that area, don't defer it.
- **Respect module boundaries** (see `docs/architecture.md`): a service
  never queries another service's database directly, only via its REST API.
  Shared logic (auth, validation, common DTOs) lives once in `backend/shared`.
  `reporting-service` is the first consumer of this pattern (Spring
  `RestClient`, base URLs from env-configurable `ConfigurationProperties`,
  failures wrapped in `shared`'s `DownstreamServiceException` → 502) — follow
  the same shape for any future cross-service call, including in
  `api-gateway`.
- **One MySQL database per service**, not shared tables in one schema, even
  though they all run on the same MySQL server (see `infra/mysql-init`). This
  isn't optional — Flyway's non-empty-schema safety check operates at the
  database level, so a shared schema breaks the moment a second service
  starts against it (found the hard way running the full stack in Phase 5).
- **Minimal, clean code.** No speculative abstractions, no unused
  configuration, no half-finished features. Three similar lines beat a
  premature abstraction.
- **Every phase should be runnable.** Don't leave a phase half-wired —
  `docker compose up` + `mvn clean install` + `npm run dev` should always
  work at the tip of `main`.

## Stack decisions (already made — don't relitigate without reason)

- Java 21 (LTS), Spring Boot 3.x, Maven multi-module reactor under `backend/`
- React + TypeScript + Redux Toolkit Query + Vite under `frontend/dashboard`
  — RTK Query for all data fetching, no hand-written thunks/slices
- MySQL for transactional data, MongoDB for unstructured/CRM data, Redis for
  caching and rate limiting
- `api-gateway` uses Spring Cloud Gateway pinned to **2023.0.3**, not a later
  2023.0.x patch: 2023.0.6's gateway module calls `HttpHeaders.headerSet()`,
  which doesn't exist in the Spring Framework version Spring Boot 3.3.4
  actually ships (`NoSuchMethodError` at request time, not at build time —
  only shows up when a route is actually hit). Re-check compatibility before
  bumping this.
- CI: GitHub Actions — build/test, OWASP Dependency-Check, TruffleHog secret
  scan, CodeQL, all blocking merges

## Commands

```bash
# Backend: build + test everything (Docker must be running — product-service,
# inventory-service, crm-service, and order-service run their integration
# tests against real MySQL/MongoDB via Testcontainers, not mocks or H2.
# reporting-service has no database of its own; its tests use
# MockRestServiceServer to stub the other services' HTTP responses)
cd backend && mvn clean install

# Backend: run a single service
cd backend/<service-name> && mvn spring-boot:run

# Frontend
cd frontend/dashboard && npm install && npm run dev    # dev server
cd frontend/dashboard && npm run build                 # production build
cd frontend/dashboard && npm run lint                   # lint

# Local infra (from infra/, with a .env copied from .env.example)
cd infra && docker compose up -d
cd infra && docker compose down
```

## Testing

Every service with persistence tests against real datastores via
Testcontainers (`AbstractIntegrationTest` in each service's test tree) —
never H2 or mocked repositories for integration tests. When adding a new
service's `AbstractIntegrationTest`, do **not** use `@Testcontainers`/
`@Container` on the container fields: that combination stops containers
after each test class's `afterAll`, even for a static field shared via an
abstract base class, which restarts them mid-suite and breaks the next
test class's connection (seen firsthand in Phase 1 — cost real debugging
time). Use Testcontainers' documented singleton pattern instead: a plain
`@ServiceConnection`-annotated static field started once in a `static {}`
initializer, left running for the JVM's lifetime.

**Frontend has no automated test runner yet** (build + lint only, via
`npm run build`/`npm run lint`) — a known gap, not a stated non-goal. Phase 5
verified the frontend's correctness by exercising every write path (the exact
requests each page's RTK Query hooks make) through a live gateway with `curl`,
since no browser-automation tool (Playwright, chromium-cli) was available in
that session to drive it visually. Add Vitest + React Testing Library before
this gap grows with more pages.

## Build phases

Work proceeds phase by phase per [`docs/project-plan.md#6-build-phases`](docs/project-plan.md#6-build-phases).
Don't jump ahead to a later phase's feature work before the current phase's
foundation (schema, module skeleton, tests) is in place. Update the phase
status in `README.md` and `SECURITY.md`'s control table as each phase lands.

## Security

Before merging anything that adds a mutation endpoint, touches auth, or
handles customer data, check it against [`SECURITY.md`](SECURITY.md):
IDOR check present, input validated via `jakarta.validation`, no raw SQL
string concatenation, no secret committed. Update `SECURITY.md`'s status
table when a control actually ships — don't mark it done ahead of the code.
