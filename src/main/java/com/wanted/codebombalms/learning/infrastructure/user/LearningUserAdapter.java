package com.wanted.codebombalms.learning.infrastructure.user;

import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
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
}
