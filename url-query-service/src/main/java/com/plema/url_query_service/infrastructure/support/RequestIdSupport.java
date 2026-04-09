package com.plema.url_query_service.infrastructure.support;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class RequestIdSupport {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final int MAX_REQUEST_ID_LENGTH = 128;
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");

    private RequestIdSupport() {
    }

    private static Optional<String> sanitize(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return Optional.empty();
        }

        if (requestId.length() > MAX_REQUEST_ID_LENGTH || !REQUEST_ID_PATTERN.matcher(requestId).matches()) {
            return Optional.empty();
        }

        return Optional.of(requestId);
    }

    public static String resolveOrGenerate(String requestId) {
        return sanitize(requestId).orElseGet(() -> UUID.randomUUID().toString());
    }

    public static void bindToMdc(String requestId) {
        sanitize(requestId).ifPresent(value -> MDC.put(REQUEST_ID_MDC_KEY, value));
    }

    public static void clearFromMdc() {
        MDC.remove(REQUEST_ID_MDC_KEY);
    }
}
