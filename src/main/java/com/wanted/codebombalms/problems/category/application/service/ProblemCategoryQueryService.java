package com.wanted.codebombalms.problems.category.application.service;

import com.wanted.codebombalms.problems.category.application.port.LoadProblemCategoryPort;
import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemCategoryQueryService implements GetProblemCategoriesUseCase {

    private final LoadProblemCategoryPort loadProblemCategoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProblemCategoryView> handle() {
        return loadProblemCategoryPort.loadActiveCategories()
                .stream()
                .map(this::toView)
                .toList();
    }

    private ProblemCategoryView toView(ProblemCategory category) {
        return new ProblemCategoryView(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription()
        );
    }
}
