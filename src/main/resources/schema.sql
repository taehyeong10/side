-- Schema initialization script
-- This script runs automatically when the application starts
-- It drops all existing tables and creates fresh ones

-- Drop and recreate the public schema to remove all tables
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Member table (updated with external_id for Keycloak mapping)
CREATE TABLE member (
    id bigint PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_external_id ON member(external_id);

-- Team hierarchy table
CREATE TABLE team (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    is_leaf BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_parent FOREIGN KEY (parent_id) REFERENCES team(id) ON DELETE CASCADE
);

CREATE INDEX idx_team_parent_id ON team(parent_id);
CREATE INDEX idx_team_is_leaf ON team(is_leaf);

-- Member-Team relationship (leaf teams only)
CREATE TABLE member_team (
    member_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id, team_id),
    CONSTRAINT fk_member_team_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_team_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE
);

CREATE INDEX idx_member_team_member ON member_team(member_id);
CREATE INDEX idx_member_team_team ON member_team(team_id);

-- Text permissions table (supports both team and member permissions)
CREATE TABLE text_permission (
    id BIGSERIAL PRIMARY KEY,
    text_id VARCHAR(255) NOT NULL,
    team_id BIGINT,
    member_id BIGINT,
    operation_type VARCHAR(20) NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT NOT NULL,
    CONSTRAINT fk_text_permission_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
    CONSTRAINT fk_text_permission_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_text_permission_granted_by FOREIGN KEY (granted_by) REFERENCES member(id),
    CONSTRAINT chk_operation_type CHECK (operation_type IN ('READ', 'EDIT', 'DELETE')),
    CONSTRAINT chk_grantee CHECK (
        (team_id IS NOT NULL AND member_id IS NULL) OR
        (team_id IS NULL AND member_id IS NOT NULL)
    ),
    UNIQUE (text_id, team_id, operation_type),
    UNIQUE (text_id, member_id, operation_type)
);

CREATE INDEX idx_text_permission_text ON text_permission(text_id);
CREATE INDEX idx_text_permission_team ON text_permission(team_id);
CREATE INDEX idx_text_permission_member ON text_permission(member_id);
