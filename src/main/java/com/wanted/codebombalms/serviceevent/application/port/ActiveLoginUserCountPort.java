package com.wanted.codebombalms.serviceevent.application.port;

import java.time.LocalDateTime;

/**
 * 기간 내 로그인 고유 회원 수 조회 포트.
 * 소스는 auth 의 login_history — 읽기 전용.
 */
public interface ActiveLoginUserCountPort {

    long countDistinctLoginUsers(LocalDateTime start, LocalDateTime end);
}
