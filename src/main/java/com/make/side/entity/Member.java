package com.make.side.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Member {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Instant time;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }
}
