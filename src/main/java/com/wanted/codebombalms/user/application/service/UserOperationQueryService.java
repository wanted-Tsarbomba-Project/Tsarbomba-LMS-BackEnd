package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.usecase.UserOperationQueryUseCase;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserOperationQueryService implements UserOperationQueryUseCase {

    private final UserRepository userRepository;

    @Override
    public List<UserOperationView> findStudents() {
        return userRepository.findAllByRole(UserRole.STUDENT).stream()
                .map(this::toView)
                .toList();
    }

    private UserOperationView toView(User user) {
        return new UserOperationView(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }
}
