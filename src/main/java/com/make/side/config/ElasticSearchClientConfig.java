package com.make.side.config;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchLegacyRestClientConfiguration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(ElasticSearchClientProperty.class)
@Configuration
@SuppressWarnings(value = {"removal"})
public class ElasticSearchClientConfig extends ElasticsearchLegacyRestClientConfiguration {
    private final ElasticSearchClientProperty elasticSearchClientProperty;

    public ElasticSearchClientConfig(ElasticSearchClientProperty elasticSearchClientProperty) {
        this.elasticSearchClientProperty = elasticSearchClientProperty;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(this.elasticSearchClientProperty.getServerUrl())
                .usingSsl()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(5))
                .withClientConfigurer((ClientConfiguration.ClientConfigurationCallback<HttpAsyncClientBuilder>) httpClientBuilder -> {
                    try {
                        return httpClientBuilder.setConnectionTimeToLive(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();
    }
}
