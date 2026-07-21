package com.wanted.codebombalms.problems.generation.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.generation.application.command.StoreProblemSetDraftDatasetCommand;
import com.wanted.codebombalms.problems.generation.application.port.DeleteProblemSetDraftDatasetPort;
import com.wanted.codebombalms.problems.generation.application.port.GenerateProblemSetDraftDatasetUrlPort;
import com.wanted.codebombalms.problems.generation.application.port.StoreProblemSetDraftDatasetPort;
import com.wanted.codebombalms.problems.generation.domain.StoredProblemSetDraftDataset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GcsProblemSetDraftDatasetStorageAdapter implements
        StoreProblemSetDraftDatasetPort,
        GenerateProblemSetDraftDatasetUrlPort,
        DeleteProblemSetDraftDatasetPort {

    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String DRAFT_DIRECTORY = "drafts";
    private static final long SIGNED_URL_DURATION_MINUTES = 30;

    private final GcpStorageProperties properties;
    private final GcpStorageClientFactory storageClientFactory;
    private volatile Storage storage;

    public GcsProblemSetDraftDatasetStorageAdapter(
            GcpStorageProperties properties,
            GcpStorageClientFactory storageClientFactory
    ) {
        this.properties = properties;
        this.storageClientFactory = storageClientFactory;
    }

    @Override
    public StoredProblemSetDraftDataset store(StoreProblemSetDraftDatasetCommand command) {
        String originalFileName = sanitizeFileName(command.originalFileName());
        String storedFileName = command.draftToken() + "_" + originalFileName;
        String objectName = buildObjectName(command, storedFileName);

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                            BlobId.of(properties.getStorage().getBucket(), objectName)
                    )
                    .setContentType(CSV_CONTENT_TYPE)
                    .build();

            getStorage().create(blobInfo, command.content());

            return new StoredProblemSetDraftDataset(
                    originalFileName,
                    storedFileName,
                    objectName,
                    command.fileSize()
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED,
                    e
            );
        }
    }

    @Override
    public String generate(String objectName) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    properties.getStorage().getBucket(),
                    objectName
            ).build();

            return getStorage()
                    .signUrl(
                            blobInfo,
                            SIGNED_URL_DURATION_MINUTES,
                            TimeUnit.MINUTES,
                            Storage.SignUrlOption.withV4Signature()
                    )
                    .toString();
        } catch (Exception e) {
            log.error(
                    "event=problem_set_draft_dataset_signed_url_failed bucket={} objectName={} exceptionType={}",
                    properties.getStorage().getBucket(),
                    objectName,
                    e.getClass().getSimpleName(),
                    e
            );

            throw new ExternalServiceException(
                    ProblemErrorCode.PROBLEM_DATASET_ACCESS_URL_FAILED,
                    e
            );
        }
    }

    @Override
    public void delete(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }

        try {
            getStorage().delete(
                    BlobId.of(properties.getStorage().getBucket(), objectName)
            );
        } catch (Exception e) {
            log.warn(
                    "event=problem_set_draft_dataset_delete_failed objectName={}",
                    objectName,
                    e
            );
        }
    }

    private Storage getStorage() throws IOException {
        if (storage == null) {
            synchronized (this) {
                if (storage == null) {
                    storage = storageClientFactory.create();
                }
            }
        }

        return storage;
    }

    private String buildObjectName(
            StoreProblemSetDraftDatasetCommand command,
            String storedFileName
    ) {
        String prefix = normalizePrefix(properties.getStorage().getDatasetPrefix());

        return prefix
                + "/"
                + DRAFT_DIRECTORY
                + "/"
                + command.operatorId()
                + "/"
                + command.draftToken()
                + "/"
                + storedFileName;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null) {
            return "";
        }

        return prefix.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "dataset.csv";
        }

        return originalFileName
                .replace("\\", "_")
                .replace("/", "_");
    }
}
