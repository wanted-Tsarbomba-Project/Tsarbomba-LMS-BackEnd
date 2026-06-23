package com.wanted.codebombalms.chatbot.infrastructure.adapter;

import com.wanted.codebombalms.chatbot.application.port.ProblemTitlePort;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import com.wanted.codebombalms.problems.set.application.service.ProblemSetQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemTitleAdapter implements ProblemTitlePort {

    private final ProblemSetQueryService problemSetQueryService;
    private final ProblemQueryService problemQueryService;

    @Override
    public String findProblemSetTitleOrNull(Long problemSetId) {
        if (problemSetId == null) return null;
        try {
            return problemSetQueryService.findProblemSetDetail(problemSetId).title();
        } catch (NotFoundException e) {
            return null;
        }
    }

    @Override
    public String findProblemTitleOrNull(Long problemId) {
        if (problemId == null) return null;
        try {
            return problemQueryService.findProblem(problemId).getTitle();
        } catch (NotFoundException e) {
            return null;
        }
    }
}
