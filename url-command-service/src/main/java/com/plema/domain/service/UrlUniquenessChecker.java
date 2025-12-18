package com.plema.domain.service;

import com.plema.domain.exception.UrlIdExistsException;
import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.vo.ShortUrlId;

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
