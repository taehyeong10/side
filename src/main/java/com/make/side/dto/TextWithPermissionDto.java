package com.make.side.dto;

import java.time.Instant;

public record TextWithPermissionDto(
    String id,
    Long memberId,
    String text,
    Instant createdAt,
    boolean canEdit,
    boolean canDelete,
    boolean isCreator
) {
}
