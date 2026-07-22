package com.wanted.codebombalms.problems.generation.infrastructure.ai;

import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftAiCommand;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftAiPort;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.infrastructure.ai.request.FastApiProblemSetDraftRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FastApiProblemSetDraftAdapter implements GenerateProblemSetDraftAiPort {

    private final ProblemSetDraftAiClient problemSetDraftAiClient;

    @Override
    public ProblemSetDraftResult generate(GenerateProblemSetDraftAiCommand command) {
        return problemSetDraftAiClient.generate(
                FastApiProblemSetDraftRequest.from(command)
        ).toResult();
    }
}
