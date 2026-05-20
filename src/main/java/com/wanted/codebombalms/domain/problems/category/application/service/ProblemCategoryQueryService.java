package com.wanted.codebombalms.domain.problems.category.application.service;

import com.wanted.codebombalms.domain.problems.category.application.port.out.LoadProblemCategoryPort;
import com.wanted.codebombalms.domain.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;
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
    public List<ProblemCategory> getActiveCategories() {
        return loadProblemCategoryPort.loadActiveCategories();
    }
}
