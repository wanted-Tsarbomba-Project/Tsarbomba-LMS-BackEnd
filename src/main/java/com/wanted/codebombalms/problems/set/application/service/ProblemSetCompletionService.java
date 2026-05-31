package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.port.IncreaseProblemSetCompletedCountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemSetCompletionService {

    private final IncreaseProblemSetCompletedCountPort increaseProblemSetCompletedCountPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long problemSetId) {
        increaseProblemSetCompletedCountPort.increaseCompletedUserCount(problemSetId);
    }
}
