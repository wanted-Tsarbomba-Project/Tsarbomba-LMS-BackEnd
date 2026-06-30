package com.wanted.codebombalms.reward.point.application.port;

public interface RecordRewardMetricsPort {

    void recordScheduled();

    void recordProcessed(ProcessResult result);

    void recordProcess(long elapsedNanos);

    void updatePending(long pendingCount);

    enum ProcessResult {
        COMPLETED("completed"),
        RETRY("retry"),
        FAILED("failed"),
        SKIPPED("skipped"),
        NOT_FOUND("not_found"),
        ERROR("error");

        private final String tagValue;

        ProcessResult(String tagValue) {
            this.tagValue = tagValue;
        }

        public String tagValue() {
            return tagValue;
        }
    }
}
