package com.plema.url_command_service.infrasturcture.observability;

import com.plema.url_command_service.infrasturcture.adapter.in.http.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestIdAccessor {

    public Optional<String> currentRequestId() {
        return Optional.ofNullable(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY));
    }
}
