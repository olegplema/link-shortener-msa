package com.plema.infrasturcture.config;

import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.service.UrlUniquenessChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public UrlUniquenessChecker urlUniquenessChecker(ShortUrlRepository shortUrlRepository) {
        return new UrlUniquenessChecker(shortUrlRepository);
    }
}
