package com.wanted.codebombalms.domain.problems.dataset.entitiy;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProblemDataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasetId;


    private String originalFileName;

    private String storedFileName;

    private  String fileUrl;

    private String filePath;

    private Long fileSize;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    public static ProblemDataset createUploaded(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            Long fileSize
    ) {
        ProblemDataset dataset = new ProblemDataset();
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

    public void connectProblem(Problem problem) {
        this.problem = problem;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getDatasetId() { return datasetId; }
    public Problem getProblem() { return problem; }
    public String getOriginalFileName() { return originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public String getFileUrl() { return fileUrl; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }

    protected ProblemDataset() {
    }
}
