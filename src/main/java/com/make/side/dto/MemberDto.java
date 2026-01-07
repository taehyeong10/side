package com.make.side.dto;

import com.make.side.entity.Member;

public record MemberDto(Long id, String name) {
    public static MemberDto from(Member member) {
        return new MemberDto(member.getId(), member.getName());
    }
}
