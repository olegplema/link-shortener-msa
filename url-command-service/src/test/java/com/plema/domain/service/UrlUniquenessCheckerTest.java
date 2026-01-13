package com.plema.domain.service;

import com.plema.domain.exception.UrlIdExistsException;
import com.plema.domain.vo.ShortUrlId;
import com.plema.testsupport.StubShortUrlRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlUniquenessCheckerTest {

    @Test
    void should_throw_when_short_url_id_already_exists() {
        var repository = new StubShortUrlRepository(true);
        var checker = new UrlUniquenessChecker(repository);

        assertThatThrownBy(() -> checker.ensureUnique(new ShortUrlId("abcde1")))
                .isInstanceOf(UrlIdExistsException.class);
    }

    @Test
    void should_not_throw_when_short_url_id_unique() {
        var repository = new StubShortUrlRepository(false);
        var checker = new UrlUniquenessChecker(repository);

        checker.ensureUnique(new ShortUrlId("abcde1"));
    }
}
