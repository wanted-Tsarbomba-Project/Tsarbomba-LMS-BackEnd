package com.wanted.codebombalms.submission.infrastructure.event;

import com.wanted.codebombalms.submission.application.port.ProblemSetCompletionEventPort;
import com.wanted.codebombalms.submission.domain.event.ProblemSetCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringProblemSetCompletionEventAdapter implements ProblemSetCompletionEventPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringProblemSetCompletionEventAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishCompleted(Long userId, Long problemSetId) {
        eventPublisher.publishEvent(new ProblemSetCompletedEvent(userId, problemSetId));
    }
}
