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
- **Auth is centralized in `api-gateway`, trusted by the other services via
  headers, not re-implemented per service.** `api-gateway` owns the `users`
  table (own MySQL database, `auth_service`) and Redis-backed sessions
  (`AuthController`, `SessionService`); after validating a session cookie, its
  `AuthenticationGatewayFilter` attaches `X-Gateway-Secret`/`X-User-Id`/
  `X-User-Roles` to the proxied request. Every other service runs
  `shared`'s `TrustedHeaderAuthenticationFilter` + a `SecurityConfig`
  requiring `.anyRequest().authenticated()`, trusting those headers instead
  of validating sessions themselves. `GATEWAY_SECRET` must be identical
  across every service's config (defaults match locally; change together in
  any real deployment — see SECURITY.md). This is defense-in-depth, not the
  real trust boundary: a service reached directly (bypassing the gateway)
  with a correct/leaked secret would still be trusted — the real boundary is
  network isolation, a Phase 8 deployment concern.
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
# inventory-service, crm-service, order-service, and api-gateway run their
# integration tests against real MySQL/MongoDB/Redis via Testcontainers, not
# mocks or H2. reporting-service has no database of its own; its tests use
# MockRestServiceServer to stub the other services' HTTP responses)
cd backend && mvn clean install

# Backend: run a single service (product/crm/inventory/order need
# DB_USERNAME/DB_PASSWORD; crm additionally needs MONGO_USERNAME/
# MONGO_PASSWORD; api-gateway additionally needs REDIS_PASSWORD — all must
# match infra/.env, see README)
cd backend/<service-name> && mvn spring-boot:run

# No user exists until you register one — the first registration becomes
# ADMIN, every one after is STAFF (see AuthService)
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"at least 12 characters"}'

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

**Two Spring Security MockMvc gotchas hit in Phase 6, worth not rediscovering:**
1. A nested `@TestConfiguration` class is only auto-detected on the exact test
   class JUnit is running, **not inherited from a superclass**. Each
   service's `AbstractIntegrationTest` defines `AuthenticatedMockMvcConfig`
   (a `MockMvcBuilderCustomizer` that attaches valid trusted headers to every
   request by default) — it only actually applies because the class carries
   an explicit `@Import(AbstractIntegrationTest.AuthenticatedMockMvcConfig.class)`.
   Drop that `@Import` and every subclass's requests silently go out with no
   headers at all, and every test starts failing with 401/403 for reasons
   that look unrelated to headers.
2. With `httpBasic().disable()` and `formLogin().disable()`, Spring Security
   has no `AuthenticationEntryPoint` configured and falls back to
   `Http403ForbiddenEntryPoint` — meaning **every** auth failure returns 403,
   including requests with no credentials at all, not just "authenticated but
   forbidden" cases. Each service's `SecurityConfig` overrides this with an
   explicit `exceptionHandling().authenticationEntryPoint(...)` returning 401,
   since that's the correct code for "no valid identity presented" and this
   API never wants a browser login prompt. If a service you're securing
   starts returning 403 for missing-auth requests instead of 401, this is why.

Each service's `SecurityEnforcementTest` builds its own `MockMvc` via
`MockMvcBuilders.webAppContextSetup(...).apply(springSecurity()).build()`
(bypassing the auto-authenticated default) specifically to prove requests
are rejected, not just that authenticated ones happen to work — the
auto-configured `MockMvc` bean alone can't test the rejection path.

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

Before merging anything that adds an endpoint, touches auth, or handles
customer data, check it against [`SECURITY.md`](SECURITY.md): the service's
`SecurityConfig` covers the new route (it will, by default —
`.anyRequest().authenticated()` — but check nothing carves out a new
`permitAll()`), input validated via `jakarta.validation`, no raw SQL string
concatenation, no secret committed, role-gated with `@PreAuthorize` if the
action should be admin-only. Update `SECURITY.md`'s status table when a
control actually ships — don't mark it done ahead of the code.
