package com.plema.infrasturcture.persistence.adapter;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.infrasturcture.persistence.adapter.IdempotencyRecordRepositoryImpl;
import com.plema.url_command_service.infrasturcture.persistence.jpa.SpringDataIdempotencyRecordRepository;
import com.plema.url_command_service.infrasturcture.persistence.mapper.IdempotencyRecordMapper;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class IdempotencyRecordRepositoryImplTest {

    @Mock
    private SpringDataIdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private IdempotencyRecordMapper idempotencyRecordMapper;

    @Test
    void should_return_false_when_duplicate_idempotency_constraint_is_violated() {
        var repository = new IdempotencyRecordRepositoryImpl(idempotencyRecordRepository, idempotencyRecordMapper);
        var sqlException = new SQLException(
                "duplicate key value violates unique constraint \"uk_idempotency_operation_key\"",
                "23505"
        );
        var cause = new ConstraintViolationException(
                "duplicate key value violates unique constraint",
                sqlException,
                "insert into idempotency_records",
                "uk_idempotency_operation_key"
        );

        doThrow(new DataIntegrityViolationException("Duplicate idempotency key.", cause))
                .when(idempotencyRecordRepository)
                .saveAndFlush(org.mockito.ArgumentMatchers.any());

        var saved = repository.trySave(
                "key-1",
                IdempotencyOperation.CREATE_SHORT_URL,
                "hash-1"
        );

        assertThat(saved).isFalse();
    }

    @Test
    void should_rethrow_when_different_constraint_is_violated() {
        var repository = new IdempotencyRecordRepositoryImpl(idempotencyRecordRepository, idempotencyRecordMapper);
        var sqlException = new SQLException(
                "duplicate key value violates unique constraint \"uk_other_constraint\"",
                "23505"
        );
        var cause = new ConstraintViolationException(
                "duplicate key value violates unique constraint",
                sqlException,
                "insert into idempotency_records",
                "uk_other_constraint"
        );

        doThrow(new DataIntegrityViolationException("Unexpected integrity violation.", cause))
                .when(idempotencyRecordRepository)
                .saveAndFlush(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> repository.trySave(
                "key-1",
                IdempotencyOperation.CREATE_SHORT_URL,
                "hash-1"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_rethrow_when_integrity_violation_is_not_duplicate_key_case() {
        var repository = new IdempotencyRecordRepositoryImpl(idempotencyRecordRepository, idempotencyRecordMapper);

        doThrow(new DataIntegrityViolationException("Unexpected integrity violation."))
                .when(idempotencyRecordRepository)
                .saveAndFlush(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> repository.trySave(
                "key-1",
                IdempotencyOperation.CREATE_SHORT_URL,
                "hash-1"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_delete_expired_records_by_key() {
        var repository = new IdempotencyRecordRepositoryImpl(idempotencyRecordRepository, idempotencyRecordMapper);

        repository.deleteExpiredForKey(IdempotencyOperation.CREATE_SHORT_URL, "key-1");

        org.mockito.Mockito.verify(idempotencyRecordRepository)
                .deleteExpiredByOperationAndIdempotencyKey(IdempotencyOperation.CREATE_SHORT_URL, "key-1");
    }

}
