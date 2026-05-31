package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort;
import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort.ProblemTargetDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemTargetDetailPersistenceAdapter implements ProblemTargetDetailPort {

    private final SpringDataProblemRepository problemRepository;

    @Override
    public Optional<ProblemTargetDetailView> findProblemTargetDetail(Long problemId) {
        return problemRepository.findProblemTargetDetail(problemId);
    }
}
