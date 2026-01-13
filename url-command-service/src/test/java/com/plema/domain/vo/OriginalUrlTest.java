package com.plema.domain.vo;

import com.plema.domain.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OriginalUrlTest {

    @Test
    void should_create_original_url_when_value_starts_with_http() {
        var validUrl = "http://example.com";

        var url = new OriginalUrl(validUrl);

        assertThat(url.value()).isEqualTo(validUrl);
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void should_throw_when_url_invalid(String rawUrl) {
        assertThatThrownBy(() -> new OriginalUrl(rawUrl))
                .isInstanceOf(InvalidUrlException.class);
    }

    private static Stream<Arguments> invalidUrls() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of("ftp://example.com"),
                Arguments.of("www.example.com")
        );
    }
}
