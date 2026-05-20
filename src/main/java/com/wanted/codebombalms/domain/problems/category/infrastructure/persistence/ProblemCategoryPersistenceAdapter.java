package com.wanted.codebombalms.domain.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.domain.problems.category.application.port.out.LoadProblemCategoryPort;
import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.domain.problems.category.repository.ProblemCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemCategoryPersistenceAdapter implements LoadProblemCategoryPort {

    private final ProblemCategoryRepository problemCategoryRepository;

    @Override
    public List<ProblemCategory> loadActiveCategories() {
        return problemCategoryRepository.findByStatusOrderByCategoryIdAsc("ACTIVE")
                .stream()
                .map(ProblemCategoryMapper::toDomain)
                .toList();
    }
}
