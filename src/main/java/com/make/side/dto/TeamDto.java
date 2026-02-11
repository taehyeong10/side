package com.make.side.dto;

import com.make.side.entity.Team;

public record TeamDto(
    Long id,
    String name,
    Long parentId,
    Boolean isLeaf,
    Integer depth
) {
    public static TeamDto from(Team team) {
        return new TeamDto(
            team.getId(),
            team.getName(),
            team.getParent() != null ? team.getParent().getId() : null,
            team.getIsLeaf(),
            team.getDepth()
        );
    }
}
