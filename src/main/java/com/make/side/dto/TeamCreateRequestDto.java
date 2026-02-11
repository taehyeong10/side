package com.make.side.dto;

public class TeamCreateRequestDto {
    private String name;
    private Long parentId;
    private Boolean isLeaf;

    public TeamCreateRequestDto() {
    }

    public TeamCreateRequestDto(String name, Long parentId, Boolean isLeaf) {
        this.name = name;
        this.parentId = parentId;
        this.isLeaf = isLeaf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
}
