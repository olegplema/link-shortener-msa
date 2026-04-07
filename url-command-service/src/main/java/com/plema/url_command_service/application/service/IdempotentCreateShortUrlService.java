package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.CreateShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.model.CreateShortUrlResult;
import com.plema.url_command_service.application.port.out.IdempotencyMetrics;
import com.plema.url_command_service.application.port.out.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class IdempotentCreateShortUrlService {

    private static final IdempotencyOperation OPERATION = IdempotencyOperation.CREATE_SHORT_URL;
    private static final long RETRY_AFTER_MS = 100L;

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final CreateShortUrlService createShortUrlService;
    private final CreateShortUrlResultSnapshotSerializer createShortUrlResultSnapshotSerializer;
    private final IdempotencyMetrics idempotencyMetrics;
    @Transactional
    public CreateShortUrlResult createShortUrl(String idempotencyKey, String originalUrl, String requestHash) {
        var existingRecord = idempotencyRecordRepository.findActiveByOperationAndKey(
                OPERATION,
                idempotencyKey
        );

        if (existingRecord.isPresent()) {
            return replayOrReject(existingRecord.get(), requestHash);
        }

        idempotencyRecordRepository.deleteExpiredForKey(OPERATION, idempotencyKey);

        return createOrResolveRace(idempotencyKey, originalUrl, requestHash);
    }

    private CreateShortUrlResult createOrResolveRace(
            String idempotencyKey,
            String originalUrl,
            String requestHash
    ) {
        var saved = idempotencyRecordRepository.trySave(
                idempotencyKey,
                OPERATION,
                requestHash
        );

        if (!saved) {
            return resolveAfterLostRace(idempotencyKey, requestHash);
        }

        var aggregate = createShortUrlService.createShortUrl(originalUrl);
        var response = new CreateShortUrlResult(
                aggregate.getId().value(),
                aggregate.getOriginalUrl().value(),
                aggregate.getExpiration().value()
        );

        idempotencyRecordRepository.updateResponse(
                idempotencyKey,
                OPERATION,
                createShortUrlResultSnapshotSerializer.serialize(response),
                response.id()
        );

        return response;
    }

    private CreateShortUrlResult resolveAfterLostRace(String idempotencyKey, String requestHash) {
        return idempotencyRecordRepository.findActiveByOperationAndKey(OPERATION, idempotencyKey)
                .map(existingRecord -> replayOrReject(existingRecord, requestHash))
                .orElseThrow(this::requestInProgressConflict);
    }

    private CreateShortUrlResult replayOrReject(IdempotencyRecordSnapshot existingRecord, String requestHash) {
        if (!existingRecord.requestHash().equals(requestHash)) {
            throw payloadMismatchConflict();
        }

        if (existingRecord.responseBody() == null) {
            throw requestInProgressConflict();
        }

        return replayResponse(existingRecord);
    }

    private CreateShortUrlResult replayResponse(IdempotencyRecordSnapshot existingRecord) {
        idempotencyMetrics.incrementReplay(OPERATION);
        return createShortUrlResultSnapshotSerializer.deserialize(existingRecord.responseBody());
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
