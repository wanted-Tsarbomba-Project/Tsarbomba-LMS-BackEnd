package com.wanted.codebombalms.submission.application.port;

public interface RecordSubmissionMetricsPort {

    void recordTotal(long nanos);

    void recordPrepare(long nanos);

    void recordGrading(long nanos);

    void recordSave(long nanos);
}
