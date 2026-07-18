# Project Plan — CoreSuite
**Enterprise Business Management Platform**
(Real, portfolio-owned rebuild of the system domain described in the Orion Technolab documentation)

> Working name: **CoreSuite**. Renaming is a find-and-replace away — swap it before `claude-power` initializes the repo if you want something else.

---

## 0. Why this project exists

This rebuilds the *domain* you worked in at Orion Technolab — product management, CRM, inventory, order processing, reporting — as a clean-room system with no proprietary Orion code, schemas, or internal naming anywhere in it. You owned the frontend/backend split there; this project gives you the same span (React/TS frontend, Spring Boot backend) but as something you fully own and can walk through end-to-end in an interview.

**Assumptions I'm making (override any of these before build):**
- No proprietary Orion source, data, or internal service/module names are reused — this is a clean-room rebuild of the *problem domain* as described in your own documentation.
- "Full feature set" means all five domains from Section 1.1 of the Orion doc get real implementations.
- Production-grade means real auth, real multi-tenant-safe data access, real observability.
- Single-developer team (you) — phased so each stage ships something runnable.
- Because this is a full-stack Java/React system (not Next.js), the zero-cost swaps below are chosen for that stack specifically, not your Next.js/Supabase pattern.

---

## 1. Tech Stack

Matched to the documented Orion stack exactly. Paid managed-service points on the natural growth path get a zero-cost substitute — same protocol, same code, swap the connection string later if you want the managed version.

| Layer | Exact-match stack | Zero-cost path (use this) | Notes |
|---|---|---|---|
| Frontend | React.js, TypeScript, Redux | Same, **hosted free on Vercel or Netlify** | No cost either way |
| Backend | Java, Spring Boot, REST APIs | Same | Free |
| Relational DB | MySQL | **MySQL via Docker locally; Railway or Aiven free tier for hosted** | Both offer genuinely free small MySQL instances |
| Document DB | MongoDB | **MongoDB Atlas free tier (M0, 512MB)** | Same as SupplyForge — reuse the same free cluster if you want, separate databases |
| Cloud/infra — object storage | Azure Blob Storage | **Cloudflare R2 free tier** (10GB, S3-API-compatible) or **MinIO self-hosted** if you want zero external dependency | R2 has no time-limited free tier |
| Cloud/infra — compute | Azure VMs | **Fly.io or Render free tier** (containerized Spring Boot deploy) | Azure VMs bill hourly from day one; these have real always-free tiers for small workloads |
| Cloud/infra — event-driven functions | Azure Functions | **Same provider's serverless free tier** (Render Cron Jobs / Fly.io Machines, or self-host a lightweight consumer) | Match the "event-driven processing" role, not the Azure brand specifically |
| CI/CD | GitHub Actions | Same | Already free — matches documented stack exactly, no swap needed |
| QA automation | Selenium | Same | Free |
| Design | Figma | Same (free tier) | Free tier covers a solo project |
| Auth | — | Self-built per Security Baseline below | See §3 |

---

## 2. Feature Scope (full — matching all five domains)

1. **Product management** — cataloguing, SKUs, pricing, variants (shared conceptual ground with SupplyForge's catalog, but this is a separate codebase/domain — no code sharing between the two projects, to keep them independently demoable).
2. **CRM** — customer profiles, communication history log, segmentation (tag-based or rule-based customer groups).
3. **Inventory control** — real-time stock tracking, warehouse management, reorder threshold alerts.
4. **Order processing** — full lifecycle from placement through fulfillment and delivery, with status transitions and history.
5. **Reporting & analytics** — business dashboards, KPI tracking (orders/revenue/inventory turnover), exportable reports (CSV/PDF).

**Architectural intent carried over from your Orion experience:**
- Frontend/backend split matching your actual role there (React/TS frontend team, Java/Spring Boot backend team) — you'll build both sides yourself here, but keep the module boundary clean, the way it was cross-team there.
- MySQL for transactional core (orders, inventory), MongoDB for less-structured data (CRM communication history, product variant attributes) — mirrors the dual-database pattern from both your Orion and Avnet experience.
- Incremental microservice extraction: start as a modular monolith (faster to build solo), with clear module boundaries so extracting a service later (the way Orion's team did with its legacy monolith) is a real, demonstrable refactor step — not a rewrite.

---

## 3. Security Baseline

Same baseline as SupplyForge, applied to this stack:

- **Auth**: Argon2id password hashing, TOTP-based 2FA, session tokens as 256-bit random values in HttpOnly/Secure/SameSite=Strict cookies
- **Rate limiting**: 10 req/15min on auth endpoints; Redis-backed rate limiting on all other API endpoints
- **Authorization**: IDOR checks on every mutation (customer records, orders, inventory adjustments) — verify resource ownership server-side on every request
- **Input handling**: Bean Validation (`jakarta.validation`) on every controller boundary — Spring's equivalent of Zod — parameterized queries only (JPA/Hibernate with parameter binding, never string-concatenated SQL), DOMPurify equivalent on any HTML the frontend renders from user input
- **Data**: AES-256-GCM field-level encryption for sensitive customer data (contact info, payment references if stored); enforce row-level access in the service layer (no native RLS in MySQL — this is a deliberate service-layer control, name it explicitly in `SECURITY.md`)
- **Network**: exact-origin CORS whitelist, no wildcard
- **Observability**: Sentry wired in from the first commit
- **CI gates**: `mvn dependency-check` (or OWASP Dependency-Check plugin), TruffleHog (secret scanning), CodeQL — all blocking merges in GitHub Actions (matches the documented stack exactly here — no swap needed)
- **Compliance basics**: right-to-delete/right-to-export endpoints for customer data, 30-day log retention, zero hardcoded secrets

## 4. License

**Recommendation: Proprietary — All Rights Reserved.**

Same reasoning as SupplyForge — this is production-grade, IP-owned work, not a library for others to build on. If you decide later you want it as an open showcase, switching to MIT is a one-line LICENSE change — make that call explicitly, don't let it happen by default.

---

## 5. Project Structure

```
coresuite/
├── backend/
│   ├── product-service/         # Spring Boot — catalog, SKUs, pricing, variants
│   ├── crm-service/             # Spring Boot — customer profiles, comms history, segmentation
│   ├── inventory-service/       # Spring Boot — stock, warehouses, reorder thresholds
│   ├── order-service/           # Spring Boot — order lifecycle, status transitions
│   ├── reporting-service/       # Spring Boot — KPI aggregation, export generation
│   ├── api-gateway/             # Spring Cloud Gateway — routing, auth enforcement
│   └── shared/                  # auth lib, validation lib, common DTOs
├── frontend/
│   └── dashboard/                # React + TypeScript + Redux
│       ├── src/
│       │   ├── features/         # product, crm, inventory, orders, reporting slices
│       │   └── components/
│       └── tests/
├── infra/
│   ├── docker-compose.yml        # local: MySQL, MongoDB, Redis, all services
│   └── github-actions/           # CI: dependency-check, TruffleHog, CodeQL, tests, deploy
├── docs/
│   ├── project-plan.md           # this file, or project-planner's regenerated version
│   └── architecture.md           # module boundaries, monolith-to-microservice extraction plan
├── SECURITY.md
└── LICENSE
```

**Where each security control lives:**
- Auth/session logic → `backend/shared` module, imported by every service
- Rate limiting → Redis in `docker-compose.yml`, Spring filter in `api-gateway`
- IDOR checks → service-layer `@PreAuthorize` checks tied to resource ownership, not just role
- Input validation → `jakarta.validation` annotations on every DTO
- CI gates → `.github/workflows/ci.yml`

---

## 6. Build Phases

| Phase | Deliverable |
|---|---|
| 0 — Infra bootstrap | `docker-compose.yml` with MySQL, MongoDB, Redis; empty Spring Boot module skeletons; CI green |
| 1 — Product + Inventory | Catalog CRUD, variants, stock tracking, reorder alerts; unit + integration tests |
| 2 — CRM | Customer profiles, communication history, segmentation |
| 3 — Orders | Full order lifecycle, status transitions, history |
| 4 — Reporting | KPI dashboards, CSV/PDF export |
| 5 — Frontend integration | React/Redux dashboard wired to all backend modules through the API gateway |
| 6 — Security + observability hardening | Full baseline audit, Sentry wired, `@PreAuthorize` coverage verified on every mutation |
| 7 — Monolith-to-microservice extraction (optional stretch) | Pull `reporting-service` out as a standalone deployable — demonstrates the exact kind of incremental extraction Orion's team was doing when you joined |
| 8 — Deploy | Free-tier deploy (Fly.io/Render for backend, Vercel/Netlify for frontend), README + demo walkthrough for interviews |

---

## 7. How to hand this off to Claude Code

Unzip `claude-power-kit.zip`, run `./install.sh` once, then from an empty `coresuite/` directory run:

```
claude-power
```

Upload this file alongside — the `project-planner` subagent will use it as grounding context for its own `docs/project-plan.md`. The license/security/structure calls are already made here; if `project-planner` regenerates its own version, diff it against this one before accepting.
