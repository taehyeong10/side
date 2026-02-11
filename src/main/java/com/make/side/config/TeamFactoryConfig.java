package com.make.side.config;

import com.make.side.entity.TeamFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeamFactoryConfig {

    @Bean
    public TeamFactory teamFactory() {
        return new TeamFactory();
    }
}
