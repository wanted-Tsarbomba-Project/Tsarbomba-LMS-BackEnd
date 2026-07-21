package com.wanted.codebombalms.problems.generation.application.port;

import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftAiCommand;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;

public interface GenerateProblemSetDraftAiPort {

    ProblemSetDraftResult generate(GenerateProblemSetDraftAiCommand command);
}
