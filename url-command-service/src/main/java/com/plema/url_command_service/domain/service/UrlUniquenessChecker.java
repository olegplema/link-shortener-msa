package com.plema.url_command_service.domain.service;

import com.plema.url_command_service.domain.exception.UrlIdExistsException;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.vo.ShortUrlId;

public class UrlUniquenessChecker {
    private final ShortUrlRepository shortUrlRepository;

    public UrlUniquenessChecker(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    public void ensureUnique(ShortUrlId id) {
        if (shortUrlRepository.existsById(id)) {
            throw new UrlIdExistsException("Short URL with id " + id + " already exists.");
        }
    }
}
