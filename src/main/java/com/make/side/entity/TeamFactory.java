package com.make.side.entity;

public class TeamFactory {
    public Team createTeam(String name){
        return new Team(name);
    }
}
