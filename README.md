# CoreSuite

Enterprise business management platform — product catalog, CRM, inventory
control, order processing, and reporting/analytics as one modular-monolith
system, built to be extracted into microservices incrementally.

Full rationale, tech stack, and phased build plan: [`docs/project-plan.md`](docs/project-plan.md).
Module boundaries and data ownership: [`docs/architecture.md`](docs/architecture.md).
Security controls and status: [`SECURITY.md`](SECURITY.md).

## Status

**Phase 4 — Product + Inventory + CRM + Orders + Reporting.**

- `product-service` — catalog, SKUs, variants, pricing (MySQL)
- `inventory-service` — warehouses, stock levels, reorder alerts (MySQL,
  optimistic locking)
- `crm-service` — customer profiles and tag-based segmentation (MySQL),
  communication history log (MongoDB)
- `order-service` — order lifecycle with an enforced status state machine
  (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED, cancellable up to
  PROCESSING) and full status-change history (MySQL). Line items snapshot SKU
  and price at order time rather than live-querying `product-service`, so
  historical orders stay accurate if catalog prices change later.
- `reporting-service` — KPI dashboard (order counts, revenue, low-stock
  alerts) and CSV/PDF order export. Holds no database of its own — it reads
  live from `order-service` and `inventory-service` over HTTP (their public
  APIs, never their databases directly, per `docs/architecture.md`).

All five are real, tested APIs. The first four have Flyway-migrated schemas,
layered controller/service/repository code, input validation on every
endpoint, and integration tests against real MySQL/MongoDB via Testcontainers.
`reporting-service`, being stateless, tests its downstream HTTP calls with
`MockRestServiceServer` instead. `api-gateway` is still a Phase 0 skeleton.
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
# 1. Start local infra (MySQL, MongoDB, Redis)
cd infra
cp .env.example .env   # fill in local dev credentials
docker compose up -d
cd ..

# 2. Build and test the backend (product-service, inventory-service, and
#    crm-service run integration tests against real, throwaway MySQL/MongoDB
#    instances via Testcontainers — Docker must be running)
cd backend
mvn clean install

# 3. Run a service against your local infra — env vars must match infra/.env
cd product-service
DB_USERNAME=root DB_PASSWORD=<your MYSQL_ROOT_PASSWORD from infra/.env> \
  mvn spring-boot:run

# crm-service additionally needs Mongo credentials:
cd ../crm-service
DB_USERNAME=root DB_PASSWORD=<MYSQL_ROOT_PASSWORD> \
  MONGO_USERNAME=<MONGO_ROOT_USERNAME> MONGO_PASSWORD=<MONGO_ROOT_PASSWORD> \
  mvn spring-boot:run

# 4. Run the frontend
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
│   └── docker-compose.yml  # local MySQL, MongoDB, Redis
├── docs/
│   ├── project-plan.md
│   └── architecture.md
├── SECURITY.md
└── LICENSE
```

## License

Proprietary — All Rights Reserved. See [`LICENSE`](LICENSE).
