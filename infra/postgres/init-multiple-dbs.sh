#!/bin/bash
# Creates one Postgres database per microservice that needs relational storage.
# Reason: each service owns its schema independently (database-per-service pattern)
# even though they share one Postgres container for local-dev simplicity.
set -e

for db in customerdb provisioningdb billingdb; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE $db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done
