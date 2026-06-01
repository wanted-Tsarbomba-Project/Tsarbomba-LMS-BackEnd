package com.wanted.codebombalms.submission.infrastructure.event;

import com.wanted.codebombalms.submission.application.port.ProblemSolvedEventPort;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringProblemSolvedEventAdapter implements ProblemSolvedEventPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishSolved(Long userId, Long problemId, Long submissionId, Integer point) {
        eventPublisher.publishEvent(new ProblemSolvedEvent(
                userId,
                problemId,
                submissionId,
                point
        ));
    }
}
