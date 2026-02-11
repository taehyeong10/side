package com.make.side.repository;

import com.make.side.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByParentIsNull();

    List<Team> findByParentId(Long parentId);

    List<Team> findByIsLeafTrue();

    Optional<Team> findByName(String name);

    /**
     * Find all descendant team IDs for a given team (includes the team itself).
     * Uses recursive CTE for downward inheritance.
     */
    @Query(value = """
        WITH RECURSIVE team_hierarchy AS (
            SELECT id FROM team WHERE id = :teamId
            UNION ALL
            SELECT t.id FROM team t
            INNER JOIN team_hierarchy th ON t.parent_id = th.id
        )
        SELECT id FROM team_hierarchy
        """, nativeQuery = true)
    List<Long> findDescendantIds(@Param("teamId") Long teamId);
}
