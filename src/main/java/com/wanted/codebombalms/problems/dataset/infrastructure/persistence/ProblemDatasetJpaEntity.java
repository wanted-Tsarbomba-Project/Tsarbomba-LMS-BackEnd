package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_dataset")
public class ProblemDatasetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasetId;

    private String originalFileName;

    private String storedFileName;

    private String fileUrl;

    private String filePath;

    private Long fileSize;

    @Column(name = "meta_data", columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id")
    private ProblemSetJpaEntity problemSet;

    protected ProblemDatasetJpaEntity() {
    }

    public static ProblemDatasetJpaEntity createUploaded(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            Long fileSize
    ) {
        ProblemDatasetJpaEntity dataset = new ProblemDatasetJpaEntity();
        dataset.originalFileName = originalFileName;
        dataset.storedFileName = storedFileName;
        dataset.fileUrl = fileUrl;
        dataset.filePath = filePath;
        dataset.fileSize = fileSize;
        dataset.status = "ACTIVE";
        dataset.createdAt = LocalDateTime.now();
        dataset.updatedAt = dataset.createdAt;
        return dataset;
    }

    public void connectProblemSet(ProblemSetJpaEntity problemSet) {
        this.problemSet = problemSet;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public ProblemSetJpaEntity getProblemSet() {
        return problemSet;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
    }
    public String getMetadata() {
        return metadata;
    }
}
