package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.DeleteShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.model.DeleteShortUrlResult;
import com.plema.url_command_service.application.port.out.IdempotencyMetrics;
import com.plema.url_command_service.application.port.out.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotentDeleteShortUrlService {

    private static final IdempotencyOperation OPERATION = IdempotencyOperation.DELETE_SHORT_URL;
    private static final long RETRY_AFTER_MS = 100L;

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final DeleteShortUrlService deleteShortUrlService;
    private final DeleteShortUrlResultSnapshotSerializer deleteShortUrlResultSnapshotSerializer;
    private final IdempotencyMetrics idempotencyMetrics;

    @Transactional
    public void deleteShortUrl(String idempotencyKey, String id, String requestHash) {
        var existingRecord = idempotencyRecordRepository.findActiveByOperationAndKey(
                OPERATION,
                idempotencyKey
        );

        if (existingRecord.isPresent()) {
            replayOrReject(existingRecord.get(), requestHash);
            return;
        }

        idempotencyRecordRepository.deleteExpiredForKey(OPERATION, idempotencyKey);

        var saved = idempotencyRecordRepository.trySave(
                idempotencyKey,
                OPERATION,
                requestHash
        );

        if (!saved) {
            replayOrRejectAfterLostRace(idempotencyKey, requestHash);
            return;
        }

        deleteShortUrlService.deleteShortUrl(id);

        var response = new DeleteShortUrlResult(id);
        idempotencyRecordRepository.updateResponse(
                idempotencyKey,
                OPERATION,
                deleteShortUrlResultSnapshotSerializer.serialize(response),
                response.id()
        );
    }

    private void replayOrReject(IdempotencyRecordSnapshot existingRecord, String requestHash) {
        if (!existingRecord.requestHash().equals(requestHash)) {
            throw payloadMismatchConflict();
        }

        if (existingRecord.responseBody() == null) {
            throw requestInProgressConflict();
        }

        idempotencyMetrics.incrementReplay(OPERATION);
    }

    private void replayOrRejectAfterLostRace(String idempotencyKey, String requestHash) {
        var existingRecord = idempotencyRecordRepository.findActiveByOperationAndKey(OPERATION, idempotencyKey)
                .orElseThrow(this::requestInProgressConflict);

        replayOrReject(existingRecord, requestHash);
    }

    private IdempotencyConflictException payloadMismatchConflict() {
        idempotencyMetrics.incrementConflict(OPERATION, com.plema.url_command_service.application.exception.IdempotencyConflictCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH.name());
        return IdempotencyConflictException.payloadMismatch();
    }

    private IdempotencyConflictException requestInProgressConflict() {
        var exception = IdempotencyConflictException.requestInProgress(RETRY_AFTER_MS);
        idempotencyMetrics.incrementConflict(OPERATION, com.plema.url_command_service.application.exception.IdempotencyConflictCode.REQUEST_IN_PROGRESS.name());
        return exception;
    }
}
