package com.plema.domain.vo;

import com.plema.url_command_service.domain.exception.InvalidExpirationException;
import com.plema.url_command_service.domain.vo.Expiration;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpirationTest {

    @Test
    void should_create_expiration_when_in_future() {
        var futureExpiration = OffsetDateTime.now().plusMinutes(5);

        var expiration = new Expiration(futureExpiration);

        assertThat(expiration.value()).isEqualTo(futureExpiration);
    }

    @Test
    void should_allow_null_expiration_when_not_provided() {
        var expiration = new Expiration(null);

        assertThat(expiration.value()).isNull();
    }

    @Test
    void should_throw_when_expiration_in_past() {
        var pastExpiration = OffsetDateTime.now().minusMinutes(5);

        assertThatThrownBy(() -> new Expiration(pastExpiration))
                .isInstanceOf(InvalidExpirationException.class);
    }
}
