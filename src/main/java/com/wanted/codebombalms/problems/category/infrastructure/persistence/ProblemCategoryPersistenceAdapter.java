package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.application.port.LoadProblemCategoryPort;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategoryStatus;
import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetCategoryPort;
import com.wanted.codebombalms.problems.set.application.port.FindOrCreateProblemSetCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProblemCategoryPersistenceAdapter implements
        LoadProblemCategoryPort,
        CheckProblemSetCategoryPort,
        FindOrCreateProblemSetCategoryPort {

    private final SpringDataProblemCategoryRepository springDataProblemCategoryRepository;

    @Override
    public List<ProblemCategory> loadActiveCategories() {
        return springDataProblemCategoryRepository.findByStatusOrderByCategoryIdAsc(ProblemCategoryStatus.ACTIVE)
                .stream()
                .map(ProblemCategoryMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveCategory(Long categoryId) {
        return springDataProblemCategoryRepository.existsByCategoryIdAndStatus(
                categoryId,
                ProblemCategoryStatus.ACTIVE
        );
    }
    @Override
    public Long findOrCreateActiveCategoryId(String categoryName) {
        ProblemCategory category = ProblemCategory.create(categoryName);

        return springDataProblemCategoryRepository.findByCategoryNameAndStatus(
                        category.getCategoryName(),
                        category.getStatus()
                )
                .orElseGet(() -> springDataProblemCategoryRepository.save(
                        ProblemCategoryMapper.toEntity(category)
                ))
                .getCategoryId();
    }
}
