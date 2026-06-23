package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.LoginActivityQueryUseCase;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginActivityQueryService implements LoginActivityQueryUseCase {

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    public Map<Long, LocalDateTime> findLatestLoginAtByUserIds(List<Long> userIds) {
        return loginHistoryRepository.findLatestLoginAtByUserIds(userIds);
    }
}
