-- Each service gets its own database on the shared MySQL instance, not just
-- separate tables in a shared schema. Flyway's non-empty-schema safety check
-- operates at the database level: with one shared "coresuite" schema, the
-- second service to start would find tables already there (from whichever
-- service started first) and refuse to migrate. Separate databases is also
-- the standard microservices pattern regardless of that Flyway detail.
CREATE DATABASE IF NOT EXISTS product_service;
CREATE DATABASE IF NOT EXISTS crm_service;
CREATE DATABASE IF NOT EXISTS inventory_service;
CREATE DATABASE IF NOT EXISTS order_service;
