package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.application.port.LoadMyBadgesPort;
import com.wanted.codebombalms.badge.application.query.MyBadgeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyBadgeReadAdapter implements LoadMyBadgesPort {

    private final SpringDataUserBadgeRepository userBadgeRepository;

    @Override
    public List<MyBadgeRow> loadMyBadges(Long userId) {
        return userBadgeRepository.findMyBadgeRows(userId);
    }
}
