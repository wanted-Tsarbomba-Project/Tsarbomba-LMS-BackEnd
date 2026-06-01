package com.wanted.codebombalms.reward.point.infrastructure.event;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.reward.point.application.usecase.GrantProblemPointUseCase;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointRewardEventHandler {

    private final GrantProblemPointUseCase grantProblemPointUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProblemSolvedEvent event) {
        try {
            grantProblemPointUseCase.grant(
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    event.point()
            );
        } catch (DomainException e) {
            log.warn(
                    "포인트획득 실패. errorCode={}, message={}, userId={}, problemId={}, submissionId={}, point={}",
                    e.getErrorCode().getCode(),
                    e.getMessage(),
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    event.point()
            );
        } catch (Exception e) {
            log.error(
                    "예상치 못한 오류. userId={}, problemId={}, submissionId={}, point={}",
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    event.point(),
                    e
            );
        }
    }
}


