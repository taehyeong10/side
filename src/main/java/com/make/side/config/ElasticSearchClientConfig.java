package com.make.side.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchLegacyRestClientConfiguration;

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
                //.withConnectTimeout(Duration.ofSeconds(10))
                //.withSocketTimeout(Duration.ofSeconds(60))
                .build();
    }

//    @Primary
//    @Bean
//    public RestClient elasticsearchRestClient() {
//        String serverUrl = this.elasticSearchClientProperty.getServerUrl();
//        String[] parts = serverUrl.split(":");
//        String host = parts[0];
//        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
//
//        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "http"));
//
//        // Add default headers for ES 8.x compatibility
//        builder.setDefaultHeaders(new org.apache.http.Header[]{
//            new org.apache.http.message.BasicHeader("Content-Type", "application/vnd.elasticsearch+json;compatible-with=8"),
//            new org.apache.http.message.BasicHeader("Accept", "application/vnd.elasticsearch+json;compatible-with=8"),
//                new BasicHeader("X-Elastic-Cleint-Meta", "es=8.15.3,jv=25,t=8.15.3")
//        });
//
//        return builder.build();
//    }


}
