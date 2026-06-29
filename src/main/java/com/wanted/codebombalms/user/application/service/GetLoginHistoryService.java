package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.infrastructure.metrics.AuthMetrics;
import com.wanted.codebombalms.user.application.usecase.GetLoginHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLoginHistoryService implements GetLoginHistoryUseCase {

    private static final int PAGE_SIZE = 20;

    private final LoginHistoryRepository loginHistoryRepository;
    private final AuthMetrics authMetrics;

    @Override
    public List<LoginHistory> getLoginHistory(Long userId, int page) {
        int safePage = Math.max(page, 0); // 음수 페이지 → PageRequest 예외(500) 방지

        long startedAt = System.nanoTime();

        // 인덱스 부재 시 풀스캔 + filesort 발생 구간 (login_history)
        List<LoginHistory> history = loginHistoryRepository.findByUserId(userId, safePage, PAGE_SIZE);

        long elapsedNanos = System.nanoTime() - startedAt;
        authMetrics.recordLoginHistoryQuery(elapsedNanos);
        log.info("event=auth_login_history_queried userId={} page={} resultCount={} durationMs={}",
                userId, safePage, history.size(), elapsedNanos / 1_000_000);

        return history;
    }
}
