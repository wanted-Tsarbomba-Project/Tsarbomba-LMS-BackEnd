package com.wanted.codebombalms.global.application.cleanup.port;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

// 공통 하드 딜리트 실행기가 호출할 도메인별 삭제 대상을 정의한다.
public interface HardDeleteTarget {

    // 로그와 식별에 사용할 삭제 대상 이름을 반환한다.
    String targetName();

    // 데이터 보존 기간을 반환한다.
    TemporalAmount retention();

    // 기준 시각 이전의 데이터를 하드 딜리트하고 삭제 건수를 반환한다.
    int hardDeleteBefore(LocalDateTime threshold);
}
