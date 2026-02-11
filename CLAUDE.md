# Claude Code Instructions - RBAC System

## Project Overview

This is a Spring Boot application with relationship-based access control (RBAC) for text document management. The system uses:

- **Keycloak** for authentication (JWT tokens)
- **PostgreSQL** for relational data (Members, Teams, Permissions)
- **Elasticsearch** for text documents (full-text search)
- **Hierarchical teams** with permission inheritance

## Architecture Principles

### Authorization Model

**Key Design Decisions:**

1. **Dual Permission Model**: Permissions can be granted to EITHER teams OR individual members
   - **Team Permission**: Grants access to all team members (with downward inheritance)
   - **Member Permission**: Grants access to a specific individual only
   - Both types can coexist on the same text

2. **Creator Always Has Access**: Text creators automatically have full READ/EDIT/DELETE privileges on their texts, regardless of team membership or explicit permissions.

3. **Downward Permission Inheritance**: When a permission is granted to a team, ALL descendant teams automatically inherit that permission. Example:
   - Grant READ to "Frontend Development" team
   - Both "React Team" and "Vue Team" (children) automatically get READ permission
   - This simplifies permission management for hierarchical organizations
   - **Note**: Inheritance applies ONLY to team permissions, NOT member permissions

4. **Leaf-Only Member Assignment**: Members can only join leaf teams (teams without children). This enforces organizational structure and prevents ambiguity.

5. **Auto-Member Creation**: When a user authenticates via Keycloak for the first time, a Member record is automatically created using their JWT subject claim as `external_id`.

### Database Architecture

```
PostgreSQL:
├── member (id, name, external_id, created_at)
├── team (id, name, parent_id, is_leaf, created_at)
├── member_team (member_id, team_id, joined_at)
└── text_permission (text_id, team_id, operation_type, granted_by, granted_at)

Elasticsearch:
└── texts (id, memberId, text, createdAt)
```

**Why split databases?**
- PostgreSQL: Relational integrity for teams/permissions
- Elasticsearch: Full-text search for documents

### Team Hierarchy

**Structure**: 3-layer maximum (0-indexed: 0, 1, 2)

```
Layer 0 (Root): Development Team
    ├── Layer 1 (Division): Frontend Development
    │   ├── Layer 2 (Leaf): React Team ✓ members can join
    │   └── Layer 2 (Leaf): Vue Team ✓ members can join
    └── Layer 1 (Division): Backend Development
        ├── Layer 2 (Leaf): API Team ✓ members can join
        └── Layer 2 (Leaf): Database Team ✓ members can join
```

**Constraints:**
- Maximum depth: 2 (enforced in TeamService.createTeam)
- Members only join leaf teams (enforced in Team.addMember and DB)
- A team becomes non-leaf when it gets children
- A team can only become leaf if it has no children

### Permission Model

**Operations**: READ, EDIT, DELETE

**Permission Types**:
- **Creator Permission**: Automatic → Creator always has full READ/EDIT/DELETE access
- **Team Permission**: Granted to a team → All team members (+ descendant team members) can access
- **Member Permission**: Granted to individual member → Only that member can access

**Permission Flow (Four-Tier Check)**:

**Tier 1 - Creator Check**:
- If requester is the creator (TextDocument.memberId) → GRANTED (all operations)

**Tier 2 - Individual Member Permission Check**:
- Query: Does this specific member have explicit permission for this operation?
- If yes → GRANTED

**Tier 3 - Direct Team Permission Check**:
- Get member's teams
- Query: Do any of member's teams have permission for this operation?
- If yes → GRANTED

**Tier 4 - Inherited Team Permission Check**:
- For each team with permission on this text:
  - Find all descendant teams (recursive)
  - Check if member belongs to any descendant
  - If yes → GRANTED (via downward inheritance)

**Example Scenarios**:

1. **Creator Access**: Alice creates text → Alice can READ/EDIT/DELETE automatically
2. **Team Permission**: Alice grants READ to "Frontend Development" → All members of Frontend, React Team, Vue Team can read
3. **Member Permission**: Alice grants EDIT to Bob → Only Bob can edit (no one else)
4. **Combined**: Alice grants READ to team AND EDIT to Bob individually → Team members can read, Bob can both read and edit

## Code Patterns

### Service Layer

**Database Schema - text_permission table**:

```sql
CREATE TABLE text_permission (
    id BIGSERIAL PRIMARY KEY,
    text_id VARCHAR(255) NOT NULL,
    team_id BIGINT,                    -- NULL if granted to member
    member_id BIGINT,                  -- NULL if granted to team
    operation_type VARCHAR(20) NOT NULL,
    granted_by BIGINT NOT NULL,
    CONSTRAINT chk_grantee CHECK (
        (team_id IS NOT NULL AND member_id IS NULL) OR
        (team_id IS NULL AND member_id IS NOT NULL)
    )
);
```

**Always inject dependencies via constructor** (existing pattern):
```java
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
}
```

**Use @Transactional for mutations**, `readOnly = true` for queries:
```java
@Transactional(readOnly = true)
public List<TeamDto> getAllTeams() { ... }

@Transactional
public void addMemberToTeam(Long memberId, Long teamId) { ... }
```

### DTO Pattern

**Use Records for read-only DTOs** (modern Java pattern):
```java
public record TeamDto(Long id, String name, Boolean isLeaf) {
    public static TeamDto from(Team team) {
        return new TeamDto(team.getId(), team.getName(), team.getIsLeaf());
    }
}
```

**Use traditional classes for request DTOs** (need setters for Jackson):
```java
public class TeamCreateRequestDto {
    private String name;
    private Long parentId;
    // Getters and setters
}
```

### Permission Checking

**In all text operations**, inject AuthenticationHelper and check permissions:

```java
@GetMapping("/texts/{id}")
public ResponseEntity<TextResponseDto> getTextById(@PathVariable String id) {
    Long currentMemberId = authHelper.getCurrentMemberId();
    // TextService.getTextById will check READ permission
    TextResponseDto response = textService.getTextById(id, currentMemberId);
    return ResponseEntity.ok(response);
}
```

**Service layer performs actual checks**:
```java
public TextResponseDto getTextById(String textId, Long requestingMemberId) {
    TextDocument document = textRepository.findById(textId).orElseThrow(...);

    // Pass creatorId for automatic creator access
    boolean hasPermission = permissionService.hasPermission(
        textId,
        requestingMemberId,
        document.getMemberId(),  // creatorId
        TextPermission.OperationType.READ
    );

    if (!hasPermission) {
        throw new SecurityException("No permission to read this text");
    }

    return toResponseDto(document);
}
```

### Cache Management

**Member team memberships are cached** to reduce DB load:

```java
@Cacheable(value = "memberTeams", key = "#memberId")
public List<Long> getMemberTeamIds(Long memberId) {
    return memberTeamRepository.findTeamIdsByMemberId(memberId);
}
```

**Cache must be evicted** when team membership changes:

```java
@CacheEvict(value = "memberTeams", key = "#memberId")
public void addMemberToTeam(Long memberId, Long teamId) {
    // ... add member logic
}
```

### Error Handling

**Use specific exceptions** for different scenarios:
- `SecurityException` → 403 Forbidden (no permission)
- `IllegalArgumentException` → 400 Bad Request (invalid input)
- `IllegalStateException` → 409 Conflict (business rule violation)

**GlobalExceptionHandler** converts these to proper HTTP responses.

## Implementation Guidelines

### When Adding New Features

1. **New Text Operations**: Always add permission checks with creatorId
   ```java
   if (!permissionService.hasPermission(textId, memberId, creatorId, operationType)) {
       throw new SecurityException("No permission");
   }
   ```

2. **Granting Permissions**: Choose between team and member permissions
   ```java
   // Grant to team (all team members + descendants get access)
   permissionService.grantPermissionToTeam(textId, teamId, operation, grantedBy);

   // Grant to individual member (only that member gets access)
   permissionService.grantPermissionToMember(textId, memberId, operation, grantedBy);
   ```

2. **New Team Operations**: Validate depth and leaf constraints
   ```java
   if (team.getDepth() >= 2) {
       throw new IllegalArgumentException("Max depth exceeded");
   }
   ```

3. **Modifying Permissions**: Always verify caller is the text owner
   ```java
   if (!document.getMemberId().equals(currentMemberId)) {
       throw new SecurityException("Only owner can grant permissions");
   }
   ```

### Security Checklist

Before deploying to production:

- [ ] All endpoints except `/actuator/health` require authentication
- [ ] All text operations check permissions via PermissionService
- [ ] JwtAuthenticationFilter creates members on first login
- [ ] Keycloak users have valid `sub` claims
- [ ] Cache eviction works for team membership changes
- [ ] Database constraints prevent invalid team structures
- [ ] Global exception handler returns appropriate status codes

### Testing Strategy

**Unit Tests**: Service layer logic
- PermissionService.hasPermission with various team structures
- TeamService.createTeam depth validation
- Team.addMember leaf-only constraint

**Integration Tests**: End-to-end flows
- Create team hierarchy → Add members → Grant permissions → Access text
- Verify downward inheritance (parent permission → child can access)
- Verify access denial (user not in team → 403)

**Manual Testing**: Full workflow
1. Authenticate via Keycloak → Member auto-created
2. Create team hierarchy (root → division → leaf)
3. Add authenticated member to leaf team
4. Create text document
5. Grant READ to division team
6. Verify leaf team members can read (inheritance)

## Common Tasks

### Add a New Permission Type

1. Add enum value: `TextPermission.OperationType.SHARE`
2. Update schema.sql constraint: `CHECK (operation_type IN ('READ', 'EDIT', 'DELETE', 'SHARE'))`
3. Add check method: `PermissionService.canShare()`
4. Use in controller: Validate before sharing operation

### Change Inheritance Model

To switch from downward to upward inheritance:

1. Modify `TeamRepository.findDescendants()` → `findAncestors()`
2. Update `PermissionService.hasPermission()` to use ancestors instead
3. Update tests to verify upward propagation

### Add Permission to Non-Leaf Teams

1. Remove leaf-only check in `PermissionService.grantPermission()`
2. Consider: Should permissions on non-leaf teams also apply to members directly?
3. Update business logic accordingly

## Maintenance Notes

### Database Migrations

**Schema changes require**:
1. Update `schema.sql` for new tables/columns
2. Add to `data.sql` for sample data
3. Restart application (schema recreated on startup with `ddl-auto: none` and `mode: always`)

**Production**: Use Flyway or Liquibase for versioned migrations (not currently configured)

### Performance Monitoring

**Watch for**:
- Cache hit rates for "memberTeams" cache
- Slow queries on permission checks (check indexes)
- Recursive CTE performance for deep hierarchies (currently max 3 layers is fine)

**Optimization opportunities**:
- Add Redis for distributed caching
- Materialize team hierarchy paths for faster lookups
- Denormalize permissions for read-heavy workloads

### Keycloak Integration

**Member.external_id** maps to Keycloak JWT `sub` claim.

**If users are missing**:
1. Check JwtAuthenticationFilter is registered in SecurityConfig
2. Verify Keycloak JWT contains `sub` claim
3. Check logs for auto-creation errors
4. Manually sync: Create Member with matching external_id

## File Locations Reference

### Core Authorization
- `/src/main/java/com/make/side/service/PermissionService.java` - Permission checking logic
- `/src/main/java/com/make/side/security/AuthenticationHelper.java` - Get current user
- `/src/main/java/com/make/side/security/JwtAuthenticationFilter.java` - Auto-create members

### Team Management
- `/src/main/java/com/make/side/entity/Team.java` - Team entity with hierarchy
- `/src/main/java/com/make/side/service/TeamService.java` - Team CRUD and validation
- `/src/main/java/com/make/side/repository/TeamRepository.java` - Recursive queries

### Permission Management
- `/src/main/java/com/make/side/entity/TextPermission.java` - Permission entity
- `/src/main/java/com/make/side/controller/PermissionController.java` - Grant/revoke API
- `/src/main/java/com/make/side/repository/TextPermissionRepository.java` - Permission queries

### Configuration
- `/src/main/resources/schema.sql` - Database schema
- `/src/main/resources/data.sql` - Sample data
- `/src/main/java/com/make/side/config/SecurityConfig.java` - Security setup
- `/src/main/java/com/make/side/config/CacheConfig.java` - Cache configuration

## Tips for Claude Code

When asked to modify this system:

1. **Always maintain team hierarchy constraints** - 3 layers max, leaf-only members
2. **Never bypass permission checks** - All text access must go through PermissionService with creatorId
3. **Respect dual permission model** - Support both team and member permissions
4. **Respect inheritance model** - Downward cascade for teams is intentional, not a bug
5. **Update cache eviction** - When modifying team membership, evict cache
6. **Follow DTO patterns** - Records for responses, classes for requests
7. **Maintain creator privileges** - Creator always has full access automatically

When debugging permission issues:

1. Check: Is user the creator? (memberId equals document.getMemberId()) - Creator always has access
2. Check: Is user authenticated? (AuthenticationHelper.getCurrentMemberId())
3. Check: Does Member have external_id? (Auto-created on first login)
4. Check: Does user have individual permission? (TextPermissionRepository.findByTextIdAndMemberIdAndOperationType)
5. Check: Is Member in any teams? (TeamService.getMemberTeams)
6. Check: Do those teams have permission? (PermissionService.getTextPermissions)
7. Check: Is inheritance working? (TeamRepository.findDescendantIds)

## Future Enhancements

Potential features to add:

- **Role-based permissions**: Team admins who can grant permissions
- **Temporary permissions**: Time-limited access grants
- **Permission templates**: Pre-defined permission sets for common scenarios
- **Audit logging**: Track all permission grants/revokes
- **Bulk operations**: Assign multiple members to teams at once
- **Team ownership**: Designated team owners with management privileges