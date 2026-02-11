package com.make.side.controller;

import com.make.side.dto.TeamCreateRequestDto;
import com.make.side.dto.TeamDto;
import com.make.side.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@RequestBody TeamCreateRequestDto request) {
        TeamDto team = teamService.createTeam(
            request.getName(),
            request.getParentId(),
            request.getIsLeaf()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @GetMapping
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        List<TeamDto> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<TeamDto>> getRootTeams() {
        List<TeamDto> teams = teamService.getRootTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<TeamDto>> getLeafTeams() {
        List<TeamDto> teams = teamService.getLeafTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeam(@PathVariable Long id) {
        TeamDto team = teamService.getTeam(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<TeamDto>> getChildTeams(@PathVariable Long id) {
        List<TeamDto> teams = teamService.getChildTeams(id);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> addMemberToTeam(
        @PathVariable Long teamId,
        @PathVariable Long memberId
    ) {
        teamService.addMemberToTeam(memberId, teamId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> removeMemberFromTeam(
        @PathVariable Long teamId,
        @PathVariable Long memberId
    ) {
        teamService.removeMemberFromTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<TeamDto>> getMemberTeams(@PathVariable Long memberId) {
        List<TeamDto> teams = teamService.getMemberTeams(memberId);
        return ResponseEntity.ok(teams);
    }
}
