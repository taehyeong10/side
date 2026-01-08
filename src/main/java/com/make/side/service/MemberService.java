package com.make.side.service;

import com.make.side.dto.MemberDto;
import com.make.side.entity.Member;
import com.make.side.repository.MemberJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {
    private final MemberJpaRepository memberJpaRepository;

    public MemberService(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    public Optional<MemberDto> findById(Long id) {
        Optional<Member> member = memberJpaRepository.findById(id);
        return member.map(MemberDto::from);
    }
}
