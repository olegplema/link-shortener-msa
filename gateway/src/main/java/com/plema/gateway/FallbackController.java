package com.plema.gateway;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class FallbackController {

    private static final String SERVICE_NAME_PATTERN = "^[A-Za-z0-9-]+$";
    private final GatewayMetrics gatewayMetrics;

    FallbackController(GatewayMetrics gatewayMetrics) {
        this.gatewayMetrics = gatewayMetrics;
    }

    @RequestMapping(path = "/fallback/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> fallback(
            @PathVariable
            @NotBlank(message = "service must not be blank")
            @Pattern(regexp = SERVICE_NAME_PATTERN, message = "service contains unsupported characters")
            String service
    ) {
        gatewayMetrics.incrementFallback(service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "error", "Service Unavailable",
                        "service", service
                ));
    }
}
