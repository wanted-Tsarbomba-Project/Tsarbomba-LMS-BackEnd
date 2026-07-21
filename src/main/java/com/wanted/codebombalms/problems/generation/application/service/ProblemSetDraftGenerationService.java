package com.wanted.codebombalms.problems.generation.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftCommand;
import com.wanted.codebombalms.problems.generation.application.command.StoreProblemSetDraftDatasetCommand;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftAiPort;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftDatasetUrlPort;
import com.wanted.codebombalms.problems.generation.application.port.StoreProblemSetDraftDatasetPort;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.application.usecase.GenerateProblemSetDraftUseCase;
import com.wanted.codebombalms.problems.generation.domain.StoredProblemSetDraftDataset;
import com.wanted.codebombalms.problems.generation.presentation.api.request.ProblemSetDraftGenerateRequest;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemSetDraftGenerationService implements GenerateProblemSetDraftUseCase {

    private final GenerateProblemSetDraftAiPort generateProblemSetDraftAiPort;
    private final StoreProblemSetDraftDatasetPort storeProblemSetDraftDatasetPort;
    private final GenerateProblemSetDraftDatasetUrlPort generateProblemSetDraftDatasetUrlPort;

    @Override
    public ProblemSetDraftResult generate(
            Long operatorId,
            ProblemSetDraftGenerateRequest request,
            MultipartFile datasetFile
    ) {
        validateDatasetFile(datasetFile);

        String draftToken = UUID.randomUUID().toString();
        StoredProblemSetDraftDataset storedDataset = storeProblemSetDraftDataset(
                operatorId,
                draftToken,
                datasetFile
        );
        String datasetUrl = generateProblemSetDraftDatasetUrlPort.generate(
                storedDataset.objectName()
        );

        GenerateProblemSetDraftCommand command = new GenerateProblemSetDraftCommand(
                operatorId,
                request.question(),
                datasetUrl,
                draftToken,
                storedDataset.objectName(),
                resolveDataFileName(request, storedDataset),
                request.topic(),
                request.categoryName(),
                request.difficulty(),
                request.problemCount(),
                request.subProblemCount()
        );

        return generateProblemSetDraftAiPort.generate(command);
    }

    private void validateDatasetFile(MultipartFile datasetFile) {
        if (datasetFile == null || datasetFile.isEmpty()) {
            throw new ValidationException(
                    ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE
            );
        }
    }

    private StoredProblemSetDraftDataset storeProblemSetDraftDataset(
            Long operatorId,
            String draftToken,
            MultipartFile datasetFile
    ) {
        try {
            return storeProblemSetDraftDatasetPort.store(
                    new StoreProblemSetDraftDatasetCommand(
                            operatorId,
                            draftToken,
                            datasetFile.getOriginalFilename(),
                            datasetFile.getContentType(),
                            datasetFile.getBytes(),
                            datasetFile.getSize()
                    )
            );
        } catch (IOException e) {
            throw new ValidationException(
                    ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED,
                    e
            );
        }
    }

    private String resolveDataFileName(
            ProblemSetDraftGenerateRequest request,
            StoredProblemSetDraftDataset storedDataset
    ) {
        if (request.dataFileName() != null && !request.dataFileName().isBlank()) {
            return request.dataFileName();
        }

        return storedDataset.originalFileName();
    }
}
