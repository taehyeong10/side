package com.make.side.dto;

import com.make.side.entity.TextPermission;

public class MemberPermissionRequestDto {
    private String textId;
    private Long memberId;
    private TextPermission.OperationType operationType;

    public MemberPermissionRequestDto() {
    }

    public MemberPermissionRequestDto(String textId, Long memberId, TextPermission.OperationType operationType) {
        this.textId = textId;
        this.memberId = memberId;
        this.operationType = operationType;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public TextPermission.OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(TextPermission.OperationType operationType) {
        this.operationType = operationType;
    }
}
