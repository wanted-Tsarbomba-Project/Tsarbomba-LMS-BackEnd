package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort.ProcessResult;
import com.wanted.codebombalms.reward.point.application.usecase.GrantProblemPointUseCase;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PointRewardTaskServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-30T00:00:00Z");

    @Mock
    private PointRewardTaskRepository pointRewardTaskRepository;

    @Mock
    private GrantProblemPointUseCase grantProblemPointUseCase;

    @Mock
    private RecordRewardMetricsPort rewardMetrics;

    private PointRewardTaskService service;

    @BeforeEach
    void setUp() {
        service = new PointRewardTaskService(
                pointRewardTaskRepository,
                grantProblemPointUseCase,
                rewardMetrics,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void recordsCompletedResultWhenGrantSucceeds() {
        PointRewardTask task = task(PointRewardTaskStatus.PENDING, 0);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));

        service.process(30L);

        ArgumentCaptor<PointRewardTask> savedTask =
                ArgumentCaptor.forClass(PointRewardTask.class);
        verify(pointRewardTaskRepository).save(savedTask.capture());
        assertThat(savedTask.getValue().status())
                .isEqualTo(PointRewardTaskStatus.COMPLETED);
        verify(rewardMetrics).recordProcessed(ProcessResult.COMPLETED);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.COMPLETED), anyLong());
    }

    @Test
    void recordsRetryResultWhenGrantThrowsTransientError() {
        PointRewardTask task = task(PointRewardTaskStatus.PENDING, 0);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));
        doThrow(new IllegalStateException("temporary DB failure"))
                .when(grantProblemPointUseCase)
                .grant(10L, 20L, 30L, 100);

        service.process(30L);

        ArgumentCaptor<PointRewardTask> savedTask =
                ArgumentCaptor.forClass(PointRewardTask.class);
        verify(pointRewardTaskRepository).save(savedTask.capture());
        assertThat(savedTask.getValue().status())
                .isEqualTo(PointRewardTaskStatus.PENDING);
        assertThat(savedTask.getValue().retryCount()).isEqualTo(1);
        verify(rewardMetrics).recordProcessed(ProcessResult.RETRY);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.RETRY), anyLong());
    }

    @Test
    void recordsFailedResultWhenRetryLimitIsReached() {
        PointRewardTask task = task(PointRewardTaskStatus.PENDING, 4);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));
        doThrow(new IllegalStateException("persistent DB failure"))
                .when(grantProblemPointUseCase)
                .grant(10L, 20L, 30L, 100);

        service.process(30L);

        ArgumentCaptor<PointRewardTask> savedTask =
                ArgumentCaptor.forClass(PointRewardTask.class);
        verify(pointRewardTaskRepository).save(savedTask.capture());
        assertThat(savedTask.getValue().status())
                .isEqualTo(PointRewardTaskStatus.FAILED);
        assertThat(savedTask.getValue().retryCount()).isEqualTo(5);
        verify(rewardMetrics).recordProcessed(ProcessResult.FAILED);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.FAILED), anyLong());
    }

    @Test
    void recordsSkippedResultForAlreadyProcessedTask() {
        PointRewardTask task = task(PointRewardTaskStatus.COMPLETED, 0);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));

        service.process(30L);

        verify(grantProblemPointUseCase, never())
                .grant(10L, 20L, 30L, 100);
        verify(pointRewardTaskRepository, never()).save(any(PointRewardTask.class));
        verify(rewardMetrics).recordProcessed(ProcessResult.SKIPPED);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.SKIPPED), anyLong());
    }

    @Test
    void defersCompletedMetricsUntilTransactionCommits() {
        PointRewardTask task = task(PointRewardTaskStatus.PENDING, 0);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));

        initTransactionSynchronization();
        try {
            service.process(30L);

            verifyNoInteractions(rewardMetrics);

            completeTransaction(TransactionSynchronization.STATUS_COMMITTED);
        } finally {
            clearTransactionSynchronization();
        }

        verify(rewardMetrics).recordProcessed(ProcessResult.COMPLETED);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.COMPLETED), anyLong());
    }

    @Test
    void recordsErrorMetricsWhenTransactionRollsBackAfterCompletedResult() {
        PointRewardTask task = task(PointRewardTaskStatus.PENDING, 0);
        given(pointRewardTaskRepository.findBySubmissionIdForUpdate(30L))
                .willReturn(Optional.of(task));

        initTransactionSynchronization();
        try {
            service.process(30L);

            verifyNoInteractions(rewardMetrics);

            completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);
        } finally {
            clearTransactionSynchronization();
        }

        verify(rewardMetrics).recordProcessed(ProcessResult.ERROR);
        verify(rewardMetrics)
                .recordProcess(eq(ProcessResult.ERROR), anyLong());
    }

    private PointRewardTask task(
            PointRewardTaskStatus status,
            int retryCount
    ) {
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        return new PointRewardTask(
                1L,
                10L,
                20L,
                30L,
                100,
                status,
                retryCount,
                null,
                now,
                now,
                now
        );
    }

    private void initTransactionSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
    }

    private void completeTransaction(int status) {
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();

        assertThat(synchronizations).hasSize(1);

        synchronizations.forEach(
                synchronization -> synchronization.afterCompletion(status)
        );
    }

    private void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
