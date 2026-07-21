package com.wanted.codebombalms.problems.generation.application.usecase;

import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.presentation.api.request.ProblemSetDraftGenerateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface GenerateProblemSetDraftUseCase {

    ProblemSetDraftResult generate(
            Long operatorId,
            ProblemSetDraftGenerateRequest request,
            MultipartFile datasetFile
    );
}
