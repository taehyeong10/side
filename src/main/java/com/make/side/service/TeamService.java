package com.make.side.service;

import com.make.side.dto.TeamDto;
import com.make.side.entity.Member;
import com.make.side.entity.MemberTeam;
import com.make.side.entity.Team;
import com.make.side.entity.TeamFactory;
import com.make.side.repository.MemberJpaRepository;
import com.make.side.repository.MemberTeamRepository;
import com.make.side.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberJpaRepository memberRepository;
    private final TeamFactory teamFactory;
    private final PermissionService permissionService;

    public TeamService(
        TeamRepository teamRepository,
        MemberTeamRepository memberTeamRepository,
        MemberJpaRepository memberRepository,
        TeamFactory teamFactory,
        PermissionService permissionService
    ) {
        this.teamRepository = teamRepository;
        this.memberTeamRepository = memberTeamRepository;
        this.memberRepository = memberRepository;
        this.teamFactory = teamFactory;
        this.permissionService = permissionService;
    }

    @Transactional
    public TeamDto createTeam(String name, Long parentId, Boolean isLeaf) {
        Team team = teamFactory.createTeam(name);

        if (parentId != null) {
            Team parent = teamRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent team not found: " + parentId));

            // Validate depth (max 3 layers: 0, 1, 2)
            if (parent.getDepth() >= 2) {
                throw new IllegalArgumentException("Cannot create team: maximum depth (3 layers) exceeded");
            }

            team.setParent(parent);
        }

        if (Boolean.TRUE.equals(isLeaf)) {
            team.markAsLeaf();
        }

        Team saved = teamRepository.save(team);
        return TeamDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll()
            .stream()
            .map(TeamDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getRootTeams() {
        return teamRepository.findByParentIsNull()
            .stream()
            .map(TeamDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getChildTeams(Long parentId) {
        return teamRepository.findByParentId(parentId)
            .stream()
            .map(TeamDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getLeafTeams() {
        return teamRepository.findByIsLeafTrue()
            .stream()
            .map(TeamDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDto getTeam(Long id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        return TeamDto.from(team);
    }

    /**
     * Add member to team. Only leaf teams can have members.
     * Evicts cache for member's team list.
     */
    @Transactional
    public void addMemberToTeam(Long memberId, Long teamId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        if (!team.getIsLeaf()) {
            throw new IllegalArgumentException("Cannot add member to non-leaf team");
        }

        // Check if already member
        if (memberTeamRepository.existsByMemberIdAndTeamId(memberId, teamId)) {
            throw new IllegalArgumentException("Member already belongs to this team");
        }

        MemberTeam memberTeam = new MemberTeam(member, team);
        memberTeamRepository.save(memberTeam);

        team.addMember(member);

        // Evict cache
        permissionService.evictMemberTeamCache(memberId);
    }

    /**
     * Remove member from team.
     * Evicts cache for member's team list.
     */
    @Transactional
    public void removeMemberFromTeam(Long memberId, Long teamId) {
        MemberTeam.MemberTeamId id = new MemberTeam.MemberTeamId(memberId, teamId);

        if (!memberTeamRepository.existsById(id)) {
            throw new IllegalArgumentException("Member is not part of this team");
        }

        memberTeamRepository.deleteById(id);

        // Evict cache
        permissionService.evictMemberTeamCache(memberId);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getMemberTeams(Long memberId) {
        return memberTeamRepository.findByMemberId(memberId)
            .stream()
            .map(mt -> TeamDto.from(mt.getTeam()))
            .collect(Collectors.toList());
    }
}
