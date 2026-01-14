#!/bin/bash
set -e

# This script creates additional databases for PostgreSQL
# It runs automatically when the container is first initialized

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    -- Create keycloak database if it doesn't exist
    SELECT 'CREATE DATABASE keycloak'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec
    
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO postgres;
EOSQL

echo "✓ Additional databases created successfully"
