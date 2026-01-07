-- Schema initialization script
-- This script runs automatically when the application starts
-- It drops all existing tables and creates fresh ones

-- Drop and recreate the public schema to remove all tables
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Add your table creation statements below:
-- Example: Create a sample table (remove or modify as needed)
CREATE TABLE member (
    id bigint PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add your table definitions here:
