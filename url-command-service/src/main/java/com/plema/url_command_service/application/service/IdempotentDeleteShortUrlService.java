package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.DeleteShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.model.DeleteShortUrlResult;
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
            throw IdempotencyConflictException.payloadMismatch();
        }

        if (existingRecord.responseBody() == null) {
            throw IdempotencyConflictException.requestInProgress(RETRY_AFTER_MS);
        }
    }

    private void replayOrRejectAfterLostRace(String idempotencyKey, String requestHash) {
        var existingRecord = idempotencyRecordRepository.findActiveByOperationAndKey(OPERATION, idempotencyKey)
                .orElseThrow(() -> IdempotencyConflictException.requestInProgress(RETRY_AFTER_MS));

        replayOrReject(existingRecord, requestHash);
    }
}
