package com.make.side.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "member_team")
public class MemberTeam {

    @EmbeddedId
    private MemberTeamId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "joined_at")
    private Instant joinedAt;

    protected MemberTeam() {
    }

    public MemberTeam(Member member, Team team) {
        this.member = member;
        this.team = team;
        this.id = new MemberTeamId(member.getId(), team.getId());
        this.joinedAt = Instant.now();
    }

    // Getters
    public MemberTeamId getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Team getTeam() {
        return team;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    @Embeddable
    public static class MemberTeamId implements Serializable {
        @Column(name = "member_id")
        private Long memberId;

        @Column(name = "team_id")
        private Long teamId;

        protected MemberTeamId() {
        }

        public MemberTeamId(Long memberId, Long teamId) {
            this.memberId = memberId;
            this.teamId = teamId;
        }

        public Long getMemberId() {
            return memberId;
        }

        public Long getTeamId() {
            return teamId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MemberTeamId)) return false;
            MemberTeamId that = (MemberTeamId) o;
            return memberId.equals(that.memberId) && teamId.equals(that.teamId);
        }

        @Override
        public int hashCode() {
            return memberId.hashCode() * 31 + teamId.hashCode();
        }
    }
}
