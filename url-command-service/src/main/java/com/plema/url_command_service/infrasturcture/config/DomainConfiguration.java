package com.plema.url_command_service.infrasturcture.config;

import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.service.UrlUniquenessChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public UrlUniquenessChecker urlUniquenessChecker(ShortUrlRepository shortUrlRepository) {
        return new UrlUniquenessChecker(shortUrlRepository);
    }
}
