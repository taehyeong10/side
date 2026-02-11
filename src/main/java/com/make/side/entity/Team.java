package com.make.side.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Team parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Team> children = new HashSet<>();

    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
        name = "member_team",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();

    // Protected constructor for JPA
    protected Team() {
    }

    // Package-private constructor for TeamFactory
    Team(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Team getParent() {
        return parent;
    }

    public Set<Team> getChildren() {
        return children;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<Member> getMembers() {
        return members;
    }

    // Business logic methods
    public void setParent(Team parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public void markAsLeaf() {
        if (!this.children.isEmpty()) {
            throw new IllegalStateException("Cannot mark team as leaf when it has children");
        }
        this.isLeaf = true;
    }

    public void markAsNonLeaf() {
        if (!this.members.isEmpty()) {
            throw new IllegalStateException("Cannot mark team as non-leaf when it has members");
        }
        this.isLeaf = false;
    }

    public void addChild(Team child) {
        this.children.add(child);
        child.parent = this;
        this.isLeaf = false;  // Parent cannot be leaf
    }

    public void addMember(Member member) {
        if (!this.isLeaf) {
            throw new IllegalStateException("Members can only join leaf teams");
        }
        this.members.add(member);
    }

    public void removeMember(Member member) {
        this.members.remove(member);
    }

    public int getDepth() {
        int depth = 0;
        Team current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }
}
