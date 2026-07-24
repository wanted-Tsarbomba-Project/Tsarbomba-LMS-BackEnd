package com.wanted.codebombalms.problems.category.application.usecase;

import java.util.List;

public interface GetProblemCategoriesUseCase {

    List<ProblemCategoryView> handle();

    record ProblemCategoryView(
            Long categoryId,
            String categoryName,
            String description
    ) {
    }
}


/* comment
*   CQRS 패턴에 의거한 분리
*   Query : 조회
*   Command : DML insert , update , delete
*  */
