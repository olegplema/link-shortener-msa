package com.plema.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.exception.IdempotencyConflictCode;
import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.DeleteShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.port.out.IdempotencyMetrics;
import com.plema.url_command_service.application.port.out.IdempotencyRecordRepository;
import com.plema.url_command_service.application.service.DeleteShortUrlService;
import com.plema.url_command_service.application.service.IdempotentDeleteShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotentDeleteShortUrlServiceTest {

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private DeleteShortUrlService deleteShortUrlService;

    @Mock
    private IdempotencyMetrics idempotencyMetrics;

    @Captor
    private ArgumentCaptor<String> responseBodyCaptor;

    private IdempotentDeleteShortUrlService idempotentDeleteShortUrlService;

    @BeforeEach
    void setUp() {
        var serializer = new DeleteShortUrlResultSnapshotSerializer(new ObjectMapper().findAndRegisterModules());
        idempotentDeleteShortUrlService = new IdempotentDeleteShortUrlService(
                idempotencyRecordRepository,
                deleteShortUrlService,
                serializer,
                idempotencyMetrics
        );
    }

    @Test
    void should_delete_and_store_idempotency_record() {
        var idempotencyKey = "key-1";
        var id = "abc123";
        var requestHash = "request-hash";

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        )).thenReturn(java.util.Optional.empty());
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(true);

        idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash);

        verify(deleteShortUrlService).deleteShortUrl(id);
        verify(idempotencyRecordRepository).updateResponse(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                responseBodyCaptor.capture(),
                eq(id)
        );
        assertThat(responseBodyCaptor.getValue()).contains("\"id\":\"abc123\"");
    }

    @Test
    void should_cleanup_expired_record_and_attempt_save_when_no_active_record_exists() {
        var idempotencyKey = "key-ttl-1";
        var id = "abc123";
        var requestHash = "request-hash";

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        )).thenReturn(java.util.Optional.empty());
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(true);

        idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash);

        verify(idempotencyRecordRepository).findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        );
        verify(idempotencyRecordRepository).deleteExpiredForKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        );
        verify(idempotencyRecordRepository).trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                eq(requestHash)
        );
    }

    @Test
    void should_skip_delete_when_existing_record_matches_hash() {
        var idempotencyKey = "key-2";
        var id = "abc123";
        var requestHash = "request-hash";
        var snapshot = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.DELETE_SHORT_URL,
                requestHash,
                "{\"id\":\"abc123\"}",
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.of(snapshot));

        idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash);

        verifyNoInteractions(deleteShortUrlService);
        verify(idempotencyMetrics).incrementReplay(IdempotencyOperation.DELETE_SHORT_URL);
    }

    @Test
    void should_throw_conflict_when_same_key_is_used_with_different_payload() {
        var idempotencyKey = "key-3";
        var id = "abc123";
        var requestHash = "request-hash";
        var existingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.DELETE_SHORT_URL,
                "different-hash",
                "{\"id\":\"abc123\"}",
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.of(existingRecord));

        assertThatThrownBy(() -> idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash))
                .isInstanceOfSatisfying(IdempotencyConflictException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdempotencyConflictCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
                    assertThat(exception.getRetryAfterMs()).isNull();
                });

        verifyNoInteractions(deleteShortUrlService);
        verify(idempotencyMetrics).incrementConflict(
                IdempotencyOperation.DELETE_SHORT_URL,
                IdempotencyConflictCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH.name()
        );
    }

    @Test
    void should_skip_delete_when_save_loses_race_to_completed_request() {
        var idempotencyKey = "key-4";
        var id = "abc123";
        var requestHash = "request-hash";
        var existingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.DELETE_SHORT_URL,
                requestHash,
                "{\"id\":\"abc123\"}",
                "abc123"
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(existingRecord));
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(false);

        idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash);

        verify(deleteShortUrlService, never()).deleteShortUrl(id);
        verify(idempotencyMetrics).incrementReplay(IdempotencyOperation.DELETE_SHORT_URL);
    }

    @Test
    void should_throw_processing_conflict_when_delete_request_is_still_processing() {
        var idempotencyKey = "key-5";
        var id = "abc123";
        var requestHash = "request-hash";
        var processingRecord = new IdempotencyRecordSnapshot(
                idempotencyKey,
                IdempotencyOperation.DELETE_SHORT_URL,
                requestHash,
                null,
                null
        );

        when(idempotencyRecordRepository.findActiveByOperationAndKey(
                IdempotencyOperation.DELETE_SHORT_URL,
                idempotencyKey
        ))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(processingRecord));
        when(idempotencyRecordRepository.trySave(
                eq(idempotencyKey),
                eq(IdempotencyOperation.DELETE_SHORT_URL),
                eq(requestHash)
        )).thenReturn(false);

        assertThatThrownBy(() -> idempotentDeleteShortUrlService.deleteShortUrl(idempotencyKey, id, requestHash))
                .isInstanceOfSatisfying(IdempotencyConflictException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(IdempotencyConflictCode.REQUEST_IN_PROGRESS);
                    assertThat(exception.getRetryAfterMs()).isEqualTo(100L);
                });

        verify(deleteShortUrlService, never()).deleteShortUrl(id);
        verify(idempotencyMetrics).incrementConflict(
                IdempotencyOperation.DELETE_SHORT_URL,
                IdempotencyConflictCode.REQUEST_IN_PROGRESS.name()
        );
    }
}
