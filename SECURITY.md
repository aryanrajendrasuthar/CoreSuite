# Security Baseline — CoreSuite

CoreSuite targets production-grade security from the first commit. This
document is the source of truth for what's required, what's implemented, and
where each control lives. No control is claimed as "done" here until it's
actually merged — see the status table.

We do not claim CoreSuite is unbreakable — no system is. The goal is
defense-in-depth: no easy unauthorized access, no known weak points,
least-privilege by default, and a system that fails safely and logs the
failure rather than failing open silently.

## Controls and status

| Control | Requirement | Status | Location |
|---|---|---|---|
| Password hashing | Argon2id | Implemented — `PasswordHasher` (Spring Security's `Argon2PasswordEncoder`, Bouncy Castle backend) | `backend/shared` |
| 2FA | TOTP-based | Not yet implemented | `backend/api-gateway` (planned) |
| Sessions | 256-bit random tokens, HttpOnly/Secure/SameSite=Strict cookies | Implemented — opaque tokens in Redis (24h TTL), cookie set with all three flags | `backend/api-gateway` (`AuthController`, `SessionService`) |
| Rate limiting | 10 req/15min on auth endpoints; Redis-backed elsewhere | Partially implemented — login is rate-limited (10/15min per client IP, Redis fixed-window counter); general per-endpoint API rate limiting is not yet applied | `backend/api-gateway` (`RateLimiterService`) |
| Authentication enforcement | Every mutation requires a valid identity | Implemented — every backend service requires a trusted identity from api-gateway on every request (not just mutations), enforced at the Spring Security filter-chain level (`anyRequest().authenticated()`) rather than per-method annotations, so a new endpoint can't accidentally ship unauthenticated | `product-service`, `crm-service`, `inventory-service`, `order-service` (`SecurityConfig`) |
| Authorization (IDOR / ownership) | Server-side resource-ownership check on every mutation | Reframed, not yet fully implemented — see note below | Per-service (planned) |
| Input validation | `jakarta.validation` on every controller DTO | Implemented in `product-service`, `inventory-service`, `crm-service`, `order-service`, `api-gateway`'s auth endpoints; not yet in the other services | Per-service DTOs |
| SQL injection | Parameterized queries only (JPA/Hibernate, no string-concatenated SQL) | Implemented in `product-service`, `inventory-service`, `crm-service`, `order-service`, `api-gateway` (Spring Data JPA/JPQL only, no native queries) | Per-service repositories |
| XSS | DOMPurify-equivalent sanitization on any user-supplied HTML rendered by the frontend | Not yet implemented — not yet applicable either, the frontend doesn't render arbitrary user-supplied HTML anywhere today | `frontend/dashboard` (planned) |
| Field-level encryption | AES-256-GCM for sensitive customer data (contact info, payment references) | Not yet implemented | `crm-service` (planned) |
| Row-level access | Enforced in the service layer (MySQL has no native RLS — this is a deliberate application-layer control) | Not yet implemented — see note below | Per-service (planned) |
| CORS | Exact-origin whitelist, no wildcard, credentials allowed for the session cookie | Implemented — single configurable origin via `CORS_ALLOWED_ORIGIN`, defaults to the local frontend dev origin | `backend/api-gateway` |
| Observability | Sentry wired from first commit | Not yet implemented | All services (planned) |
| CI security gates | `mvn dependency-check`, TruffleHog secret scanning, CodeQL — all blocking merges | Implemented | `.github/workflows/ci.yml` |
| Secrets | Zero hardcoded secrets; local dev secrets via `.env` (gitignored) | Enforced by `.gitignore` + CI secret scan. The gateway-service shared secret (`GATEWAY_SECRET`) and Redis/MySQL credentials follow the same pattern — env-var only, dev defaults are clearly marked `change-me` | Repo root |
| Compliance | Right-to-delete / right-to-export endpoints for customer data; 30-day log retention | Not yet implemented | `crm-service` (planned) |

**On "IDOR / row-level access" and this being an internal tool, not a
multi-tenant SaaS:** the original baseline's wording ("resource-ownership
check," "row-level access") was written for a customer-self-service system
where a customer can only see their own orders. CoreSuite is an internal
business-management tool used by one organization's staff — there is no
natural "which records does this caller own" question for a product catalog
or an order queue the way there is for a customer portal. What's actually
implemented instead, and what those controls become for this kind of system:
mandatory authentication on every request (done, see above) and role-based
authorization (`ADMIN` vs `STAFF`, done at the identity layer — every backend
service already receives the caller's roles via `X-User-Roles` and could
enforce role checks with `@PreAuthorize("hasRole('ADMIN')")` — but no
endpoint currently requires a role beyond "authenticated," since none of the
five domains have an admin-only mutation yet). If CoreSuite ever needs true
multi-tenancy (e.g., hosting multiple businesses), row-level scoping by
tenant ID becomes a real requirement again — tracked, not implemented.

**Known gap, stated plainly:** a service reached directly (bypassing the
gateway) and presenting a stolen/guessed `X-Gateway-Secret` would be trusted.
The `TrustedHeaderAuthenticationFilter` is defense-in-depth, not the real
boundary — the real boundary is network isolation (only the gateway can reach
the other services), which is a Phase 8 deployment concern, not something
enforceable in application code alone. In this repo's current local/dev
topology, all services listen on `localhost` with no network segmentation.

## CI setup required once the repo is live

- Add an `NVD_API_KEY` repository secret (free key from
  [nvd.nist.gov](https://nvd.nist.gov/developers/request-an-api-key)) so the
  OWASP Dependency-Check job in `.github/workflows/ci.yml` doesn't get
  rate-limited fetching the vulnerability database.
- Enable branch protection on `main` requiring the `backend-build`,
  `frontend-build`, `dependency-check`, `secret-scan`, and `codeql` checks to
  pass before merge — the workflow runs them, but only branch protection
  makes them actually blocking.

## Secrets to change before any real deployment

Every service defaults `GATEWAY_SECRET` to `local-dev-gateway-secret-change-me`
and MySQL/Redis credentials to `changeme` — fine for `docker compose up`
against `localhost`, not fine anywhere reachable outside this machine. Set
real values via each service's environment before deploying (Phase 8).

## Reporting a vulnerability

This is a single-developer portfolio/production project. If you find a
vulnerability, open a private security advisory on the GitHub repository
rather than a public issue.

## Phased rollout

Security controls land alongside the feature phases in
[`docs/project-plan.md`](docs/project-plan.md), with a dedicated hardening
pass in Phase 6 to verify full coverage (every mutation has an `@PreAuthorize`
check, every controller boundary validates input, encryption is applied
everywhere sensitive data is written). This table is updated as each control
ships — do not treat an unchecked row as covered.
