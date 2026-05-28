package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemDatasetPersistenceAdapter implements
        ProblemDatasetPersistencePort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemDatasetRepository problemDatasetRepository;


    @Override
    public ProblemDataset saveUploadedDataset(StoredDatasetFile storedFile) {
        ProblemDatasetJpaEntity dataset = ProblemDatasetJpaEntity.createUploaded(
                storedFile.getOriginalFileName(),
                storedFile.getStoredFileName(),
                storedFile.getFileUrl(),
                storedFile.getFilePath(),
                storedFile.getFileSize()
        );

        ProblemDatasetJpaEntity savedDataset = problemDatasetRepository.save(dataset);

        return ProblemDataset.restore(
                savedDataset.getDatasetId(),
                savedDataset.getOriginalFileName(),
                savedDataset.getStoredFileName(),
                savedDataset.getFileUrl(),
                savedDataset.getFilePath(),
                savedDataset.getStatus()
        );
    }


    @Override
    public ProblemDataset saveUploadedDataset(Long problemSetId, StoredDatasetFile storedFile) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = ProblemDatasetJpaEntity.createUploaded(
                storedFile.getOriginalFileName(),
                storedFile.getStoredFileName(),
                storedFile.getFileUrl(),
                storedFile.getFilePath(),
                storedFile.getFileSize()
        );

        dataset.connectProblemSet(problemSet);

        ProblemDatasetJpaEntity savedDataset = problemDatasetRepository.save(dataset);

        return ProblemDataset.restore(
                savedDataset.getDatasetId(),
                savedDataset.getOriginalFileName(),
                savedDataset.getStoredFileName(),
                savedDataset.getFileUrl(),
                savedDataset.getFilePath(),
                savedDataset.getStatus()
        );
    }

}
