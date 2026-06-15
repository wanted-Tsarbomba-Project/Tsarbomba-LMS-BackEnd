package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemSetAccessPersistenceAdapter implements LoadProblemSetAccessPort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public Optional<ProblemSetAccessData> loadAccessData(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .map(problemSet -> new ProblemSetAccessData(
                        problemSet.getStatus(),
                        problemSet.getDeletedAt()
                ));
    }
}
