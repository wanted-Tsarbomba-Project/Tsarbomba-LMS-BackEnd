package com.wanted.codebombalms.user.application.port;

import java.time.LocalDateTime;
import java.util.List;

public interface UserHardDeletePort {

    // 소프트딜리트 후 기준 시각을 지난 회원 ID 목록 (@SQLRestriction 우회 — 네이티브)
    List<Long> findDeletedUserIdsBefore(LocalDateTime threshold);

    // 해당 회원의 PII 물리 삭제 (users + 인증·기기·동의 이력). 활동데이터는 익명화(유지)
    void purgeByUserId(Long userId);
}
