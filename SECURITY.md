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
| Password hashing | Argon2id | Not yet implemented | `backend/shared` (planned) |
| 2FA | TOTP-based | Not yet implemented | `backend/shared` (planned) |
| Sessions | 256-bit random tokens, HttpOnly/Secure/SameSite=Strict cookies | Not yet implemented | `backend/shared` (planned) |
| Rate limiting | 10 req/15min on auth endpoints; Redis-backed elsewhere | Not yet implemented | `backend/api-gateway` (planned) |
| Authorization (IDOR) | Server-side resource-ownership check on every mutation | Not yet implemented | Per-service `@PreAuthorize` (planned) |
| Input validation | `jakarta.validation` on every controller DTO | Implemented in `product-service`, `inventory-service`, `crm-service`, `order-service`; not yet in the other services | Per-service DTOs |
| SQL injection | Parameterized queries only (JPA/Hibernate, no string-concatenated SQL) | Implemented in `product-service`, `inventory-service`, `crm-service`, `order-service` (Spring Data JPA/JPQL only, no native queries) | Per-service repositories |
| XSS | DOMPurify-equivalent sanitization on any user-supplied HTML rendered by the frontend | Not yet implemented | `frontend/dashboard` (planned) |
| Field-level encryption | AES-256-GCM for sensitive customer data (contact info, payment references) | Not yet implemented | `crm-service` (planned) |
| Row-level access | Enforced in the service layer (MySQL has no native RLS — this is a deliberate application-layer control) | Not yet implemented | Per-service |
| CORS | Exact-origin whitelist, no wildcard | Not yet implemented | `backend/api-gateway` (planned) |
| Observability | Sentry wired from first commit | Not yet implemented | All services (planned) |
| CI security gates | `mvn dependency-check`, TruffleHog secret scanning, CodeQL — all blocking merges | Implemented | `.github/workflows/ci.yml` |
| Secrets | Zero hardcoded secrets; local dev secrets via `.env` (gitignored) | Enforced by `.gitignore` + CI secret scan | Repo root |
| Compliance | Right-to-delete / right-to-export endpoints for customer data; 30-day log retention | Not yet implemented | `crm-service` (planned) |

## CI setup required once the repo is live

- Add an `NVD_API_KEY` repository secret (free key from
  [nvd.nist.gov](https://nvd.nist.gov/developers/request-an-api-key)) so the
  OWASP Dependency-Check job in `.github/workflows/ci.yml` doesn't get
  rate-limited fetching the vulnerability database.
- Enable branch protection on `main` requiring the `backend-build`,
  `frontend-build`, `dependency-check`, `secret-scan`, and `codeql` checks to
  pass before merge — the workflow runs them, but only branch protection
  makes them actually blocking.

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
