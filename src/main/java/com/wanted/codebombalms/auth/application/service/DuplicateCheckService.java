package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.DuplicateCheckUseCase;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuplicateCheckService implements DuplicateCheckUseCase {

    private final UserRepository userRepository;

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}