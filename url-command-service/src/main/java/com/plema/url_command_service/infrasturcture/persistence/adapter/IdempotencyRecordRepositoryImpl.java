package com.plema.url_command_service.infrasturcture.persistence.adapter;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.application.port.out.IdempotencyRecordRepository;
import com.plema.url_command_service.infrasturcture.persistence.entity.IdempotencyRecordEntity;
import com.plema.url_command_service.infrasturcture.persistence.jpa.SpringDataIdempotencyRecordRepository;
import com.plema.url_command_service.infrasturcture.persistence.mapper.IdempotencyRecordMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyRecordRepositoryImpl implements IdempotencyRecordRepository {

    private static final String IDEMPOTENCY_CONSTRAINT = "uk_idempotency_operation_key";
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final SpringDataIdempotencyRecordRepository idempotencyRecordRepository;
    private final IdempotencyRecordMapper idempotencyRecordMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyRecordSnapshot> findActiveByOperationAndKey(
            IdempotencyOperation operation,
            String idempotencyKey
    ) {
        return idempotencyRecordRepository
                .findActiveByOperationAndIdempotencyKey(operation, idempotencyKey)
                .map(idempotencyRecordMapper::toSnapshot);
    }

    @Override
    @Transactional
    public boolean trySave(String idempotencyKey, IdempotencyOperation operation, String requestHash) {
        var entity = new IdempotencyRecordEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setOperation(operation);
        entity.setRequestHash(requestHash);

        try {
            idempotencyRecordRepository.saveAndFlush(entity);
            return true;
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateIdempotencyKeyViolation(ex)) {
                return false;
            }

            throw ex;
        }
    }

    @Override
    @Transactional
    public void deleteExpiredForKey(IdempotencyOperation operation, String idempotencyKey) {
        idempotencyRecordRepository.deleteExpiredByOperationAndIdempotencyKey(operation, idempotencyKey);
    }

    @Override
    @Transactional
    public void updateResponse(
            String idempotencyKey,
            IdempotencyOperation operation,
            String responseBody,
            String resourceId
    ) {
        var entity = idempotencyRecordRepository.findByOperationAndIdempotencyKey(operation, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Idempotency record not found for completion."));

        entity.setResponseBody(responseBody);
        entity.setResourceId(resourceId);

        idempotencyRecordRepository.save(entity);
    }

    private boolean isDuplicateIdempotencyKeyViolation(Throwable throwable) {
        var current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                if (IDEMPOTENCY_CONSTRAINT.equals(constraintViolationException.getConstraintName())) {
                    return true;
                }
            }

            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())
                    && hasIdempotencyConstraintInMessage(sqlException)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private boolean hasIdempotencyConstraintInMessage(SQLException sqlException) {
        return sqlException.getMessage() != null
                && sqlException.getMessage().contains(IDEMPOTENCY_CONSTRAINT);
    }
}
