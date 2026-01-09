package com.make.side.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ElasticSearchClientProperty {

    @Value(value = "${spring.elasticsearch.uris}")
    private String serverUrl;
    private String username;
    private String password;

    public String getServerUrl() {
        // Remove protocol prefix for connectedTo() which expects "host:port"
        if (serverUrl == null) {
            return "localhost:9200";
        }
        return serverUrl
                .replace("http://", "")
                .replace("https://", "")
                .replace("/", "");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
