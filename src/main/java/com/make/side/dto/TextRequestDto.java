package com.make.side.dto;

public class TextRequestDto {
    private Long memberId;
    private String text;

    public TextRequestDto() {
    }

    public TextRequestDto(Long memberId, String text) {
        this.memberId = memberId;
        this.text = text;
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
}
