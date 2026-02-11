package com.make.side.repository;

import com.make.side.entity.TextPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TextPermissionRepository extends JpaRepository<TextPermission, Long> {

    List<TextPermission> findByTextId(String textId);

    /**
     * Find individual member permission for a specific text and operation.
     */
    Optional<TextPermission> findByTextIdAndMemberIdAndOperationType(
        String textId,
        Long memberId,
        TextPermission.OperationType operationType
    );

    /**
     * Get all team permissions for a text (where team_id is not null).
     */
    @Query("SELECT tp FROM TextPermission tp WHERE tp.textId = :textId AND tp.team IS NOT NULL")
    List<TextPermission> findTeamPermissionsByTextId(@Param("textId") String textId);

    /**
     * Check if a team permission exists.
     */
    boolean existsByTextIdAndTeamIdAndOperationType(
        String textId,
        Long teamId,
        TextPermission.OperationType operationType
    );

    /**
     * Check if a member permission exists.
     */
    boolean existsByTextIdAndMemberIdAndOperationType(
        String textId,
        Long memberId,
        TextPermission.OperationType operationType
    );

    /**
     * Delete team permission.
     */
    void deleteByTextIdAndTeamIdAndOperationType(
        String textId,
        Long teamId,
        TextPermission.OperationType operationType
    );

    /**
     * Delete member permission.
     */
    void deleteByTextIdAndMemberIdAndOperationType(
        String textId,
        Long memberId,
        TextPermission.OperationType operationType
    );
}
