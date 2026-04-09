package com.plema.url_query_service.infrastructure.support;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdSupportTest {

    @Test
    void should_generate_request_id_for_blank_request_id() {
        var resolved = RequestIdSupport.resolveOrGenerate(" ");

        assertThat(resolved).isNotBlank();
        assertThat(resolved).matches("^[A-Za-z0-9._:-]{1,128}$");
    }

    @Test
    void should_generate_request_id_for_invalid_request_id() {
        var resolved = RequestIdSupport.resolveOrGenerate("bad id with spaces");

        assertThat(resolved).isNotBlank();
        assertThat(resolved).matches("^[A-Za-z0-9._:-]{1,128}$");
    }

    @Test
    void should_bind_valid_request_id_to_mdc() {
        RequestIdSupport.bindToMdc("req-123");

        assertThat(MDC.get(RequestIdSupport.REQUEST_ID_MDC_KEY)).isEqualTo("req-123");

        RequestIdSupport.clearFromMdc();
    }

    @Test
    void should_ignore_invalid_request_id_when_binding_to_mdc() {
        RequestIdSupport.bindToMdc("bad id with spaces");

        assertThat(MDC.get(RequestIdSupport.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void should_keep_request_id_when_valid() {
        assertThat(RequestIdSupport.resolveOrGenerate("req-123")).isEqualTo("req-123");
    }
}
