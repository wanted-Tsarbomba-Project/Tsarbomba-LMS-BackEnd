package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.problems.category.domain.repository.ProblemCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@RequiredArgsConstructor
public class ProblemCategoryPersistenceAdapter implements ProblemCategoryRepository {

    private final SpringDataProblemCategoryRepository springDataProblemCategoryRepository;

    @Override
    public List<ProblemCategory> loadActiveCategories() {
        return springDataProblemCategoryRepository.findByStatusOrderByCategoryIdAsc("ACTIVE")
                .stream()
                .map(ProblemCategoryMapper::toDomain)
                .toList();
    }
}
