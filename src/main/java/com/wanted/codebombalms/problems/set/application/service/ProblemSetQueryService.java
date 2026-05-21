package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetCategoryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetPort;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemSetQueryService implements GetProblemSetsUseCase {

    private final CheckProblemSetCategoryPort checkProblemSetCategoryPort;
    private final LoadProblemSetPort loadProblemSetPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProblemSetSummary> handle(GetProblemSetsQuery query) {
        if (!checkProblemSetCategoryPort.existsActiveCategory(query.categoryId())) {
            throw new NotFoundException(ProblemErrorCode.CATEGORY_NOT_FOUND);
        }

        return loadProblemSetPort.loadActiveProblemSetsByCategory(query.categoryId());
    }
}
