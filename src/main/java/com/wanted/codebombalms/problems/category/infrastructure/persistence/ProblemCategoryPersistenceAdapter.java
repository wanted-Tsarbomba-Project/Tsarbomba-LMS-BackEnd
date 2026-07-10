package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.category.application.port.LoadProblemCategoryPort;
import com.wanted.codebombalms.problems.category.application.port.ManageProblemCategoryPort;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategoryStatus;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetCategoryPort;
import com.wanted.codebombalms.problems.set.application.port.FindActiveProblemSetCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProblemCategoryPersistenceAdapter implements
        LoadProblemCategoryPort,
        CheckProblemSetCategoryPort,
        FindActiveProblemSetCategoryPort,
        ManageProblemCategoryPort {

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
    public Long findActiveCategoryId(String categoryName) {
        String normalizedCategoryName = categoryName == null ? "" : categoryName.trim();

        return springDataProblemCategoryRepository.findByCategoryNameAndStatus(
                        normalizedCategoryName,
                        ProblemCategoryStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.CATEGORY_NOT_FOUND))
                .getCategoryId();
    }

    @Override
    public List<ProblemCategory> loadAllCategories() {
        return springDataProblemCategoryRepository.findAllByOrderByCategoryIdAsc()
                .stream()
                .map(ProblemCategoryMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByCategoryName(String categoryName) {
        return springDataProblemCategoryRepository.existsByCategoryNameAndStatus(
                categoryName,
                ProblemCategoryStatus.ACTIVE
        );
    }

    @Override
    public boolean existsActiveByCategoryNameAndCategoryIdNot(String categoryName, Long categoryId) {
        return springDataProblemCategoryRepository.existsByCategoryNameAndStatusAndCategoryIdNot(
                categoryName,
                ProblemCategoryStatus.ACTIVE,
                categoryId
        );
    }

    @Override
    public ProblemCategory create(String categoryName) {
        ProblemCategory category = ProblemCategory.create(categoryName);
        ProblemCategoryJpaEntity savedCategory =
                springDataProblemCategoryRepository.save(ProblemCategoryMapper.toEntity(category));

        return ProblemCategoryMapper.toDomain(savedCategory);
    }

    @Override
    public ProblemCategory updateName(Long categoryId, String categoryName) {
        ProblemCategoryJpaEntity category = springDataProblemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.CATEGORY_NOT_FOUND));

        category.updateName(categoryName);

        return ProblemCategoryMapper.toDomain(category);
    }

    @Override
    public ProblemCategory deactivate(Long categoryId) {
        ProblemCategoryJpaEntity category = springDataProblemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.CATEGORY_NOT_FOUND));

        category.deactivate();

        return ProblemCategoryMapper.toDomain(category);
    }
}
