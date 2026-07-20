# CoreSuite

Enterprise business management platform — product catalog, CRM, inventory
control, order processing, and reporting/analytics as one modular-monolith
system, built to be extracted into microservices incrementally.

Full rationale, tech stack, and phased build plan: [`docs/project-plan.md`](docs/project-plan.md).
Module boundaries and data ownership: [`docs/architecture.md`](docs/architecture.md).
Security controls and status: [`SECURITY.md`](SECURITY.md).

## Status

**Phase 6a — full stack, authenticated end-to-end.**

- `product-service` — catalog, SKUs, variants, pricing (own MySQL database)
- `inventory-service` — warehouses, stock levels, reorder alerts (own MySQL
  database, optimistic locking)
- `crm-service` — customer profiles and tag-based segmentation (own MySQL
  database), communication history log (MongoDB)
- `order-service` — order lifecycle with an enforced status state machine
  (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED, cancellable up to
  PROCESSING) and full status-change history (own MySQL database). Line items
  snapshot SKU and price at order time rather than live-querying
  `product-service`, so historical orders stay accurate if catalog prices
  change later.
- `reporting-service` — KPI dashboard (order counts, revenue, low-stock
  alerts) and CSV/PDF order export. Holds no database of its own — it reads
  live from `order-service` and `inventory-service` over HTTP (their public
  APIs, never their databases directly, per `docs/architecture.md`).
- `api-gateway` — Spring Cloud Gateway routing every service under one origin,
  with an exact-origin CORS whitelist, **and now real authentication**: owns
  user accounts (Argon2id password hashing) and Redis-backed sessions
  (HttpOnly/Secure/SameSite=Strict cookie), rate-limits login attempts, and
  attaches a trusted identity to every proxied request. Every backend service
  requires that trusted identity to answer any request at all — verified with
  tests that prove rejection, not just that authenticated calls happen to
  work.
- `frontend/dashboard` — a real React/Redux Toolkit Query UI for all five
  domains (list + create flows, order status transitions, a KPI view) behind
  a login screen, talking only to the gateway, never to a service directly.

Each MySQL-backed service owns its own database on the shared MySQL instance
(`product_service`, `crm_service`, `inventory_service`, `order_service`,
`auth_service` — created by `infra/mysql-init` on first boot), not just
separate tables in a shared schema — Flyway's non-empty-schema safety check
operates per database, so a genuinely shared schema breaks the moment a
second service starts against it. `reporting-service` tests its downstream
HTTP calls with `MockRestServiceServer` instead of Testcontainers, since it
holds no data of its own. Still explicitly deferred: TOTP 2FA, field-level
encryption, Sentry observability, and the compliance (right-to-delete/export)
endpoints — see [`SECURITY.md`](SECURITY.md) for the full status table and
what "IDOR" means for an internal tool rather than a customer-facing SaaS.
See [`docs/project-plan.md`](docs/project-plan.md#6-build-phases) for what's
next.

## Stack

- **Backend**: Java 21, Spring Boot 3, Maven multi-module reactor
- **Frontend**: React, TypeScript, Redux Toolkit, Vite
- **Databases**: MySQL (transactional core), MongoDB (unstructured/CRM data)
- **Cache/rate limiting**: Redis
- **CI**: GitHub Actions (build, test, dependency audit, secret scan, CodeQL)

## Prerequisites

- JDK 21 (`brew install openjdk@21`)
- Maven (`brew install maven`)
- Node.js 20+ and npm
- Docker (for local MySQL/MongoDB/Redis)

## Local setup

```bash
# 1. Start local infra (MySQL, MongoDB, Redis). MySQL creates one database
#    per service on first boot via infra/mysql-init — see infra/data/ being
#    populated as a sign it worked.
cd infra
cp .env.example .env   # fill in local dev credentials
docker compose up -d
cd ..

# 2. Build and test the backend (product-service, inventory-service,
#    crm-service, order-service, and api-gateway run integration tests
#    against real, throwaway MySQL/MongoDB/Redis instances via
#    Testcontainers — Docker must be running; reporting-service needs no
#    database)
cd backend
mvn clean install

# 3. Run each service — env vars must match infra/.env. Ports: product 8081,
#    crm 8082, inventory 8083, order 8084, reporting 8085, gateway 8080.
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> mvn -pl product-service spring-boot:run &
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> \
  MONGO_USERNAME=<MONGO_ROOT_USERNAME> MONGO_PASSWORD=<MONGO_ROOT_PASSWORD> \
  mvn -pl crm-service spring-boot:run &
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> mvn -pl inventory-service spring-boot:run &
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> mvn -pl order-service spring-boot:run &
mvn -pl reporting-service spring-boot:run &
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> REDIS_PASSWORD=<REDIS_PASSWORD> \
  mvn -pl api-gateway spring-boot:run &

# 4. Create your account — the first registration becomes ADMIN, every one
#    after is STAFF. There's no seed user; nothing works until you do this.
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"at least 12 characters"}'

# 5. Run the frontend — talks to the gateway at http://localhost:8080 by
#    default (override with frontend/dashboard/.env.local, see .env.example)
cd frontend/dashboard
npm install
npm run dev
```

## Project structure

```
coresuite/
├── backend/               # Maven multi-module Spring Boot reactor
│   ├── shared/             # auth/session lib, validation, common DTOs
│   ├── product-service/
│   ├── crm-service/
│   ├── inventory-service/
│   ├── order-service/
│   ├── reporting-service/
│   └── api-gateway/
├── frontend/
│   └── dashboard/          # React + TypeScript + Redux Toolkit
├── infra/
│   ├── docker-compose.yml  # local MySQL, MongoDB, Redis
│   └── mysql-init/         # creates one database per service on first boot
├── docs/
│   ├── project-plan.md
│   └── architecture.md
├── SECURITY.md
└── LICENSE
```

## License

Proprietary — All Rights Reserved. See [`LICENSE`](LICENSE).
