package com.wanted.codebombalms.global.infrastructure.cleanup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

// 도메인에서 이름, 보존 기간, 삭제 작업만 넘겨 사용할 수 있는 기본 하드 딜리트 대상이다.
public class DefaultHardDeleteTarget implements HardDeleteTarget {

    private final String targetName;
    private final Duration retention;
    private final HardDeleteOperation operation;

    public DefaultHardDeleteTarget(
            String targetName,
            Duration retention,
            HardDeleteOperation operation
    ) {
        this.targetName = requireText(targetName, "targetName");
        this.retention = Objects.requireNonNull(retention, "retention must not be null");
        this.operation = Objects.requireNonNull(operation, "operation must not be null");

        if (retention.isNegative() || retention.isZero()) {
            throw new IllegalArgumentException("보존 기간은 0보다 커야 합니다.");
        }
    }

    @Override
    // 삭제 대상 이름을 반환한다.
    public String targetName() {
        return targetName;
    }

    @Override
    // 데이터 보존 기간을 반환한다.
    public Duration retention() {
        return retention;
    }

    @Override
    // 기준 시각 이전의 데이터를 설정된 삭제 작업으로 하드 딜리트한다.
    public int hardDeleteBefore(LocalDateTime threshold) {
        Objects.requireNonNull(threshold, "기준 시각은 null일 수 없습니다.");
        return operation.deleteBefore(threshold);
    }

    // 필수 문자열 값이 비어 있지 않은지 검증한다.
    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }
        return value;
    }
}
