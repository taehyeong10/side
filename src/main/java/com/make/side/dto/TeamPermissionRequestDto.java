package com.make.side.dto;

import com.make.side.entity.TextPermission;

public class TeamPermissionRequestDto {
    private String textId;
    private Long teamId;
    private TextPermission.OperationType operationType;

    public TeamPermissionRequestDto() {
    }

    public TeamPermissionRequestDto(String textId, Long teamId, TextPermission.OperationType operationType) {
        this.textId = textId;
        this.teamId = teamId;
        this.operationType = operationType;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public TextPermission.OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(TextPermission.OperationType operationType) {
        this.operationType = operationType;
    }
}
