package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.application.idempotency.CreateShortUrlResultSnapshotSerializer;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.model.CreateShortUrlResult;
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
                .orElseThrow(() -> IdempotencyConflictException.requestInProgress(RETRY_AFTER_MS));
    }

    private CreateShortUrlResult replayOrReject(IdempotencyRecordSnapshot existingRecord, String requestHash) {
        if (!existingRecord.requestHash().equals(requestHash)) {
            throw IdempotencyConflictException.payloadMismatch();
        }

        if (existingRecord.responseBody() == null) {
            throw IdempotencyConflictException.requestInProgress(RETRY_AFTER_MS);
        }

        return replayResponse(existingRecord);
    }

    private CreateShortUrlResult replayResponse(IdempotencyRecordSnapshot existingRecord) {
        return createShortUrlResultSnapshotSerializer.deserialize(existingRecord.responseBody());
    }
}
