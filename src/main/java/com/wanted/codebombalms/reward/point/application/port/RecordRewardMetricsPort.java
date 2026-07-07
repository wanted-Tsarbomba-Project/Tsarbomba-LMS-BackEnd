package com.wanted.codebombalms.reward.point.application.port;

public interface RecordRewardMetricsPort {

    void recordScheduled();

    void recordSchedule(ScheduleResult result);

    void recordProcessed(ProcessResult result);

    void recordProcess(ProcessResult result, long elapsedNanos);

    void updatePending(long pendingCount);

    enum ScheduleResult {
        SCHEDULED("scheduled"),
        ALREADY_SCHEDULED("already_scheduled"),
        FAILED("failed");

        private final String tagValue;

        ScheduleResult(String tagValue) {
            this.tagValue = tagValue;
        }

        public String tagValue() {
            return tagValue;
        }
    }

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
