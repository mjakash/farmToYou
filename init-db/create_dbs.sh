#!/bin/bash
set -e

# This script will be executed as the 'postgres' user
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE user_service_db;
    CREATE DATABASE product_service_db;
    CREATE DATABASE order_service_db;
    CREATE DATABASE inventory_service_db;
    CREATE DATABASE delivery_service_db;
EOSQL