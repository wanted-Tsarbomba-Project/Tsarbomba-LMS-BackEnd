package com.wanted.codebombalms.problems.generation.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftAiCommand;
import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftCommand;
import com.wanted.codebombalms.problems.generation.application.command.StoreProblemSetDraftDatasetCommand;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftAiPort;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftDatasetUrlPort;
import com.wanted.codebombalms.problems.generation.application.port.StoreProblemSetDraftDatasetPort;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.application.usecase.GenerateProblemSetDraftUseCase;
import com.wanted.codebombalms.problems.generation.domain.StoredProblemSetDraftDataset;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemSetDraftGenerationService implements GenerateProblemSetDraftUseCase {

    private final GenerateProblemSetDraftAiPort generateProblemSetDraftAiPort;
    private final StoreProblemSetDraftDatasetPort storeProblemSetDraftDatasetPort;
    private final GenerateProblemSetDraftDatasetUrlPort generateProblemSetDraftDatasetUrlPort;

    @Override
    public ProblemSetDraftResult generate(GenerateProblemSetDraftCommand command) {
        validateDatasetFile(command);

        String draftToken = UUID.randomUUID().toString();
        StoredProblemSetDraftDataset storedDataset = storeProblemSetDraftDataset(
                command.operatorId(),
                draftToken,
                command
        );
        String datasetUrl = generateProblemSetDraftDatasetUrlPort.generate(
                storedDataset.objectName()
        );

        GenerateProblemSetDraftAiCommand aiCommand = new GenerateProblemSetDraftAiCommand(
                command.operatorId(),
                command.question(),
                datasetUrl,
                resolveDataFileName(command, storedDataset),
                command.topic(),
                command.categoryName(),
                command.difficulty(),
                command.problemCount(),
                command.subProblemCount()
        );

        return generateProblemSetDraftAiPort.generate(aiCommand);
    }

    private void validateDatasetFile(GenerateProblemSetDraftCommand command) {
        if (command.datasetContent() == null || command.datasetFileSize() <= 0) {
            throw new ValidationException(
                    ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE
            );
        }
    }

    private StoredProblemSetDraftDataset storeProblemSetDraftDataset(
            Long operatorId,
            String draftToken,
            GenerateProblemSetDraftCommand command
    ) {
        return storeProblemSetDraftDatasetPort.store(
                new StoreProblemSetDraftDatasetCommand(
                        operatorId,
                        draftToken,
                        command.originalFileName(),
                        command.contentType(),
                        command.datasetContent(),
                        command.datasetFileSize()
                )
        );
    }

    private String resolveDataFileName(
            GenerateProblemSetDraftCommand command,
            StoredProblemSetDraftDataset storedDataset
    ) {
        if (command.dataFileName() != null && !command.dataFileName().isBlank()) {
            return command.dataFileName();
        }

        return storedDataset.originalFileName();
    }
}
