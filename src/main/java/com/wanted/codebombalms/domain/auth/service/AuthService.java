package com.wanted.codebombalms.domain.auth.service;

import com.wanted.codebombalms.domain.auth.dto.request.SignupRequest;
import com.wanted.codebombalms.domain.user.entity.User;
import com.wanted.codebombalms.domain.user.exception.UserErrorCode;
import com.wanted.codebombalms.domain.user.repository.UserRepository;
import com.wanted.codebombalms.global.error.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.createLocalUser(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getNickname(),
                request.getPhone()
        );

        userRepository.save(user);
    }
}