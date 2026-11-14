-- Database-per-service: each service owns its schema; nothing is shared.
-- Runs once, on the first start of an empty Postgres volume.
CREATE DATABASE productdb;
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;
CREATE DATABASE keycloakdb;
