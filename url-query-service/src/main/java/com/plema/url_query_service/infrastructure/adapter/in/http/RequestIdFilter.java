package com.plema.url_query_service.infrastructure.adapter.in.http;

import com.plema.url_query_service.infrastructure.support.RequestIdSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var requestId = resolveRequestId(request);
        RequestIdSupport.bindToMdc(requestId);

        response.setHeader(RequestIdSupport.REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestIdSupport.clearFromMdc();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        return RequestIdSupport.resolveOrGenerate(request.getHeader(RequestIdSupport.REQUEST_ID_HEADER));
    }
}
