package com.make.side.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Member {
    @Id
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

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getTime() {
        return time;
    }

    public Set<Team> getTeams() {
        return teams;
    }
}
