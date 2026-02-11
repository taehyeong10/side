package com.make.side.dto;

public record PermissionCheckDto(
    boolean canRead,
    boolean canEdit,
    boolean canDelete,
    boolean isCreator
) {
}
