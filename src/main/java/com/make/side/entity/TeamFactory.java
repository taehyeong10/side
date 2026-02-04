package com.make.side.entity;

public class TeamFactory {
    Team createTeam(String name){
        return new Team(name);
    }
}
