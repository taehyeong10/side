package com.make.side;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchRestClientAutoConfiguration;

@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
public class SideApplication {

	public static void main(String[] args) {
		SpringApplication.run(SideApplication.class, args);
	}

}
