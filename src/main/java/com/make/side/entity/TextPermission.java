package com.make.side.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "text_permission")
public class TextPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_id", nullable = false)
    private String textId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private Member grantedBy;

    @Column(name = "granted_at")
    private Instant grantedAt;

    protected TextPermission() {
    }

    // Constructor for team permission
    public TextPermission(String textId, Team team, OperationType operationType, Member grantedBy) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null for team permission");
        }
        this.textId = textId;
        this.team = team;
        this.operationType = operationType;
        this.grantedBy = grantedBy;
        this.grantedAt = Instant.now();
    }

    // Constructor for member permission
    public TextPermission(String textId, Member member, OperationType operationType, Member grantedBy) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null for member permission");
        }
        this.textId = textId;
        this.member = member;
        this.operationType = operationType;
        this.grantedBy = grantedBy;
        this.grantedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTextId() {
        return textId;
    }

    public Team getTeam() {
        return team;
    }

    public Member getMember() {
        return member;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Member getGrantedBy() {
        return grantedBy;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public boolean isTeamPermission() {
        return team != null;
    }

    public boolean isMemberPermission() {
        return member != null;
    }

    public enum OperationType {
        READ, EDIT, DELETE
    }
}
