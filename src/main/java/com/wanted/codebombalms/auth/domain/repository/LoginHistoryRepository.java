package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LoginHistoryRepository {

    LoginHistory save(LoginHistory loginHistory);

    Map<Long, LocalDateTime> findLatestLoginAtByUserIds(List<Long> userIds);
}
