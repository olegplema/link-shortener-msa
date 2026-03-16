package com.plema.url_command_service.infrasturcture.persistence.jpa;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.infrasturcture.persistence.entity.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SpringDataIdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, Long> {
    @Query("""
            select record
            from IdempotencyRecordEntity record
            where record.operation = :operation
              and record.idempotencyKey = :idempotencyKey
              and record.expiresAt > CURRENT_TIMESTAMP
            """)
    Optional<IdempotencyRecordEntity> findActiveByOperationAndIdempotencyKey(
            IdempotencyOperation operation,
            String idempotencyKey
    );

    Optional<IdempotencyRecordEntity> findByOperationAndIdempotencyKey(IdempotencyOperation operation, String idempotencyKey);

    @Modifying
    @Query("""
            delete from IdempotencyRecordEntity record
            where record.operation = :operation
              and record.idempotencyKey = :idempotencyKey
              and record.expiresAt < CURRENT_TIMESTAMP
            """)
    void deleteExpiredByOperationAndIdempotencyKey(
            IdempotencyOperation operation,
            String idempotencyKey
    );

    @Modifying
    @Query("delete from IdempotencyRecordEntity record where record.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpired();
}
