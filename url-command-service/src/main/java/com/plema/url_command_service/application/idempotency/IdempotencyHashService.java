package com.plema.url_command_service.application.idempotency;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class IdempotencyHashService {

    public String hash(IdempotencyOperation operation, String fingerprint) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var source = operation.name() + "|" + fingerprint;
            return HexFormat.of().formatHex(digest.digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
