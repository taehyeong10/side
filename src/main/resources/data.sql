-- Data initialization script (optional)
-- This script runs after schema.sql
-- Add your initial data inserts here

-- Sample members (external_id will be set on first login via Keycloak)
INSERT INTO member(id, name) VALUES
    (1, 'John Doe'),
    (2, 'Jane Smith'),
    (3, 'Bob Johnson');

-- Sample team hierarchy (3 layers: 0=root, 1=division, 2=leaf)
INSERT INTO team(id, name, parent_id, is_leaf) VALUES
    (1, 'Development Team', NULL, false),           -- Layer 0: Root
    (2, 'Frontend Development', 1, false),          -- Layer 1: Division
    (3, 'Backend Development', 1, false),           -- Layer 1: Division
    (4, 'React Team', 2, true),                     -- Layer 2: Leaf
    (5, 'Vue Team', 2, true),                       -- Layer 2: Leaf
    (6, 'API Team', 3, true),                       -- Layer 2: Leaf
    (7, 'Database Team', 3, true);                  -- Layer 2: Leaf

-- Sample member-team assignments (only leaf teams)
INSERT INTO member_team(member_id, team_id) VALUES
    (1, 4),  -- John in React Team
    (2, 6),  -- Jane in API Team
    (3, 7);  -- Bob in Database Team

-- Reset sequences for auto-increment
SELECT setval('team_id_seq', (SELECT MAX(id) FROM team));