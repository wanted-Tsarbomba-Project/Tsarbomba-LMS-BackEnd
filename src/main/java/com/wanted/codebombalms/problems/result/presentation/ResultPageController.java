package com.wanted.codebombalms.problems.result.presentation;

import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;
import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase;
import com.wanted.codebombalms.problems.result.presentation.response.ProblemSetResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ResultPageController {

    private final GetProblemSetResultUseCase getProblemSetResultUseCase;

    @GetMapping("/problem/sets/{problemSetId}/result")
    public String resultPage(
            @PathVariable Long problemSetId,
            @RequestParam(defaultValue = "1") Long userId,
            Model model
    ) {
        var query = new GetProblemSetResultQuery(problemSetId, userId);
        var result = new ProblemSetResultResponse(
                getProblemSetResultUseCase.handle(query)
        );

        model.addAttribute("result", result);

        return "problem/result";
    }
}
