package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.user.application.usecase.GetLoginHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLoginHistoryService implements GetLoginHistoryUseCase {

    private static final int PAGE_SIZE = 20;

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    public List<LoginHistory> getLoginHistory(Long userId, int page) {
        int safePage = Math.max(page, 0); // 음수 페이지 → PageRequest 예외(500) 방지
        return loginHistoryRepository.findByUserId(userId, safePage, PAGE_SIZE);
    }
}
