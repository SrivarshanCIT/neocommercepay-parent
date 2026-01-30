#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE neocommercepay_users;
    CREATE DATABASE neocommercepay_orders;
    CREATE DATABASE neocommercepay_payments;
EOSQL

echo "PostgreSQL databases created successfully"
