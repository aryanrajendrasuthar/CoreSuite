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
- **Minimal, clean code.** No speculative abstractions, no unused
  configuration, no half-finished features. Three similar lines beat a
  premature abstraction.
- **Every phase should be runnable.** Don't leave a phase half-wired —
  `docker compose up` + `mvn clean install` + `npm run dev` should always
  work at the tip of `main`.

## Stack decisions (already made — don't relitigate without reason)

- Java 21 (LTS), Spring Boot 3.x, Maven multi-module reactor under `backend/`
- React + TypeScript + Redux Toolkit + Vite under `frontend/dashboard`
- MySQL for transactional data, MongoDB for unstructured/CRM data, Redis for
  caching and rate limiting
- CI: GitHub Actions — build/test, OWASP Dependency-Check, TruffleHog secret
  scan, CodeQL, all blocking merges

## Commands

```bash
# Backend: build + test everything
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
