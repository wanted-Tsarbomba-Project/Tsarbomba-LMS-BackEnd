package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.infrastructure.persistence.SpringDataProblemCategoryRepository;
import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetCategoryPersistenceAdapter implements CheckProblemSetCategoryPort {

    private final SpringDataProblemCategoryRepository problemCategoryRepository;

    @Override
    public boolean existsActiveCategory(Long categoryId) {
        return problemCategoryRepository.existsByCategoryIdAndStatus(categoryId, "ACTIVE");
    }
}
