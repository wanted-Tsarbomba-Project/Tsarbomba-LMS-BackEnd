package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.reward.point.application.usecase.GrantProblemPointUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.domain.exception.RewardErrorCode;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointRewardTaskService
        implements SchedulePointRewardTaskUseCase, ProcessPointRewardTaskUseCase {

    private static final int MAX_RETRY_COUNT = 5;

    private final PointRewardTaskRepository pointRewardTaskRepository;
    private final GrantProblemPointUseCase grantProblemPointUseCase;
    private final Clock clock;

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
        PointRewardTask task = pointRewardTaskRepository
                .findBySubmissionId(submissionId)
                .orElseThrow(() -> new NotFoundException(
                        RewardErrorCode.REWARD_POINT_TASK_NOT_FOUND
                ));

        if (task.status() != com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus.PENDING) {
            return;
        }

        try {
            grantProblemPointUseCase.grant(
                    task.userId(),
                    task.problemId(),
                    task.submissionId(),
                    task.point()
            );

            pointRewardTaskRepository.save(task.complete());
        } catch (DomainException e) {
            handleFailure(task, e);
        } catch (Exception e) {
            handleFailure(task, e);
        }
    }

    private void handleFailure(PointRewardTask task, Exception exception) {
        if (isAlreadyGranted(exception)) {
            pointRewardTaskRepository.save(task.complete());
            return;
        }

        long retryDelayMinutes = task.retryCount() + 1L;

        pointRewardTaskRepository.save(task.retry(
                exception.getMessage(),
                now().plusMinutes(retryDelayMinutes),
                MAX_RETRY_COUNT
        ));
    }

    private boolean isAlreadyGranted(Exception exception) {
        return exception instanceof DomainException domainException
                && domainException.getErrorCode()
                == RewardErrorCode.REWARD_POINT_ALREADY_GRANTED;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
