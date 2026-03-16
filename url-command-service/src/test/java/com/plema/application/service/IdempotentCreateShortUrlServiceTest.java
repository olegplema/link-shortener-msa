package com.plema.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.exception.IdempotencyConflictCode;
import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.idempotency.CreateShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.model.CreateShortUrlResult;
import com.plema.url_command_service.application.port.out.IdempotencyRecordRepository;
import com.plema.url_command_service.application.service.CreateShortUrlService;
import com.plema.url_command_service.application.service.IdempotentCreateShortUrlService;
import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotentCreateShortUrlServiceTest {

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private CreateShortUrlService createShortUrlService;

    @Captor
    private ArgumentCaptor<String> responseBodyCaptor;

    private IdempotentCreateShortUrlService idempotentCreateShortUrlService;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        var serializer = new CreateShortUrlResultSnapshotSerializer(new ObjectMapper().findAndRegisterModules());
        idempotentCreateShortUrlService = new IdempotentCreateShortUrlService(
                idempotencyRecordRepository,
                createShortUrlService,
                serializer
        );
    }

    @Test
    void should_create_short_url_and_store_completed_idempotency_record() {
        var idempotencyKey = "key-1";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var aggregate = ShortUrlAggregate.create("abc123", originalUrl, now);

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.empty());
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(true);
        when(createShortUrlService.createShortUrl(originalUrl)).thenReturn(aggregate);

        var response = idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash);

        assertThat(response.id()).isEqualTo("abc123");
        assertThat(response.originalUrl()).isEqualTo(originalUrl);
        assertThat(response.expiration()).isEqualTo(now.plusDays(7));

        verify(createShortUrlService).createShortUrl(originalUrl);
        verify(idempotencyRecordRepository).updateResponse(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                responseBodyCaptor.capture(),
                eq("abc123")
        );
        assertThat(responseBodyCaptor.getValue()).contains("\"id\":\"abc123\"");
    }

    @Test
    void should_cleanup_expired_record_and_attempt_save_when_no_active_record_exists() {
        var idempotencyKey = "key-ttl-1";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var aggregate = ShortUrlAggregate.create("abc123", originalUrl, now);

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        )).thenReturn(java.util.Optional.empty());
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(true);
        when(createShortUrlService.createShortUrl(originalUrl)).thenReturn(aggregate);

        idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash);

        verify(idempotencyRecordRepository).findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        );
        verify(idempotencyRecordRepository).deleteExpiredForKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        );
        verify(idempotencyRecordRepository).trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                eq(requestHash)
        );
    }

    @Test
    void should_replay_response_when_existing_completed_record_matches_hash() throws Exception {
        var idempotencyKey = "key-2";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var responseBody = new ObjectMapper().findAndRegisterModules().writeValueAsString(
                new CreateShortUrlResult("abc123", "https://example.com", now.plusDays(7))
        );
        var snapshot = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.CREATE_SHORT_URL,
                requestHash,
                responseBody,
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.of(snapshot));

        var response = idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash);

        assertThat(response.id()).isEqualTo("abc123");
        assertThat(response.originalUrl()).isEqualTo(originalUrl);
        assertThat(response.expiration()).isEqualTo(now.plusDays(7));
        verifyNoInteractions(createShortUrlService);
    }

    @Test
    void should_throw_conflict_when_same_key_is_used_with_different_payload() {
        var idempotencyKey = "key-3";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var existingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.CREATE_SHORT_URL,
                "different-hash",
                "{\"id\":\"abc123\"}",
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.of(existingRecord));

        assertThatThrownBy(() -> idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash))
                .isInstanceOfSatisfying(IdempotencyConflictException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdempotencyConflictCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
                    assertThat(exception.getRetryAfterMs()).isNull();
                });

        verifyNoInteractions(createShortUrlService);
    }

    @Test
    void should_replay_response_when_save_loses_race_to_completed_request() throws Exception {
        var idempotencyKey = "key-4";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var responseBody = new ObjectMapper().findAndRegisterModules().writeValueAsString(
                new CreateShortUrlResult("abc123", originalUrl, now.plusDays(7))
        );
        var existingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.CREATE_SHORT_URL,
                requestHash,
                responseBody,
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(existingRecord));
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(false);

        var response = idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash);

        assertThat(response.id()).isEqualTo("abc123");
        assertThat(response.originalUrl()).isEqualTo(originalUrl);
        verify(createShortUrlService, never()).createShortUrl(any());
    }

    @Test
    void should_throw_processing_conflict_when_request_is_still_processing() {
        var idempotencyKey = "key-5";
        var originalUrl = "https://example.com";
        var requestHash = "request-hash";
        var processingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.CREATE_SHORT_URL,
                requestHash,
                null,
                null
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.CREATE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(processingRecord));
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.CREATE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(false);

        assertThatThrownBy(() -> idempotentCreateShortUrlService.createShortUrl(idempotencyKey, originalUrl, requestHash))
                .isInstanceOfSatisfying(IdempotencyConflictException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdempotencyConflictCode.REQUEST_IN_PROGRESS);
                    assertThat(exception.getRetryAfterMs()).isEqualTo(100L);
                });

        verify(createShortUrlService, never()).createShortUrl(any());
    }
}
