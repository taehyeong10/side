package com.make.side.dto;

import java.time.Instant;

public class TextResponseDto {
    private String id;
    private Long memberId;
    private String text;
    private Instant createdAt;

    public TextResponseDto() {
    }

    public TextResponseDto(String id, Long memberId, String text, Instant createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
