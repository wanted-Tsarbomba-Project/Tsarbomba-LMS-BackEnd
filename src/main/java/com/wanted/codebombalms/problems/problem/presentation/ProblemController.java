package com.wanted.codebombalms.problems.problem.presentation;

import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase.ProblemCategoryView;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetListResponse;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProblemController {

    private final GetProblemCategoriesUseCase getProblemCategoriesUseCase;
    private final GetProblemSetsUseCase getProblemSetsUseCase;

    public ProblemController(
            GetProblemCategoriesUseCase getProblemCategoriesUseCase,
            GetProblemSetsUseCase getProblemSetsUseCase
    ) {
        this.getProblemCategoriesUseCase = getProblemCategoriesUseCase;
        this.getProblemSetsUseCase = getProblemSetsUseCase;
    }

    @GetMapping("/problem")
    public String problemList(
            @RequestParam(required = false) Long categoryId,
            Model model
    ) {
        List<ProblemCategoryView> categories = getProblemCategoriesUseCase.handle();

        Long selectedCategoryId = categoryId;

        if (selectedCategoryId == null && !categories.isEmpty()) {
            selectedCategoryId = categories.get(0).categoryId();
        }

        List<ProblemSetListResponse> problemSets = List.of();

        if (selectedCategoryId != null) {
            problemSets = getProblemSetsUseCase.handle(new GetProblemSetsQuery(selectedCategoryId))
                    .stream()
                    .map(ProblemSetListResponse::new)
                    .toList();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("problemSets", problemSets);

        return "problem/list";
    }
}
