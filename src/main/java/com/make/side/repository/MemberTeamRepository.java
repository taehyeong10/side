package com.make.side.repository;

import com.make.side.entity.MemberTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberTeamRepository extends JpaRepository<MemberTeam, MemberTeam.MemberTeamId> {

    List<MemberTeam> findByMemberId(Long memberId);

    List<MemberTeam> findByTeamId(Long teamId);

    @Query("SELECT mt.team.id FROM MemberTeam mt WHERE mt.member.id = :memberId")
    List<Long> findTeamIdsByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndTeamId(Long memberId, Long teamId);
}
