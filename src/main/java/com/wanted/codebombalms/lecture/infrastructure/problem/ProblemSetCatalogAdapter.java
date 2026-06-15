package com.wanted.codebombalms.lecture.infrastructure.problem;

import com.wanted.codebombalms.lecture.application.port.ProblemSetCatalogPort;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSetCatalogAdapter implements ProblemSetCatalogPort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public boolean existsProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .filter(problemSet -> problemSet.getDeletedAt() == null)
                .isPresent();
    }
}
