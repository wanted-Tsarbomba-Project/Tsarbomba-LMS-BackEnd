package com.wanted.codebombalms.problems.progress.presentation;

import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase;
import com.wanted.codebombalms.problems.progress.presentation.response.ProblemProgressResponse;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetEnterResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProgressController {

    private final EnterProblemSetUseCase enterProblemSetUseCase;
    private final GetProblemProgressUseCase getProblemProgressUseCase;

    public ProgressController(
            EnterProblemSetUseCase enterProblemSetUseCase,
            GetProblemProgressUseCase getProblemProgressUseCase
    ) {
        this.enterProblemSetUseCase = enterProblemSetUseCase;
        this.getProblemProgressUseCase = getProblemProgressUseCase;
    }

    @GetMapping("/problem/sets/{problemSetId}")
    public String solvePage(
            @PathVariable Long problemSetId,
            @RequestParam(defaultValue = "1") Long userId,
            Model model
    ) {
        ProblemSetEnterResponse response =
                new ProblemSetEnterResponse(
                        enterProblemSetUseCase.handle(new EnterProblemSetQuery(problemSetId, userId))
                );

        if (Boolean.TRUE.equals(response.isCompleted())) {
            return "redirect:/problem/sets/" + problemSetId + "/result?userId=" + userId;
        }

        ProblemProgressResponse progress =
                new ProblemProgressResponse(
                        getProblemProgressUseCase.handle(
                                new GetProblemProgressQuery(problemSetId, userId)
                        )
                );

        model.addAttribute("problemSet", response);
        model.addAttribute("progress", progress);

        return "problem/solve";
    }
}
