package com.wanted.codebombalms.problems.set.infrastructure.event;

import com.wanted.codebombalms.problems.set.application.service.ProblemSetCompletionService;
import com.wanted.codebombalms.submission.domain.event.ProblemSetCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemSetCompletedEventListener {

    private final ProblemSetCompletionService problemSetCompletionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProblemSetCompletedEvent event) {
        problemSetCompletionService.complete(event.problemSetId());

        log.info(
                "[ProblemSetCompletedEventListener] completed count increased - userId: {}, problemSetId: {}",
                event.userId(),
                event.problemSetId()
        );
    }
}
