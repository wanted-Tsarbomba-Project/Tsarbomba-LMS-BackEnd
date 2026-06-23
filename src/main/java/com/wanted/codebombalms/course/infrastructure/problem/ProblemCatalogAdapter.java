package com.wanted.codebombalms.course.infrastructure.problem;

import com.wanted.codebombalms.course.application.port.ProblemCatalogPort;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemCatalogAdapter implements ProblemCatalogPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final EntityManager entityManager;

    @Override
    public boolean existsProblemSet(Long problemSetId) {
        return problemSetRepository.existsById(problemSetId);
    }

    @Override
    public boolean existsProblem(Long problemId) {
        return entityManager.createQuery("""
                        select count(p)
                        from ProblemJpaEntity p
                        where p.problemId = :problemId
                        """, Long.class)
                .setParameter("problemId", problemId)
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsProblemInSet(Long problemSetId, Long problemId) {
        return entityManager.createQuery("""
                        select count(p)
                        from ProblemJpaEntity p
                        where p.problemId = :problemId
                          and p.problemSet.problemSetId = :problemSetId
                        """, Long.class)
                .setParameter("problemId", problemId)
                .setParameter("problemSetId", problemSetId)
                .getSingleResult() > 0;
    }
}
