package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort.ProcessResult;
import com.wanted.codebombalms.reward.point.application.usecase.GrantProblemPointUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.domain.exception.RewardErrorCode;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointRewardTaskService
        implements SchedulePointRewardTaskUseCase, ProcessPointRewardTaskUseCase {

    private static final int MAX_RETRY_COUNT = 5;

    private final PointRewardTaskRepository pointRewardTaskRepository;
    private final GrantProblemPointUseCase grantProblemPointUseCase;
    private final RecordRewardMetricsPort rewardMetrics;
    private final Clock clock;
    private static final long MAX_RETRY_DELAY_MINUTES = 16L;

    @Override
    @Transactional
    public void schedule(
            Long userId,
            Long problemId,
            Long submissionId,
            Integer point
    ) {
        pointRewardTaskRepository.save(PointRewardTask.create(
                userId,
                problemId,
                submissionId,
                point,
                now()
        ));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long submissionId) {
        long startedAtNanos = System.nanoTime();
        ProcessResult result = null;
        try {
            PointRewardTask task = pointRewardTaskRepository
                    .findBySubmissionIdForUpdate(submissionId)
                    .orElseThrow(() -> new NotFoundException(
                            RewardErrorCode.REWARD_POINT_TASK_NOT_FOUND
                    ));

            if (task.status() != PointRewardTaskStatus.PENDING) {
                result = ProcessResult.SKIPPED;
                log.info(
                        "event=reward_point_task_skipped submissionId={} status={} durationMs={}",
                        submissionId,
                        task.status(),
                        elapsedMillis(startedAtNanos)
                );
                return;
            }

            result = processPendingTask(task, startedAtNanos);
        } catch (NotFoundException e) {
            result = ProcessResult.NOT_FOUND;
            log.warn(
                    "event=reward_point_task_not_found submissionId={} durationMs={}",
                    submissionId,
                    elapsedMillis(startedAtNanos)
            );
            throw e;
        } catch (Exception e) {
            result = ProcessResult.ERROR;
            log.error(
                    "event=reward_point_task_processing_failed submissionId={} exceptionType={} durationMs={}",
                    submissionId,
                    e.getClass().getSimpleName(),
                    elapsedMillis(startedAtNanos),
                    e
            );
            throw e;
        } finally {
            if (result != null) {
                recordMetricsAfterTransaction(
                        result,
                        System.nanoTime() - startedAtNanos
                );
            }
        }
    }

    private void recordMetricsAfterTransaction(
            ProcessResult result,
            long elapsedNanos
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            rewardMetrics.recordProcessed(result);
            rewardMetrics.recordProcess(result, elapsedNanos);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        ProcessResult finalResult = status == STATUS_COMMITTED
                                ? result
                                : rollbackResult(result);

                        rewardMetrics.recordProcessed(finalResult);
                        rewardMetrics.recordProcess(finalResult, elapsedNanos);
                    }
                }
        );
    }

    private ProcessResult rollbackResult(ProcessResult result) {
        return switch (result) {
            case NOT_FOUND, ERROR -> result;
            default -> ProcessResult.ERROR;
        };
    }

    private ProcessResult processPendingTask(
            PointRewardTask task,
            long startedAtNanos
    ) {
        try {
            grantProblemPointUseCase.grant(
                    task.userId(),
                    task.problemId(),
                    task.submissionId(),
                    task.point()
            );

            pointRewardTaskRepository.save(task.complete());
            log.info(
                    "event=reward_point_task_completed userId={} problemId={} submissionId={} point={} retryCount={} durationMs={}",
                    task.userId(),
                    task.problemId(),
                    task.submissionId(),
                    task.point(),
                    task.retryCount(),
                    elapsedMillis(startedAtNanos)
            );
            return ProcessResult.COMPLETED;
        } catch (DomainException e) {
            return handleDomainFailure(task, e, startedAtNanos);
        } catch (Exception e) {
            return handleFailure(task, e, startedAtNanos);
        }
    }

    private ProcessResult handleDomainFailure(
            PointRewardTask task,
            DomainException exception,
            long startedAtNanos
    ) {
        if (isAlreadyGranted(exception)) {
            pointRewardTaskRepository.save(task.complete());
            log.info(
                    "event=reward_point_task_completed userId={} problemId={} submissionId={} point={} retryCount={} reason=already_granted durationMs={}",
                    task.userId(),
                    task.problemId(),
                    task.submissionId(),
                    task.point(),
                    task.retryCount(),
                    elapsedMillis(startedAtNanos)
            );
            return ProcessResult.COMPLETED;
        }

        pointRewardTaskRepository.save(
                task.failPermanently(exception.getMessage())
        );
        log.warn(
                "event=reward_point_task_failed userId={} problemId={} submissionId={} retryCount={} reason=domain_failure exceptionType={} durationMs={}",
                task.userId(),
                task.problemId(),
                task.submissionId(),
                task.retryCount(),
                exception.getClass().getSimpleName(),
                elapsedMillis(startedAtNanos)
        );
        return ProcessResult.FAILED;
    }

    private ProcessResult handleFailure(
            PointRewardTask task,
            Exception exception,
            long startedAtNanos
    ) {
        if (isAlreadyGranted(exception)) {
            pointRewardTaskRepository.save(task.complete());
            return ProcessResult.COMPLETED;
        }

        long retryDelayMinutes = Math.min(
                MAX_RETRY_DELAY_MINUTES,
                1L << task.retryCount()
        );

        PointRewardTask updatedTask = task.retry(
                exception.getMessage(),
                now().plusMinutes(retryDelayMinutes),
                MAX_RETRY_COUNT
        );
        pointRewardTaskRepository.save(updatedTask);

        if (updatedTask.status() == PointRewardTaskStatus.FAILED) {
            log.error(
                    "event=reward_point_task_failed userId={} problemId={} submissionId={} retryCount={} reason=max_retry_exceeded exceptionType={} durationMs={}",
                    task.userId(),
                    task.problemId(),
                    task.submissionId(),
                    updatedTask.retryCount(),
                    exception.getClass().getSimpleName(),
                    elapsedMillis(startedAtNanos),
                    exception
            );
            return ProcessResult.FAILED;
        }

        log.warn(
                "event=reward_point_task_retry_scheduled userId={} problemId={} submissionId={} retryCount={} exceptionType={} nextRetryAt={} durationMs={}",
                task.userId(),
                task.problemId(),
                task.submissionId(),
                updatedTask.retryCount(),
                exception.getClass().getSimpleName(),
                updatedTask.nextRetryAt(),
                elapsedMillis(startedAtNanos)
        );
        return ProcessResult.RETRY;
    }

    private boolean isAlreadyGranted(Exception exception) {
        return exception instanceof DomainException domainException
                && domainException.getErrorCode()
                == RewardErrorCode.REWARD_POINT_ALREADY_GRANTED;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private long elapsedMillis(long startedAtNanos) {
        return TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startedAtNanos
        );
    }
}
