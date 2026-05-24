package com.wanted.codebombalms.global.infrastructure.cleanup;

import java.time.LocalDateTime;

// 기준 시각 이전의 데이터를 실제로 하드 딜리트하는 작업을 정의한다.
@FunctionalInterface
public interface HardDeleteOperation {

    // 전달받은 기준 시각 이전의 데이터를 삭제하고 삭제 건수를 반환한다.
    int deleteBefore(LocalDateTime threshold);
}
