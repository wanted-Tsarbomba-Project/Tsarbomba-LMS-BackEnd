package com.wanted.codebombalms.learning.infrastructure.user;

import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningUserAdapter implements LearningUserPort {

    private final UserRepository userRepository;

    @Override
    public String findUserName(Long userId) {
        return userRepository.findByUserId(userId)
                .map(user -> user.getName() != null ? user.getName() : user.getNickname())
                .orElse("알 수 없음");
    }

    @Override
    public Map<Long, String> findUserNames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user.getName() != null ? user.getName() : user.getNickname(),
                        (left, right) -> left
                ));
    }
}
