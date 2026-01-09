package com.make.side.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ElasticSearchClientProperty {

    @Value(value = "spring.elasticsearch.uris")
    private String serverUrl;
    private String username;
    private String password;

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
