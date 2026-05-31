package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;
import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetUrlPort;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.execution.application.port.LoadExecutionDatasetPort;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
@Component
@RequiredArgsConstructor
public class ProblemDatasetPersistenceAdapter implements
        ProblemDatasetPersistencePort,
        LoadExecutionDatasetPort,
        LoadActiveDatasetUrlPort {

    private final SpringDataProblemDatasetRepository problemDatasetRepository;
    private final EntityManager entityManager;

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
        ProblemSetJpaEntity problemSet = entityManager.getReference(
                ProblemSetJpaEntity.class,
                problemSetId
        );

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

    @Override
    public String loadActiveDatasetUrl(Long problemSetId) {
        return problemDatasetRepository
                .findFirstByProblemSet_ProblemSetIdAndStatusOrderByDatasetIdDesc(problemSetId, "ACTIVE")
                .map(ProblemDatasetJpaEntity::getFileUrl)
                .orElse(null);
    }

    @Override
    public void deactivateActiveDatasetsByProblemSetId(Long problemSetId) {
        problemDatasetRepository
                .findAllByProblemSet_ProblemSetIdAndStatus(problemSetId, "ACTIVE")
                .forEach(ProblemDatasetJpaEntity::deactivate);
    }
}
