package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.ActiveLoginUserCountPort;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginUserCountAdapter implements ActiveLoginUserCountPort {

    private final SpringDataLoginHistoryRepository loginHistoryRepository;

    @Override
    public long countDistinctLoginUsers(LocalDateTime start, LocalDateTime end) {
        return loginHistoryRepository.countDistinctUserIdBetween(start, end);
    }
}
