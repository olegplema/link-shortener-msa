package com.plema.url_query_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;

import static org.assertj.core.api.Assertions.assertThat;

class UrlQueryServiceApplicationTests {

	@Test
	@DisplayName("application bootstrap annotations should stay enabled")
	void shouldDeclareRequiredBootstrapAnnotations() {
		assertThat(UrlQueryServiceApplication.class)
				.hasAnnotation(SpringBootApplication.class)
				.hasAnnotation(EnableKafkaRetryTopic.class);
	}
}
