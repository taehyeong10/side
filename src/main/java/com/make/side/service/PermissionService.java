package com.make.side.service;

import com.make.side.dto.PermissionCheckDto;
import com.make.side.entity.Member;
import com.make.side.entity.Team;
import com.make.side.entity.TextPermission;
import com.make.side.repository.MemberTeamRepository;
import com.make.side.repository.TeamRepository;
import com.make.side.repository.TextPermissionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final TextPermissionRepository permissionRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final TeamRepository teamRepository;

    public PermissionService(
        TextPermissionRepository permissionRepository,
        MemberTeamRepository memberTeamRepository,
        TeamRepository teamRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.memberTeamRepository = memberTeamRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * Check if member has specific permission on text.
     * Four-tier checking:
     * 1. Is member the creator? → GRANTED (full access)
     * 2. Does member have individual permission? → GRANTED
     * 3. Is member in a team with permission? → GRANTED
     * 4. Is member in a descendant team? → GRANTED (via inheritance)
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(
        String textId,
        Long memberId,
        Long creatorId,
        TextPermission.OperationType operation
    ) {
        // Tier 1: Creator always has full access
        if (memberId.equals(creatorId)) {
            return true;
        }

        // Tier 2: Check individual member permission
        Optional<TextPermission> memberPermission = permissionRepository
            .findByTextIdAndMemberIdAndOperationType(textId, memberId, operation);
        if (memberPermission.isPresent()) {
            return true;
        }

        // Tier 3 & 4: Check team permissions (including inheritance)
        List<Long> memberTeamIds = getMemberTeamIds(memberId);
        if (memberTeamIds.isEmpty()) {
            return false;
        }

        // Get all team permissions for this text
        List<TextPermission> teamPermissions = permissionRepository.findTeamPermissionsByTextId(textId);

        // Check if member is in any team (or descendant) with permission
        for (TextPermission permission : teamPermissions) {
            if (permission.getOperationType() != operation) {
                continue;
            }

            Long grantedTeamId = permission.getTeam().getId();

            // Get all descendants (includes the team itself)
            List<Long> allowedTeamIds = teamRepository.findDescendantIds(grantedTeamId);

            // Check if member is in any allowed team
            if (memberTeamIds.stream().anyMatch(allowedTeamIds::contains)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all permissions for a member on a specific text.
     */
    @Transactional(readOnly = true)
    public PermissionCheckDto getPermissions(String textId, Long memberId, Long creatorId) {
        boolean isCreator = memberId.equals(creatorId);

        if (isCreator) {
            return new PermissionCheckDto(true, true, true, true);
        }

        boolean canRead = hasPermission(textId, memberId, creatorId, TextPermission.OperationType.READ);
        boolean canEdit = hasPermission(textId, memberId, creatorId, TextPermission.OperationType.EDIT);
        boolean canDelete = hasPermission(textId, memberId, creatorId, TextPermission.OperationType.DELETE);

        return new PermissionCheckDto(canRead, canEdit, canDelete, false);
    }

    /**
     * Grant permission to a team for a text.
     */
    @Transactional
    public void grantPermissionToTeam(
        String textId,
        Team team,
        TextPermission.OperationType operation,
        Member grantedBy
    ) {
        // Check if permission already exists
        if (permissionRepository.existsByTextIdAndTeamIdAndOperationType(textId, team.getId(), operation)) {
            throw new IllegalArgumentException("Permission already exists for this team");
        }

        TextPermission permission = new TextPermission(textId, team, operation, grantedBy);
        permissionRepository.save(permission);
    }

    /**
     * Grant permission to an individual member for a text.
     */
    @Transactional
    public void grantPermissionToMember(
        String textId,
        Member member,
        TextPermission.OperationType operation,
        Member grantedBy
    ) {
        // Check if permission already exists
        if (permissionRepository.existsByTextIdAndMemberIdAndOperationType(textId, member.getId(), operation)) {
            throw new IllegalArgumentException("Permission already exists for this member");
        }

        TextPermission permission = new TextPermission(textId, member, operation, grantedBy);
        permissionRepository.save(permission);
    }

    /**
     * Revoke permission from a team for a text.
     */
    @Transactional
    public void revokePermissionFromTeam(String textId, Long teamId, TextPermission.OperationType operation) {
        permissionRepository.deleteByTextIdAndTeamIdAndOperationType(textId, teamId, operation);
    }

    /**
     * Revoke permission from a member for a text.
     */
    @Transactional
    public void revokePermissionFromMember(String textId, Long memberId, TextPermission.OperationType operation) {
        permissionRepository.deleteByTextIdAndMemberIdAndOperationType(textId, memberId, operation);
    }

    /**
     * Get all permissions for a text.
     */
    @Transactional(readOnly = true)
    public List<TextPermission> getTextPermissions(String textId) {
        return permissionRepository.findByTextId(textId);
    }

    /**
     * Get member's team IDs (cached for performance).
     */
    @Cacheable(value = "memberTeams", key = "#memberId")
    public List<Long> getMemberTeamIds(Long memberId) {
        return memberTeamRepository.findTeamIdsByMemberId(memberId);
    }

    /**
     * Evict member's team cache (called when team membership changes).
     */
    @CacheEvict(value = "memberTeams", key = "#memberId")
    public void evictMemberTeamCache(Long memberId) {
        // Cache eviction happens automatically
    }
}
