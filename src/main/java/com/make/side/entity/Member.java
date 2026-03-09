package com.make.side.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

// [dirty checking 확인]
// @Transactional 메서드 내에서 조회된 엔티티는 영속성 컨텍스트가 관리함.
// 필드를 변경하면 트랜잭션 커밋 시점에 Hibernate가 변경을 감지하고 UPDATE 쿼리를 자동 실행함.
// → repository.save() 호출 불필요.

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "created_at")
    private Instant time;

    @ManyToMany(mappedBy = "members")
    private Set<Team> teams = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public Instant getTime() {
        return time;
    }

    public Set<Team> getTeams() {
        return teams;
    }
}
