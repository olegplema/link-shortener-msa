package com.plema.domain.vo;

import com.plema.url_command_service.domain.exception.InvalidShortUrlIdException;
import com.plema.url_command_service.domain.vo.ShortUrlId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortUrlIdTest {

    @Test
    void should_create_short_url_id_when_format_valid() {
        var validId = "abcde1";

        var id = new ShortUrlId(validId);

        assertThat(id.value()).isEqualTo(validId);
    }

    @ParameterizedTest
    @MethodSource("invalidIds")
    void should_throw_when_short_url_id_invalid(String rawId) {
        assertThatThrownBy(() -> new ShortUrlId(rawId))
                .isInstanceOf(InvalidShortUrlIdException.class);
    }

    private static Stream<Arguments> invalidIds() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("abcd"),
                Arguments.of("abc$"),
                Arguments.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        );
    }
}
