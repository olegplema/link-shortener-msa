package com.plema.gateway;

public interface GatewayMetrics {
    void incrementFallback(String service);
}
