package com.make.side.config;

import jakarta.annotation.PostConstruct;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConnectorConfig {

    @Value("${server.port:8080}")
    private int apiPort;

    @Value("${server.health-port:8081}")
    private int healthPort;

    @PostConstruct
    public void validatePorts() {
        if (apiPort == healthPort) {
            throw new IllegalStateException(
                "server.port (%d) and server.health-port (%d) must be different".formatted(apiPort, healthPort)
            );
        }
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> healthCheckConnector() {
        return factory -> factory.addAdditionalTomcatConnectors(createHealthCheckConnector());
    }

    private Connector createHealthCheckConnector() {
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(healthPort);

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        protocol.setMaxThreads(5);
        protocol.setMinSpareThreads(2);
        protocol.setAcceptCount(10);
        protocol.setConnectionTimeout(5000);

        return connector;
    }
}