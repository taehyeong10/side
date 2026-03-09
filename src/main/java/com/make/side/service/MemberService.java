package com.make.side.service;

import com.make.side.dto.MemberDto;
import com.make.side.entity.Member;
import com.make.side.repository.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {
    private final MemberJpaRepository memberJpaRepository;
    private final EntityManager entityManager;

    public MemberService(MemberJpaRepository memberJpaRepository, EntityManager entityManager) {
        this.memberJpaRepository = memberJpaRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Optional<MemberDto> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberDto::from);
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
        Member member = new Member();
        member.setName(name);
        member.setTime(Instant.now());
        memberJpaRepository.save(member);
        return MemberDto.from(member);
    }

    // [수정] dirty checking 확인
    // 조회된 엔티티는 영속성 컨텍스트가 관리 → setter만 호출해도 커밋 시 UPDATE 자동 실행
    // save() 를 명시적으로 호출하지 않아도 변경이 DB에 반영됨
    @Transactional
    public MemberDto updateName(Long id, String name) {
        Member member = memberJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));

        member.setName(name); // save() 호출 없음 → dirty checking이 변경 감지

        return MemberDto.from(member);
    }
}
