package com.plema.gateway;

import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RequestIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final int MAX_REQUEST_ID_LENGTH = 128;
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");
    private static final Logger log = LoggerFactory.getLogger(RequestIdGatewayFilter.class);

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var requestId = resolveRequestId(exchange.getRequest().getHeaders());
        var mutatedRequest = mutateRequest(exchange.getRequest(), requestId);
        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);
            return Mono.empty();
        });

        log.info("request started requestId={} method={} path={}", requestId, mutatedRequest.getMethod(), mutatedRequest.getURI().getPath());

        return chain.filter(mutatedExchange)
                .doOnSuccess(ignored -> log.info(
                        "request completed requestId={} method={} path={} status={}",
                        requestId,
                        mutatedRequest.getMethod(),
                        mutatedRequest.getURI().getPath(),
                        mutatedExchange.getResponse().getStatusCode()
                ))
                .doOnError(error -> log.error(
                        "request failed requestId={} method={} path={}",
                        requestId,
                        mutatedRequest.getMethod(),
                        mutatedRequest.getURI().getPath(),
                        error
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveRequestId(HttpHeaders headers) {
        var requestId = headers.getFirst(REQUEST_ID_HEADER);
        return isValidRequestId(requestId) ? requestId : UUID.randomUUID().toString();
    }

    private ServerHttpRequest mutateRequest(ServerHttpRequest request, String requestId) {
        return request.mutate()
                .headers(headers -> headers.set(REQUEST_ID_HEADER, requestId))
                .build();
    }

    private boolean isValidRequestId(String requestId) {
        return StringUtils.hasText(requestId)
                && requestId.length() <= MAX_REQUEST_ID_LENGTH
                && REQUEST_ID_PATTERN.matcher(requestId).matches();
    }
}
