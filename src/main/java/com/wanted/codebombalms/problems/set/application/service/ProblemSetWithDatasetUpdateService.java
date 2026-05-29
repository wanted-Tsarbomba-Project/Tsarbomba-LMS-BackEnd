package com.wanted.codebombalms.problems.set.application.service;


import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetWithDatasetUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemSetWithDatasetUpdateService implements UpdateProblemSetWithDatasetUseCase {

    private final UpdateProblemSetUseCase updateProblemSetUseCase;
    private final StoreDatasetFilePort storeDatasetFilePort;
    private final ProblemDatasetPersistencePort problemDatasetPersistencePort;

    public ProblemSetWithDatasetUpdateService(
            UpdateProblemSetUseCase updateProblemSetUseCase,
            StoreDatasetFilePort storeDatasetFilePort,
            ProblemDatasetPersistencePort problemDatasetPersistencePort
    ) {
        this.updateProblemSetUseCase = updateProblemSetUseCase;
        this.storeDatasetFilePort = storeDatasetFilePort;
        this.problemDatasetPersistencePort = problemDatasetPersistencePort;
    }

    @Override
    @Transactional
    public ProblemSetUpdateCommandResult handle(
            UpdateProblemSetCommand problemSetCommand,
            UploadProblemDatasetCommand datasetCommand
    ) {
        StoredDatasetFile storedFile = null;

        try {
            ProblemSetUpdateCommandResult result = updateProblemSetUseCase.handle(problemSetCommand);

            storedFile = storeDatasetFilePort.store(datasetCommand);

            problemDatasetPersistencePort.deactivateActiveDatasetsByProblemSetId(
                    problemSetCommand.problemSetId()
            );

            problemDatasetPersistencePort.saveUploadedDataset(
                    problemSetCommand.problemSetId(),
                    storedFile
            );

            return result;
        } catch (DomainException e) {
            if (storedFile != null) {
                storeDatasetFilePort.delete(storedFile.getFilePath());
            }
            throw e;
        } catch (Exception e) {
            if (storedFile != null) {
                storeDatasetFilePort.delete(storedFile.getFilePath());
            }
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }
}
