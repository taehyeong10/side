package com.make.side.service;

import com.make.side.dto.MemberDto;
import com.make.side.entity.Member;
import com.make.side.repository.MemberJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Transactional(readOnly = true)
    public List<MemberDto> findAll() {
        return memberJpaRepository.findAll()
                .stream()
                .map(MemberDto::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MemberDto create(String name) {
        // Note: In a real application, you'd want to handle ID generation properly
        // For now, this is a placeholder implementation
        throw new UnsupportedOperationException("Member creation not yet fully implemented - requires ID generation strategy");
    }
}
