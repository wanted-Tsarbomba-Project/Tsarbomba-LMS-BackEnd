package com.wanted.codebombalms.serviceevent.application.port;

import java.time.LocalDateTime;

/**
 * 기간 내 로그인한 고유 회원 수 조회 포트 (#608).
 * 소스는 auth 의 login_history — 읽기 전용(테이블·기존 로직 무변경 원칙).
 */
public interface ActiveLoginUserCountPort {

    long countDistinctLoginUsers(LocalDateTime start, LocalDateTime end);
}
