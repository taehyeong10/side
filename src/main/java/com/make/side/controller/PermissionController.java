package com.make.side.controller;

import com.make.side.dto.MemberPermissionRequestDto;
import com.make.side.dto.PermissionCheckDto;
import com.make.side.dto.TeamPermissionRequestDto;
import com.make.side.entity.Member;
import com.make.side.entity.Team;
import com.make.side.entity.TextDocument;
import com.make.side.entity.TextPermission;
import com.make.side.repository.MemberJpaRepository;
import com.make.side.repository.TeamRepository;
import com.make.side.repository.TextRepository;
import com.make.side.security.AuthenticationHelper;
import com.make.side.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final AuthenticationHelper authHelper;
    private final TextRepository textRepository;
    private final TeamRepository teamRepository;
    private final MemberJpaRepository memberRepository;

    public PermissionController(
        PermissionService permissionService,
        AuthenticationHelper authHelper,
        TextRepository textRepository,
        TeamRepository teamRepository,
        MemberJpaRepository memberRepository
    ) {
        this.permissionService = permissionService;
        this.authHelper = authHelper;
        this.textRepository = textRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Grant permission to a team for a text.
     * Only the creator can grant permissions.
     */
    @PostMapping("/grant/team")
    public ResponseEntity<Void> grantPermissionToTeam(@RequestBody TeamPermissionRequestDto request) {
        Member currentMember = authHelper.getCurrentMember();

        // Verify the requester is the creator
        TextDocument document = textRepository.findById(request.getTextId())
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        if (!document.getMemberId().equals(currentMember.getId())) {
            throw new SecurityException("Only the creator can grant permissions");
        }

        Team team = teamRepository.findById(request.getTeamId())
            .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        permissionService.grantPermissionToTeam(
            request.getTextId(),
            team,
            request.getOperationType(),
            currentMember
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Grant permission to an individual member for a text.
     * Only the creator can grant permissions.
     */
    @PostMapping("/grant/member")
    public ResponseEntity<Void> grantPermissionToMember(@RequestBody MemberPermissionRequestDto request) {
        Member currentMember = authHelper.getCurrentMember();

        // Verify the requester is the creator
        TextDocument document = textRepository.findById(request.getTextId())
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        if (!document.getMemberId().equals(currentMember.getId())) {
            throw new SecurityException("Only the creator can grant permissions");
        }

        Member targetMember = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        permissionService.grantPermissionToMember(
            request.getTextId(),
            targetMember,
            request.getOperationType(),
            currentMember
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Revoke permission from a team for a text.
     * Only the creator can revoke permissions.
     */
    @DeleteMapping("/revoke/team")
    public ResponseEntity<Void> revokePermissionFromTeam(@RequestBody TeamPermissionRequestDto request) {
        Member currentMember = authHelper.getCurrentMember();

        // Verify the requester is the creator
        TextDocument document = textRepository.findById(request.getTextId())
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        if (!document.getMemberId().equals(currentMember.getId())) {
            throw new SecurityException("Only the creator can revoke permissions");
        }

        permissionService.revokePermissionFromTeam(
            request.getTextId(),
            request.getTeamId(),
            request.getOperationType()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Revoke permission from a member for a text.
     * Only the creator can revoke permissions.
     */
    @DeleteMapping("/revoke/member")
    public ResponseEntity<Void> revokePermissionFromMember(@RequestBody MemberPermissionRequestDto request) {
        Member currentMember = authHelper.getCurrentMember();

        // Verify the requester is the creator
        TextDocument document = textRepository.findById(request.getTextId())
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        if (!document.getMemberId().equals(currentMember.getId())) {
            throw new SecurityException("Only the creator can revoke permissions");
        }

        permissionService.revokePermissionFromMember(
            request.getTextId(),
            request.getMemberId(),
            request.getOperationType()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Check what permissions the current user has on a text.
     */
    @GetMapping("/check/{textId}")
    public ResponseEntity<PermissionCheckDto> checkPermissions(@PathVariable String textId) {
        Member currentMember = authHelper.getCurrentMember();

        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        PermissionCheckDto permissions = permissionService.getPermissions(
            textId,
            currentMember.getId(),
            document.getMemberId()
        );

        return ResponseEntity.ok(permissions);
    }

    /**
     * Get all permissions for a text (creator only).
     */
    @GetMapping("/text/{textId}")
    public ResponseEntity<List<TextPermission>> getTextPermissions(@PathVariable String textId) {
        Member currentMember = authHelper.getCurrentMember();

        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found"));

        if (!document.getMemberId().equals(currentMember.getId())) {
            throw new SecurityException("Only the creator can view all permissions");
        }

        List<TextPermission> permissions = permissionService.getTextPermissions(textId);
        return ResponseEntity.ok(permissions);
    }
}
