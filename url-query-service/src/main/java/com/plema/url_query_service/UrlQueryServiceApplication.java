package com.plema.url_query_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class UrlQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlQueryServiceApplication.class, args);
	}

}
