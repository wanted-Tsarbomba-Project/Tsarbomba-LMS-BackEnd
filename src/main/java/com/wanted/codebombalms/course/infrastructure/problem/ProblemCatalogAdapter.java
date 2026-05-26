package com.wanted.codebombalms.course.infrastructure.problem;

import com.wanted.codebombalms.course.application.port.ProblemCatalogPort;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemCatalogAdapter implements ProblemCatalogPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemRepository problemRepository;

    @Override
    public boolean existsProblemSet(Long problemSetId) {
        return problemSetRepository.existsById(problemSetId);
    }

    @Override
    public boolean existsProblemInSet(Long problemSetId, Long problemId) {
        return problemRepository.findByProblemIdAndProblemSet_ProblemSetId(problemId, problemSetId)
                .isPresent();
    }
}
