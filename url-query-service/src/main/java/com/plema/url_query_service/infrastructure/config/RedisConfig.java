package com.plema.url_query_service.infrastructure.config;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ShortUrlReadModel> shortUrlRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, ShortUrlReadModel> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        GenericJacksonJsonRedisSerializer serializer =
                new GenericJacksonJsonRedisSerializer(objectMapper);

        template.setValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
