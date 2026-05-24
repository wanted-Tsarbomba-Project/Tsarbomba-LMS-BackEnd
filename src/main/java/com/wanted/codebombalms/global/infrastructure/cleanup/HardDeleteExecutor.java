package com.wanted.codebombalms.global.infrastructure.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

// 등록된 하드 딜리트 대상의 보존 기간을 기준으로 삭제 기준 시각을 계산하고 실행한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class HardDeleteExecutor {

    private final List<HardDeleteTarget> targets;
    private final Clock clock;

    // 단일 하드 딜리트 대상을 실행한다.
    public int execute(HardDeleteTarget target) {
        Objects.requireNonNull(target, "하드 딜리트 대상은 null일 수 없습니다.");

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.minus(target.retention());

        int deletedCount = target.hardDeleteBefore(threshold);

        log.info(
                "하드 딜리트가 완료되었습니다. 대상={}, 보존기간={}, 삭제기준시각={}, 삭제건수={}",
                target.targetName(),
                target.retention(),
                threshold,
                deletedCount
        );

        return deletedCount;
    }

    // 스프링 빈으로 등록된 모든 하드 딜리트 대상을 실행한다.
    public int executeAll() {
        if (targets.isEmpty()) {
            log.debug("등록된 하드 딜리트 대상이 없습니다.");
            return 0;
        }

        int totalDeletedCount = 0;

        for (HardDeleteTarget target : targets) {
            totalDeletedCount += execute(target);
        }

        return totalDeletedCount;
    }
}
